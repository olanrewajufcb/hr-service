package com.emis.hrservice.controller;

import com.emis.hrservice.dto.request.AddStaffTeachingQualificationRequest;
import com.emis.hrservice.dto.response.StaffTeachingQualificationResponse;
import com.emis.hrservice.security.CanAccessRestrictedResource;
import com.emis.hrservice.service.StaffTeachingQualificationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
public class StaffTeachingQualificationController {

    private final StaffTeachingQualificationService teachingQualificationService;

    @CanAccessRestrictedResource
    @Operation(summary = "Add a staff teaching qualifications to the system")
    @PostMapping("/schools/{schoolCode}/staff/{staffCode}/teachingc-qualifications")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<StaffTeachingQualificationResponse> addStaffTeachingQualification(
            @PathVariable String schoolCode,
            @PathVariable String staffCode,
            @RequestBody @Valid AddStaffTeachingQualificationRequest request) {

        String requestId = UUID.randomUUID().toString();
        return teachingQualificationService.addStaffTeachingQualification(
                staffCode, schoolCode, request, requestId)
                .contextWrite(ctx -> ctx.put("requestId", requestId));

    }

    @CanAccessRestrictedResource
    @Operation(summary = "Retrieve a staff teaching qualifications from the system")
    @GetMapping("/schools/{schoolCode}/staff/{staffCode}/teaching-qualifications")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Page<StaffTeachingQualificationResponse>> retrieveStaffTeachingQualifications(
            @PathVariable String schoolCode,
            @PathVariable String staffCode,
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page number must be greater than or equal to 0")
            int page,
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "Page size must be greater than or equal to 1")
            int size,
            @RequestParam(defaultValue = "schoolId")
            String sortBy) {

        String requestId = UUID.randomUUID().toString();
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return teachingQualificationService.retrieveStaffTeachingQualifications(
                staffCode, schoolCode,pageable, requestId)
                .contextWrite(ctx -> ctx.put("requestId", requestId));

    }

}
