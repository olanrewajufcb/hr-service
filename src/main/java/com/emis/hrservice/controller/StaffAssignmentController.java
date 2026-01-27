package com.emis.hrservice.controller;


import com.emis.hrservice.dto.request.CreateStaffAssignmentRequest;
import com.emis.hrservice.dto.response.StaffAssignmentResponse;
import com.emis.hrservice.service.StaffAssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/hr")
@Validated
@RequiredArgsConstructor
public class StaffAssignmentController {

    private final StaffAssignmentService staffAssignmentService;

    @Operation(summary = "Assign a staff member to a school class/subject")
    @PostMapping("/school/{schoolCode}/staff/{staffCode}/assignments")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<StaffAssignmentResponse> assignStaffToClass(
            @RequestBody CreateStaffAssignmentRequest request,
            @PathVariable String staffCode, @PathVariable String schoolCode
    ) {
        String requestId = UUID.randomUUID().toString();
        return staffAssignmentService.assignStaffToClass(request, staffCode, schoolCode, requestId);
    }
}
