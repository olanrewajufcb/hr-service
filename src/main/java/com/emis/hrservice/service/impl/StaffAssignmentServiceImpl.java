package com.emis.hrservice.service.impl;

import com.emis.hrservice.domain.db.Staff;
import com.emis.hrservice.domain.db.StaffAssignment;
import com.emis.hrservice.dto.request.CreateStaffAssignmentRequest;
import com.emis.hrservice.dto.response.StaffAssignmentResponse;
import com.emis.hrservice.enums.AssignmentRole;
import com.emis.hrservice.enums.StaffCategory;
import com.emis.hrservice.events.outbox.DomainEvent;
import com.emis.hrservice.events.outbox.OutboxEvent;
import com.emis.hrservice.events.outbox.OutboxEventRepository;
import com.emis.hrservice.events.outbox.StaffAssignedEvent;
import com.emis.hrservice.exceptions.*;
import com.emis.hrservice.mapper.StaffAssignmentMapper;
import com.emis.hrservice.repository.StaffAssignmentRepository;
import com.emis.hrservice.repository.StaffRepository;
import com.emis.hrservice.repository.StaffTeachingQualificationRepository;
import com.emis.hrservice.service.StaffAssignmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaffAssignmentServiceImpl implements StaffAssignmentService {

    private final StaffAssignmentRepository staffAssignmentRepository;
    private final StaffRepository staffRepository;
    private final StaffAssignmentMapper assignmentMapper;
    private final TransactionalOperator transactionalOperator;
    private final StaffTeachingQualificationRepository staffTeachingQualificationRepository;
    private final OutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<StaffAssignmentResponse> assignStaffToClass(CreateStaffAssignmentRequest request,
                                   String staffCode, String schoolCode, String requestId) {
        return staffRepository
                .findByStaffCodeAndSchoolCodeAndIsDeletedFalse(staffCode, schoolCode)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Staff not found")))
                .flatMap(staff ->
                        validateTeachingEligibility(staff, request)
                                .then(Mono.defer(() -> createAssignment(staff, request)
                                .flatMap(assignment ->
                                        writeOutboxEvent(assignment, staff, request, requestId)
                                                .thenReturn(assignment)
                                ))
                                        .as(transactionalOperator::transactional)
                ))
                .map(StaffAssignmentResponse::from);
    }

    @Override
    public Flux<StaffAssignmentResponse> viewStaffAssignments(String staffCode, String schoolCode) {
        return staffRepository.findByStaffCodeAndSchoolCodeAndIsDeletedFalse(staffCode, schoolCode)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Staff not found")))
                .flatMapMany(staff -> staffAssignmentRepository.findByStaffIdAndIsDeletedFalse(staff.getStaffId())
                        .map(StaffAssignmentResponse::from));
    }

    @Override
    public Flux<StaffAssignmentResponse> viewStaffAssignmentById(Long staffId) {
        return staffRepository.findById(staffId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Staff not found")))
                .flatMapMany(staff -> staffAssignmentRepository.findByStaffIdAndIsDeletedFalse(staff.getStaffId())
                        .map(StaffAssignmentResponse::from));
    }

    private Mono<Staff> validateTeachingEligibility(
      Staff staff, CreateStaffAssignmentRequest request) {

        boolean isTeachingStaff = request.assignmentRole().equals(AssignmentRole.FORM_TEACHER)
                || request.assignmentRole().equals(AssignmentRole.SUBJECT_TEACHER)
                || request.assignmentRole().equals(AssignmentRole.ASSISTANT_TEACHER);

        if(!isTeachingStaff){
            return Mono.just(staff);
        }
            if(staff.getStaffCategory() != StaffCategory.TEACHING){
                return Mono.error(new ValidationException("Non-teaching staff can not be assigned teaching roles"));
            }
        return staffTeachingQualificationRepository
                .staffHasQualification(staff.getStaffId())
                .filter(Boolean.TRUE::equals)
                .switchIfEmpty(Mono.error(
                        new ValidationException("Staff does not have a valid teaching qualification")))
                .thenReturn(staff);
    }

    private Mono<Void> writeOutboxEvent(
            StaffAssignment assignment,
            Staff staff,
            CreateStaffAssignmentRequest request,
            String correlationId
    ) {

        StaffAssignedEvent payload =
                StaffAssignedEvent.builder()
                        .assignmentId(assignment.getAssignmentId())
                        .schoolId(staff.getSchoolId())
                        .schoolCode(staff.getSchoolCode())
                        .staffId(staff.getStaffId())
                        .staffCode(staff.getStaffCode())
                        .staffName(staff.getFullName())
                        .classId(request.classId())
                        .sectionId(request.sectionId())
                        .subjectId(request.subjectId())
                        .academicYear(request.academicYear())
                        .assignmentRole(request.assignmentRole().name())
                        .build();

        DomainEvent<StaffAssignedEvent> event =
                DomainEvent.<StaffAssignedEvent>builder()
                        .eventId(UUID.randomUUID())
                        .eventType("STAFF_ASSIGNED_TO_CLASS")
                        .eventVersion(1)
                        .occurredAt(Instant.now())
                        .producer("hr-service")
                        .correlationId(correlationId)
                        .data(payload)
                        .build();

        return outboxRepository.save(
                OutboxEvent.builder()
                        .eventId(event.getEventId())
                        .aggregateType("STAFF_ASSIGNMENT")
                        .aggregateId(assignment.getAssignmentId().toString())
                        .eventType(event.getEventType())
                        .topic("hr.events.v1")
                        .payload(objectMapper.valueToTree(event))
                        .status("PENDING")
                        .build()
        ).then();
    }

    private Mono<StaffAssignment> createAssignment(
            Staff staff, CreateStaffAssignmentRequest request) {

        return staffAssignmentRepository
                .findByStaffIdAndClassIdAndSubjectIdAndAcademicYearAndIsDeletedFalse(
                        staff.getStaffId(),
                        request.classId(),
                        request.subjectId(),
                        request.academicYear())
                .switchIfEmpty(addNewStaffAssignment(staff, assignmentMapper.toEntity(request)));
    }
    private Mono<StaffAssignment> addNewStaffAssignment(
            Staff staff, StaffAssignment assignment) {

        assignment.setStaffId(staff.getStaffId());
        return staffAssignmentRepository.save(assignment);
    }

}
