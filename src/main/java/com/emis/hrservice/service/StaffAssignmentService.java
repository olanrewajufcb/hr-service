package com.emis.hrservice.service;

import com.emis.hrservice.dto.request.CreateStaffAssignmentRequest;
import com.emis.hrservice.dto.response.StaffAssignmentResponse;
import reactor.core.publisher.Mono;

public interface StaffAssignmentService {

    Mono<StaffAssignmentResponse> assignStaffToClass(
            CreateStaffAssignmentRequest request, String staffCode, String schoolCode, String requestId);
}
