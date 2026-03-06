package com.emis.hrservice.controller;


import com.emis.hrservice.dto.request.CreateStaffAssignmentRequest;
import com.emis.hrservice.dto.response.StaffAssignmentResponse;
import com.emis.hrservice.security.CanAccessRestrictedResource;
import com.emis.hrservice.security.CanViewResource;
import com.emis.hrservice.service.StaffAssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/hr")
@Validated
@RequiredArgsConstructor
public class StaffAssignmentController {

    private final StaffAssignmentService staffAssignmentService;

    @CanAccessRestrictedResource
    @Operation(summary = "Assign a staff member to a school class/subject")
    @PostMapping("/school/{schoolCode}/staff/{staffCode}/assignments")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<StaffAssignmentResponse> assignStaffToClass(
            @PathVariable String schoolCode,
            @RequestBody CreateStaffAssignmentRequest request,
            @PathVariable String staffCode
    ) {
        String requestId = UUID.randomUUID().toString();
        return staffAssignmentService.assignStaffToClass(request, staffCode, schoolCode, requestId);
    }

    @CanViewResource
    @Operation(summary = "view a staff member assignments")
    @GetMapping("/schools/{schoolCode}/staff/{staffCode}/assignments")
    @ResponseStatus(HttpStatus.OK)
    public Flux<StaffAssignmentResponse> viewStaffAssignments(
            @PathVariable String schoolCode,
            @PathVariable String staffCode
    ) {
        return staffAssignmentService.viewStaffAssignments(staffCode, schoolCode);
    }

    @CanViewResource
    @Operation(summary = "view a staff member assignment by staff id")
    @GetMapping("/assignments/{assignmentId}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<StaffAssignmentResponse> viewStaffAssignmentById(
             @RequestHeader(required = false) String schoolCode,
             @PathVariable Long assignmentId
    ) {
        return staffAssignmentService.viewStaffAssignmentById(assignmentId);
    }
}
