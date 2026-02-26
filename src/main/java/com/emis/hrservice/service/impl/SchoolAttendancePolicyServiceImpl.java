package com.emis.hrservice.service.impl;

import com.emis.hrservice.domain.db.AttendancePolicy;
import com.emis.hrservice.dto.request.AttendancePolicyRequest;
import com.emis.hrservice.dto.response.PolicyResponse;
import com.emis.hrservice.exceptions.ResourceAlreadyExistsException;
import com.emis.hrservice.exceptions.ResourceNotFoundException;
import com.emis.hrservice.repository.SchoolAttendancePolicyRepository;
import com.emis.hrservice.service.SchoolAttendancePolicyService;
import com.emis.hrservice.service.cache.SchoolCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class SchoolAttendancePolicyServiceImpl implements SchoolAttendancePolicyService {

    private final SchoolAttendancePolicyRepository policyRepository;
    private final SchoolCacheService schoolCacheService;

    @Override
    public Mono<PolicyResponse> createPolicy(String schoolCode, AttendancePolicyRequest request, String requestId) {
    return schoolCacheService
        .getSchoolDetails(schoolCode)
        .flatMap(
            school -> {
              AttendancePolicy policy =
                  AttendancePolicy.builder()
                      .checkInTime(request.checkInTime())
                      .cutOffTime(request.cutOffTime())
                      .schoolCode(schoolCode)
                      .schoolId(school.schoolId())
                      .effectiveFrom(request.effectiveFrom())
                      .effectiveTo(request.effectiveTo())
                      .build();

              return policyRepository
                  .save(policy)
                  .map(savedPolicy -> PolicyResponse.from(savedPolicy, school.schoolName()));
            })
        .onErrorMap(
            DuplicateKeyException.class,
            ex -> new ResourceAlreadyExistsException("Policy already exists in the DB"));
    }

    @Override
    public Flux<PolicyResponse> getAllSchoolPolicy(String schoolCode, String requestId) {
        return policyRepository.findBySchoolCodeAndStatus(schoolCode)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException
                        (String.format("No attendance policy found for the given school code: %s",schoolCode ))))
                .map(PolicyResponse::from);

    }
}
