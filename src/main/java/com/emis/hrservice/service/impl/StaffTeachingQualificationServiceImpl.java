package com.emis.hrservice.service.impl;

import com.emis.hrservice.config.ServiceConfigurationProperties;
import com.emis.hrservice.domain.db.Staff;
import com.emis.hrservice.domain.db.StaffAcademicQualification;
import com.emis.hrservice.domain.db.StaffTeachingQualification;
import com.emis.hrservice.dto.request.AddStaffTeachingQualificationRequest;
import com.emis.hrservice.dto.response.AddStaffAcademicQualificationResponse;
import com.emis.hrservice.dto.response.StaffTeachingQualificationResponse;
import com.emis.hrservice.exceptions.HrServiceException;
import com.emis.hrservice.exceptions.ResourceCreationException;
import com.emis.hrservice.exceptions.ResourceNotFoundException;
import com.emis.hrservice.exceptions.ResourceTimeoutException;
import com.emis.hrservice.mapper.StaffTeachingQualificationMapper;
import com.emis.hrservice.repository.StaffRepository;
import com.emis.hrservice.repository.StaffTeachingQualificationRepository;
import com.emis.hrservice.service.StaffTeachingQualificationService;
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
import java.util.List;
import java.util.concurrent.TimeoutException;

@RequiredArgsConstructor
@Slf4j
@Service
public class StaffTeachingQualificationServiceImpl implements StaffTeachingQualificationService {

    private final StaffTeachingQualificationRepository staffTeachingQualificationRepository;
    private final StaffRepository staffRepository;
    private final ServiceConfigurationProperties properties;
    private final TransactionalOperator transactionalOperator;
    private final StaffTeachingQualificationMapper teachingQualificationMapper;

    @Override
    public Mono<StaffTeachingQualificationResponse> addStaffTeachingQualification(
            String staffCode, String schoolCode, AddStaffTeachingQualificationRequest request, String requestId) {
        return staffRepository
                .findByStaffCodeAndSchoolCodeAndIsDeletedFalse(staffCode, schoolCode)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Staff not found with code " + staffCode)))
                .flatMap(
                        staff ->
                                staffTeachingQualificationRepository
                                        .findByStaffIdAndTeachingQualificationAndSubjectOfQualificationAndIsDeletedFalse(
                                                staff.getStaffId(),
                                                request.teachingQualification().name(),
                                                request.subjectOfQualification().name())
                                        .map(StaffTeachingQualificationResponse::from)
                                        .switchIfEmpty(Mono.defer(() -> addNewTeachingQualification(teachingQualificationMapper.toEntity(request), staff)))
                )
                .as(transactionalOperator::transactional)
                .doOnSuccess(response -> log.info("[{}] Staff teaching qualification added successfully: {}", requestId, response))
                .doOnError(ex -> log.error("Error adding staff qualification: {}", ex.getMessage()))
                .onErrorMap(
                        ex -> {
                            log.error("Error adding staff teaching qualifications: {}", ex.getMessage());
                            return new ResourceCreationException("Error adding staff teaching qualifications", ex);
                        });
    }

    @Override
    public Mono<Page<StaffTeachingQualificationResponse>> retrieveStaffTeachingQualifications(
            String staffCode, String schoolCode, Pageable pageable, String requestId) {

        int size = pageable.getPageSize();
        long offset = pageable.getOffset();

        return staffRepository
                .findByStaffCodeAndSchoolCodeAndIsDeletedFalse(staffCode, schoolCode)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Staff not found with code " + staffCode)))
                .flatMap(staff -> Mono.zip(staffTeachingQualificationRepository
                                .findByStaffIdAndIsDeletedFalse(staff.getStaffId(), size, offset).collectList(),
                        staffTeachingQualificationRepository
                                .countByStaffIdAndIsDeletedFalse(staff.getStaffId())))
                .timeout(Duration.ofSeconds(properties.getTimeout()))
                .map(tuple -> {
                    List<StaffTeachingQualification> qualifications = tuple.getT1();
                    long total = tuple.getT2();
                    List<StaffTeachingQualificationResponse> responses =
                            total == 0
                                    ? List.of()
                                    : qualifications.stream()
                                    .map(StaffTeachingQualificationResponse::from)
                                    .toList();
                    return (Page<StaffTeachingQualificationResponse>)
                            new PageImpl<>(responses, pageable, total);
                })
                .onErrorMap(ex -> {
                    log.error("[{}] Error retrieving staff teaching qualifications with code {}", requestId, schoolCode, ex);
                    if (ex instanceof TimeoutException) {
                        return new ResourceTimeoutException("DB timeout :::", ex);
                    }
                    return new HrServiceException(
                            "Error fetching staff teaching qualifications", ex);
                });
    }

    private Mono<StaffTeachingQualificationResponse> addNewTeachingQualification(
            StaffTeachingQualification qualification, Staff staff) {
        qualification.setStaffId(staff.getStaffId());
        return staffTeachingQualificationRepository.save(qualification)
                .map(StaffTeachingQualificationResponse::from);
    }
}
