package com.emis.hrservice.controller;

import com.emis.hrservice.dto.request.AttendancePolicyRequest;
import com.emis.hrservice.dto.response.PolicyResponse;
import com.emis.hrservice.security.CanAccessRestrictedResource;
import com.emis.hrservice.security.CanCreateResource;
import com.emis.hrservice.service.SchoolAttendancePolicyService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
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
public class SchoolAttendancePolicyController {

    private final SchoolAttendancePolicyService policyService;

// TODO: I need to change the unique index for staff assignment
    @CanCreateResource
    @Operation(summary = "Create school attendance policy")
    @PostMapping("/schools/{schoolCode}/attendance-policy")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<PolicyResponse> createAttendancePolicy(
                            @PathVariable String schoolCode,
                            @RequestBody @Valid AttendancePolicyRequest request){

        String requestId = UUID.randomUUID().toString();
        return policyService.createPolicy(schoolCode, request, requestId)
                .contextWrite(ctx -> ctx.put("requestId", requestId));

    }

    @CanAccessRestrictedResource
    @Operation(summary = "Retrieve all the school attendance policy")
    @GetMapping("/schools/{schoolCode}/attendance-policies")
    public Flux<PolicyResponse> getAllSchoolPolicy(
            @PathVariable String schoolCode){

        String requestId = UUID.randomUUID().toString();
        return policyService.getAllSchoolPolicy(schoolCode, requestId)
                .contextWrite(ctx -> ctx.put("requestId", requestId));

    }
}
