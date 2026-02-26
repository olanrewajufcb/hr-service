package com.emis.hrservice.service;

import com.emis.hrservice.dto.request.AttendancePolicyRequest;
import com.emis.hrservice.dto.response.PolicyResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SchoolAttendancePolicyService {

    Mono<PolicyResponse> createPolicy(String schoolCode, AttendancePolicyRequest request, String requestId);

    Flux<PolicyResponse> getAllSchoolPolicy(String schoolCode, String requestId);
}
