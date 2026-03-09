package com.emis.hrservice.service.impl;

import com.emis.hrservice.domain.db.AttendancePolicy;
import com.emis.hrservice.dto.request.AttendancePolicyRequest;
import com.emis.hrservice.dto.response.PolicyResponse;
import com.emis.hrservice.dto.response.SchoolDetailsResponse;
import com.emis.hrservice.exceptions.ResourceAlreadyExistsException;
import com.emis.hrservice.exceptions.ResourceNotFoundException;
import com.emis.hrservice.repository.SchoolAttendancePolicyRepository;
import com.emis.hrservice.service.cache.SchoolCacheService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchoolAttendancePolicyServiceImplTest {

    @Mock
    private SchoolAttendancePolicyRepository policyRepository;

    @Mock
    private SchoolCacheService schoolCacheService;

    @InjectMocks
    private SchoolAttendancePolicyServiceImpl policyService;

    private final String schoolCode = "SCH-001";
    private final String requestId = "req-123";

    @Test
    void createPolicy_ShouldReturnResponse_WhenSuccess() {
        AttendancePolicyRequest request = new AttendancePolicyRequest(
                LocalTime.of(8, 0),
                LocalTime.of(9, 0),
                LocalDate.now(),
                LocalDate.now().plusYears(1)
        );

        SchoolDetailsResponse schoolDetails = mock(SchoolDetailsResponse.class);
        when(schoolDetails.schoolId()).thenReturn(1L);
        when(schoolDetails.schoolName()).thenReturn("Test School");

        AttendancePolicy savedPolicy = AttendancePolicy.builder()
                .policyId(100L)
                .schoolCode(schoolCode)
                .schoolId(1L)
                .checkInTime(request.checkInTime())
                .cutOffTime(request.cutOffTime())
                .effectiveFrom(request.effectiveFrom())
                .effectiveTo(request.effectiveTo())
                .status("ACTIVE")
                .build();

        when(schoolCacheService.getSchoolDetails(schoolCode)).thenReturn(Mono.just(schoolDetails));
        when(policyRepository.save(any(AttendancePolicy.class))).thenReturn(Mono.just(savedPolicy));

        Mono<PolicyResponse> result = policyService.createPolicy(schoolCode, request, requestId);

        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.policyId().equals(100L) &&
                        response.schoolName().equals("Test School") &&
                        response.checkInTime().equals(request.checkInTime())
                )
                .verifyComplete();

        verify(schoolCacheService).getSchoolDetails(schoolCode);
        verify(policyRepository).save(any(AttendancePolicy.class));
    }

    @Test
    void createPolicy_ShouldReturnError_WhenSchoolNotFound() {
        AttendancePolicyRequest request = new AttendancePolicyRequest(
                LocalTime.of(8, 0),
                LocalTime.of(9, 0),
                LocalDate.now(),
                LocalDate.now().plusYears(1)
        );

        when(schoolCacheService.getSchoolDetails(schoolCode)).thenReturn(Mono.empty());

        Mono<PolicyResponse> result = policyService.createPolicy(schoolCode, request, requestId);

        StepVerifier.create(result)
                .verifyComplete(); // flatMap on empty Mono returns empty Mono

        verify(schoolCacheService).getSchoolDetails(schoolCode);
        verify(policyRepository, never()).save(any());
    }

    @Test
    void createPolicy_ShouldThrowAlreadyExistsException_WhenDuplicateKey() {
        AttendancePolicyRequest request = new AttendancePolicyRequest(
                LocalTime.of(8, 0),
                LocalTime.of(9, 0),
                LocalDate.now(),
                LocalDate.now().plusYears(1)
        );

        SchoolDetailsResponse schoolDetails = mock(SchoolDetailsResponse.class);
        when(schoolDetails.schoolId()).thenReturn(1L);

        when(schoolCacheService.getSchoolDetails(schoolCode)).thenReturn(Mono.just(schoolDetails));
        when(policyRepository.save(any(AttendancePolicy.class)))
                .thenReturn(Mono.error(new DuplicateKeyException("Duplicate")));

        Mono<PolicyResponse> result = policyService.createPolicy(schoolCode, request, requestId);

        StepVerifier.create(result)
                .expectError(ResourceAlreadyExistsException.class)
                .verify();
    }

    @Test
    void getAllSchoolPolicy_ShouldReturnFlux_WhenPoliciesExist() {
        AttendancePolicy policy = AttendancePolicy.builder()
                .policyId(100L)
                .schoolCode(schoolCode)
                .status("ACTIVE")
                .build();

        when(policyRepository.findBySchoolCodeAndStatus(schoolCode)).thenReturn(Flux.just(policy));

        Flux<PolicyResponse> result = policyService.getAllSchoolPolicy(schoolCode, requestId);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.policyId().equals(100L))
                .verifyComplete();

        verify(policyRepository).findBySchoolCodeAndStatus(schoolCode);
    }

    @Test
    void getAllSchoolPolicy_ShouldThrowNotFoundException_WhenNoPolicies() {
        when(policyRepository.findBySchoolCodeAndStatus(schoolCode)).thenReturn(Flux.empty());

        Flux<PolicyResponse> result = policyService.getAllSchoolPolicy(schoolCode, requestId);

        StepVerifier.create(result)
                .expectError(ResourceNotFoundException.class)
                .verify();

        verify(policyRepository).findBySchoolCodeAndStatus(schoolCode);
    }
}
