package com.emis.hrservice.service.impl;

import com.emis.hrservice.config.ServiceConfigurationProperties;
import com.emis.hrservice.domain.db.Staff;
import com.emis.hrservice.domain.db.StaffServiceHistory;
import com.emis.hrservice.dto.request.CreateStaffRequest;
import com.emis.hrservice.dto.request.UpdateEmergencyContactRequest;
import com.emis.hrservice.dto.request.UpdateStaffBioRequest;
import com.emis.hrservice.dto.response.CreateStaffResponse;
import com.emis.hrservice.dto.response.EmergencyContactResponse;
import com.emis.hrservice.dto.response.SchoolDetailsResponse;
import com.emis.hrservice.dto.response.UpdateStaffBioResponse;
import com.emis.hrservice.enums.ChangeType;
import com.emis.hrservice.exceptions.HrServiceException;
import com.emis.hrservice.exceptions.ResourceNotFoundException;
import com.emis.hrservice.exceptions.ResourceTimeoutException;
import com.emis.hrservice.mapper.StaffMapper;
import com.emis.hrservice.repository.StaffRepository;
import com.emis.hrservice.repository.StaffServiceHistoryRepository;
import com.emis.hrservice.service.StaffManagementService;
import com.emis.hrservice.service.cache.SchoolCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeoutException;
@RequiredArgsConstructor
@Slf4j
@Service
public class StaffManagementServiceImpl implements StaffManagementService {

    private final StaffRepository staffRepository;
    private final StaffServiceHistoryRepository staffServiceHistoryRepository;
    private final SchoolCacheService schoolCacheService;
    private final StaffMapper staffMapper;
    private final TransactionalOperator transactionalOperator;
    private final ServiceConfigurationProperties properties;
    private static final String STAFF_NOT_FOUND = "Staff with code %s does not exist";
    @Override
    public Mono<CreateStaffResponse> createStaff(CreateStaffRequest request, String requestId) {

    return schoolCacheService
           .getSchoolDetails(request.schoolCode())
           .flatMap(
            schoolDetails -> {
              Staff staff = staffMapper.toEntity(request);
              staff.setSchoolId(schoolDetails.schoolId());
              log.info("[{}] Logging entity before saving {}", requestId, staff);
              return staffRepository
                  .findByStaffCodeAndSchoolCodeAndIsDeletedFalse(
                      request.staffCode(), schoolDetails.schoolCode())
                  .map(CreateStaffResponse::from)
                  .switchIfEmpty(Mono.defer(() -> createNewStaff(request, schoolDetails)));
            });
    }

    @Override
    public Mono<CreateStaffResponse> retrieveStaff(String schoolCode, String staffCode, String requestId) {
        return  staffRepository.findByStaffCodeAndSchoolCodeAndIsDeletedFalse(staffCode, schoolCode)
                                .flatMap(staff ->
                                        Mono.just(CreateStaffResponse.from(staff))
                                )
                                .switchIfEmpty(
                                        Mono.error(new ResourceNotFoundException(String.format(STAFF_NOT_FOUND, staffCode)))
                                );
    }

    @Override
    public Mono<CreateStaffResponse> retrieveStaffById(Long staffId, String requestId) {
        return staffRepository.findById(staffId)
                .flatMap(staff ->
                        Mono.just(CreateStaffResponse.from(staff))
                )
                .switchIfEmpty(
                        Mono.error(new ResourceNotFoundException(String.format("Staff with id %s does not exist", staffId)))
                );
    }

    @Override
    public Mono<Page<CreateStaffResponse>> retrieveStaffsBySchoolCode(String schoolCode,
                                                     Pageable pageable, String requestId) {
        int size = pageable.getPageSize();
        long offset = pageable.getOffset();
    return Mono.zip(staffRepository
                            .findBySchoolCodeAndIsDeletedFalse(schoolCode, size, offset)
                            .collectList(),
                        staffRepository.countBySchoolCodeAndIsDeletedFalse(schoolCode))
                    .timeout(Duration.ofSeconds(properties.getTimeout()))
                    .map(
                        tuple -> {
                          List<Staff> staffs = tuple.getT1();
                          long total = tuple.getT2();
                          List<CreateStaffResponse> responses =
                              total == 0
                                  ? List.of()
                                  : staffs.stream().map(CreateStaffResponse::from).toList();
                          return (Page<CreateStaffResponse>)
                              new PageImpl<>(responses, pageable, total);
                        })
            .onErrorMap(ex -> {
                log.error("[{}] Error retrieving staff with code {}", requestId, schoolCode, ex);
                if (ex instanceof TimeoutException) {
                    return new ResourceTimeoutException("DB timeout :::", ex);
                }
                return new HrServiceException(
                        "Error fetching staff ", ex);
            });
    }

    @Override
    public Mono<UpdateStaffBioResponse> updateStaffBio(String staffCode, UpdateStaffBioRequest request, String requestId) {
        return Mono.defer(() -> staffRepository.findByStaffCodeAndSchoolCodeAndIsDeletedFalse(staffCode, request.schoolCode())
                                .switchIfEmpty(Mono.error(new ResourceNotFoundException(String.format(STAFF_NOT_FOUND, staffCode))))
                                .flatMap(existingStaff -> {
                                    existingStaff.setDateOfBirth(request.dateOfBirth());
                                    existingStaff.setEmail(request.email());
                                    existingStaff.setPhone(request.phoneNumber());
                                    existingStaff.setAddress(request.address());
                                    existingStaff.setNationality(request.nationality());
                                    return staffRepository.save(existingStaff);

                                })
                                .as(transactionalOperator::transactional))

                .map(UpdateStaffBioResponse::from);
    }

    @Override
    public Mono<EmergencyContactResponse> updateStaffEmergencyContact(String staffCode, UpdateEmergencyContactRequest request, String requestId) {
        return Mono.defer(() -> staffRepository.findByStaffCodeAndSchoolCodeAndIsDeletedFalse(staffCode, request.schoolCode())
                                .switchIfEmpty(Mono.error(new ResourceNotFoundException(String.format(STAFF_NOT_FOUND, staffCode))))
                                .flatMap(existingStaff -> {
                                    existingStaff.setEmergencyContactName(request.name());
                                    existingStaff.setEmergencyContactPhone(request.phoneNumber());
                                    existingStaff.setEmergencyContactRelationship(request.relationship());
                                    return staffRepository.save(existingStaff);

                                })
                                .as(transactionalOperator::transactional))

                .map(EmergencyContactResponse::from);
    }

    private Mono<CreateStaffResponse> createNewStaff(
            CreateStaffRequest request,
            SchoolDetailsResponse schoolDetails
    ) {
        Staff staff = staffMapper.toEntity(request);
        staff.setSchoolId(schoolDetails.schoolId());
        staff.setSchoolCode(schoolDetails.schoolCode());
        staff.setSchoolName(schoolDetails.schoolName());

    return staffRepository
        .save(staff)
        .flatMap(
            savedStaff ->
                staffServiceHistoryRepository
                    .save(
                        StaffServiceHistory.builder()
                            .staffId(savedStaff.getStaffId())
                            .schoolId(schoolDetails.schoolId())
                            .changeType(ChangeType.APPOINTMENT.name())
                            .position(savedStaff.getStaffRole().name())
                            .startDate(savedStaff.getAppointmentDate())
                            .createdAt(LocalDateTime.now())
                            .build())
                    .thenReturn(savedStaff))
        .as(transactionalOperator::transactional)
        .map(CreateStaffResponse::from);
    }

}
