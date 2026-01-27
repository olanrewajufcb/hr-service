package com.emis.hrservice.service.impl;


import com.emis.hrservice.config.ServiceConfigurationProperties;
import com.emis.hrservice.domain.db.Staff;
import com.emis.hrservice.domain.db.StaffAcademicQualification;
import com.emis.hrservice.dto.request.AddStaffAcademicQualificationRequest;
import com.emis.hrservice.dto.response.AddStaffAcademicQualificationResponse;
import com.emis.hrservice.exceptions.HrServiceException;
import com.emis.hrservice.exceptions.ResourceCreationException;
import com.emis.hrservice.exceptions.ResourceNotFoundException;
import com.emis.hrservice.exceptions.ResourceTimeoutException;
import com.emis.hrservice.mapper.StaffQualificationMapper;
import com.emis.hrservice.repository.StaffAcademicQualificationRepository;
import com.emis.hrservice.repository.StaffRepository;
import com.emis.hrservice.service.StaffAcademicQualificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Slf4j
@RequiredArgsConstructor
@Service
public class StaffAcademicQualificationServiceImp implements StaffAcademicQualificationService {

    private final StaffAcademicQualificationRepository staffAcademicQualificationRepository;
    private final StaffRepository staffRepository;
    private final TransactionalOperator transactionalOperator;
    private final StaffQualificationMapper qualificationMapper;
    private final ServiceConfigurationProperties properties;

    @Override
    public Mono<AddStaffAcademicQualificationResponse> addStaffAcademicQualification(String staffCode,
                              String schoolCode,
                              AddStaffAcademicQualificationRequest request, String requestId) {
    return staffRepository
        .findByStaffCodeAndSchoolCodeAndIsDeletedFalse(staffCode, schoolCode)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Staff not found with code " + staffCode)))
        .flatMap(
            staff ->
                 staffAcademicQualificationRepository
                         .findByStaffIdAndQualificationLevelAndYearObtainedAndIsDeletedFalse(
                        staff.getStaffId(),
                        request.qualificationLevel().name(),
                        request.yearObtained())
            .map(AddStaffAcademicQualificationResponse::from)
                         .switchIfEmpty(addNewStaffQualification(qualificationMapper.toEntity(request), staff))
            )
        .as(transactionalOperator::transactional)
            .doOnSuccess(response -> log.info("[{}] Staff qualification added successfully: {}", requestId, response))
        .doOnError(ex -> log.error("Error adding staff qualification: {}", ex.getMessage()))
        .onErrorMap(
            ex -> {
              log.error("Error adding staff qualifications: {}", ex.getMessage());
              return new ResourceCreationException("Error adding staff qualifications", ex);
            });
    }

    @Override
    public Mono<Page<AddStaffAcademicQualificationResponse>> retrieveStaffAcademicQualification(
            String staffCode, String schoolCode, Pageable pageable, String requestId) {
        int size = pageable.getPageSize();
        long offset = pageable.getOffset();

        return staffRepository
                .findByStaffCodeAndSchoolCodeAndIsDeletedFalse(staffCode, schoolCode)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Staff not found with code " + staffCode)))
                .flatMap(staff -> Mono.zip(staffAcademicQualificationRepository
                                .findByStaffIdAndIsDeletedFalse(staff.getStaffId(), size, offset).collectList(),
                                staffAcademicQualificationRepository
                                        .countByStaffIdAndIsDeletedFalse(staff.getStaffId())))
                .timeout(Duration.ofSeconds(properties.getTimeout()))
                .map(tuple -> {
                    List<StaffAcademicQualification> qualifications = tuple.getT1();
                    long total = tuple.getT2();
                    List<AddStaffAcademicQualificationResponse> responses =
                            total == 0
                                    ? List.of()
                                    : qualifications.stream().map(AddStaffAcademicQualificationResponse::from).toList();
                    return (Page<AddStaffAcademicQualificationResponse>)
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

    private Mono<AddStaffAcademicQualificationResponse> addNewStaffQualification(
            StaffAcademicQualification qualification, Staff staff) {
        qualification.setStaffId(staff.getStaffId());
        return staffAcademicQualificationRepository.save(qualification)
                .map(AddStaffAcademicQualificationResponse::from);
    }
}
