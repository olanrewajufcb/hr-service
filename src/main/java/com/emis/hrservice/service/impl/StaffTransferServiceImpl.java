package com.emis.hrservice.service.impl;

import com.emis.hrservice.domain.db.Staff;
import com.emis.hrservice.domain.db.StaffServiceHistory;
import com.emis.hrservice.dto.request.StaffTransferRequest;
import com.emis.hrservice.dto.response.SchoolDetailsResponse;
import com.emis.hrservice.dto.response.StaffTransferResponse;
import com.emis.hrservice.enums.ChangeType;
import com.emis.hrservice.events.outbox.DomainEvent;
import com.emis.hrservice.events.outbox.OutboxEvent;
import com.emis.hrservice.events.outbox.OutboxEventRepository;
import com.emis.hrservice.events.outbox.StaffTransferredEvent;
import com.emis.hrservice.exceptions.BadRequestException;
import com.emis.hrservice.exceptions.ResourceNotFoundException;
import com.emis.hrservice.exceptions.ValidationException;
import com.emis.hrservice.helper.AcademicServiceHelper;
import com.emis.hrservice.mapper.StaffTransferMapper;
import com.emis.hrservice.repository.StaffRepository;
import com.emis.hrservice.repository.StaffServiceHistoryRepository;
import com.emis.hrservice.service.StaffTransferService;
import com.emis.hrservice.service.cache.SchoolCacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

import static com.emis.hrservice.helper.AcademicServiceHelper.generateDeterministicEventId;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaffTransferServiceImpl implements StaffTransferService {
    private final StaffRepository staffRepository;
    private final StaffServiceHistoryRepository staffServiceHistoryRepository;
    private final StaffTransferMapper staffTransferMapper;
    private final SchoolCacheService schoolCacheService;
    private final TransactionalOperator transactionalOperator;
    private final ObjectMapper objectMapper;
    private final OutboxEventRepository outboxRepository;

    @Override
    public Mono<StaffTransferResponse> transferStaff(String staffCode,
                              StaffTransferRequest request, String requestId) {

        StaffServiceHistory staffServiceHistory = staffTransferMapper.toEntity(request);
        if (!request.changeType().equals(ChangeType.TRANSFER)){
            return Mono.error(new BadRequestException("Invalid change type, the value must be Transfer"));
        }

        if (request.fromSchoolCode().equals(request.toSchoolCode())) {
            return Mono.error(
                    new ValidationException("From school and to school cannot be the same")
            );
        }

        return Mono.zip(schoolCacheService
                .getSchoolDetails(request.toSchoolCode()),
                 staffRepository.findByStaffCodeAndSchoolCodeAndIsDeletedFalse(staffCode, request.fromSchoolCode())
                )
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Staff with code " + staffCode + " does not exist")))
                .flatMap(tuple -> {
                    SchoolDetailsResponse schoolDetails = tuple.getT1();
                    Staff staff = tuple.getT2();
                    return recordTransferAndUpdateStaff(staffServiceHistory, request,
                            staff, schoolDetails.schoolId())
                            .flatMap(history -> writeTransferOutboxEvent(
                                    staff, history, request, schoolDetails.schoolId(),  requestId)
                                    .thenReturn(history));

                })
                .as(transactionalOperator::transactional)
                .map(StaffTransferResponse::from);

    }

    @Override
    public Flux<StaffTransferResponse> getStaffServiceHistory(Long staffId) {
        return staffRepository
                .findById(staffId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Staff with id " + staffId + " does not exist")))
                .flatMapMany(staff -> staffServiceHistoryRepository.findByStaffId(staff.getStaffId()))
                .map(StaffTransferResponse::from);
    }

    private Mono<StaffServiceHistory> recordTransferAndUpdateStaff(
            StaffServiceHistory history,
            StaffTransferRequest request,
            Staff staff,
            Long toSchoolId) {

        log.info(
                "Transferring staffCode={} from {} to {}",
                staff.getStaffCode(),
                request.fromSchoolCode(),
                request.toSchoolCode()
        );

        history.setStaffId(staff.getStaffId());
        history.setSchoolId(staff.getSchoolId());
        history.setFromSchoolId(staff.getSchoolId());
        history.setToSchoolId(toSchoolId);
        history.setPosition(history.getPosition());
        history.setNewPosition(request.newPosition());
        history.setFromSchoolCode(request.fromSchoolCode());
        history.setToSchoolCode(request.toSchoolCode());

        return staffServiceHistoryRepository
                .existsActiveTransfer(
                        staff.getStaffId(),
                        toSchoolId,
                        request.startDate()
                )
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(
                                new BadRequestException(
                                        "Staff " + staff.getStaffCode() + " already has an active transfer"
                                )
                        );
                    }
                    return staffServiceHistoryRepository.updateStaffServiceHistory(
                            staff.getStaffId()
                    )

                            .then(staffServiceHistoryRepository.save(history)
                            .flatMap(saved ->
                                    staffRepository
                                            .updateStaffSchool(
                                                    staff.getStaffId(),
                                                    request.toSchoolCode(),
                                                    toSchoolId,
                                                    request.startDate()
                                            )
                                            .thenReturn(saved)
                            ));
                });
    }

    private Mono<Void> writeTransferOutboxEvent(
            Staff staff,
            StaffServiceHistory history,
            StaffTransferRequest request,
            Long toSchoolId,
            String correlationId
    ) {

        StaffTransferredEvent payload =
                StaffTransferredEvent.builder()
                        .historyId(history.getHistoryId())
                        .staffId(staff.getStaffId())
                        .staffCode(staff.getStaffCode())
                        .staffName(staff.getFullName())
                        .fromSchoolId(history.getFromSchoolId())
                        .fromSchoolCode(history.getFromSchoolCode())
                        .toSchoolId(toSchoolId)
                        .toSchoolCode(request.toSchoolCode())
                        .newPosition(request.newPosition())
                        .changeType(ChangeType.TRANSFER.name())
                        .startDate(request.startDate())
                        .build();

        String[] components = {
                request.changeType().toString(),
                request.newPosition(),
                request.fromSchoolCode(),
                request.toSchoolCode(),
                request.startDate().toString(),
                request.remarks() != null ? request.remarks() : ""
        };
        UUID eventId = generateDeterministicEventId(correlationId, components);

        DomainEvent<StaffTransferredEvent> event =
                DomainEvent.<StaffTransferredEvent>builder()
                        .eventId(eventId)
                        .eventType("STAFF_TRANSFERRED")
                        .eventVersion(1)
                        .occurredAt(Instant.now())
                        .producer("hr-service")
                        .correlationId(correlationId)
                        .data(payload)
                        .build();

        return outboxRepository.save(
                OutboxEvent.builder()
                        .eventId(event.getEventId())
                        .aggregateType("STAFF")
                        .aggregateId(staff.getStaffId().toString())
                        .eventType(event.getEventType())
                        .topic("hr.events.v1")
                        .payload(objectMapper.valueToTree(event))
                        .status("PENDING")
                        .retryCount(0)
                        .createdAt(Instant.now())
                        .build()
        ).then();
    }

}
