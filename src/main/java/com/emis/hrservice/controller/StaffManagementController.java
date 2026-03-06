package com.emis.hrservice.controller;

import com.emis.hrservice.dto.request.CreateStaffRequest;
import com.emis.hrservice.dto.request.UpdateEmergencyContactRequest;
import com.emis.hrservice.dto.request.UpdateStaffBioRequest;
import com.emis.hrservice.dto.response.CreateStaffResponse;
import com.emis.hrservice.dto.response.EmergencyContactResponse;
import com.emis.hrservice.dto.response.UpdateStaffBioResponse;
import com.emis.hrservice.security.CanAccessRestrictedResource;
import com.emis.hrservice.security.CanAccessStrictlyRestrictedResource;
import com.emis.hrservice.service.StaffManagementService;
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
public class StaffManagementController {

    private final StaffManagementService staffManagementService;

    @CanAccessRestrictedResource
    @Operation(summary = "Create a new staff member for a school")
    @PostMapping("/staff")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CreateStaffResponse> createStaff(@RequestBody @Valid CreateStaffRequest request) {
        String requestId = UUID.randomUUID().toString();
        return staffManagementService.createStaff(request, requestId)
                .contextWrite(ctx -> ctx.put("requestId", requestId));
    }

    @CanAccessRestrictedResource
    @Operation(summary = "Retrieve a staff member from a school with school code")
    @GetMapping("/schools/{schoolCode}/staff/{staffCode}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<CreateStaffResponse> retrieveStaff(@PathVariable String schoolCode,
                                                   @PathVariable String staffCode) {
        String requestId = UUID.randomUUID().toString();
        return staffManagementService.retrieveStaff(schoolCode, staffCode, requestId)
                .contextWrite(ctx -> ctx.put("requestId", requestId));
    }

    @CanAccessRestrictedResource
    @Operation(summary = "Retrieve a staff member from a school with staff id")
    @GetMapping("/staff/{staffId}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<CreateStaffResponse> retrieveStaff(
            @RequestHeader String schoolCode,
            @PathVariable Long staffId) {
        log.info("Retrieving staff member with id: {} and schoolCode {} ", staffId, schoolCode);
        String requestId = UUID.randomUUID().toString();
        return staffManagementService.retrieveStaffById(staffId, requestId)
                .contextWrite(ctx -> ctx.put("requestId", requestId));
    }

    @CanAccessRestrictedResource
    @Operation(summary = "Retrieve staff members from a school with school code")
    @GetMapping("/schools/{schoolCode}/staff")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Page<CreateStaffResponse>> retrieveStaffs(@PathVariable String schoolCode,
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
        return staffManagementService.retrieveStaffsBySchoolCode(schoolCode, pageable, requestId)
                .contextWrite(ctx -> ctx.put("requestId", requestId));
    }

    @CanAccessRestrictedResource
    @Operation(summary = "Update staff bio")
    @PutMapping("/staff/{staffCode}/bio")
    @ResponseStatus(HttpStatus.OK)
    public Mono<UpdateStaffBioResponse> updateStaffBio(
            @RequestHeader(required = false) String schoolCode,
            @PathVariable String staffCode,
            @RequestBody UpdateStaffBioRequest request) {

        log.info("Updating staff bio for staff with code: {} and schoolCode {} ", staffCode, schoolCode);
        String requestId = UUID.randomUUID().toString();
        return staffManagementService.
                updateStaffBio(staffCode, request, requestId)
                .contextWrite(ctx -> ctx.put("requestId", requestId));
    }

    @CanAccessRestrictedResource
    @Operation(summary = "Update emergency contact of a staff with staff code")
    @PutMapping("/staff/{staffCode}/emergency-contact")
    @ResponseStatus(HttpStatus.OK)
    public Mono<EmergencyContactResponse> updateStaffEmergencyContact(
            @RequestHeader(required = false) String schoolCode,
            @PathVariable String staffCode,
            @RequestBody UpdateEmergencyContactRequest request) {

        log.info("Updating emergency contact for staff with code: {} and schoolCode {} ", staffCode, schoolCode);
        String requestId = UUID.randomUUID().toString();
        return staffManagementService.
                updateStaffEmergencyContact(staffCode, request, requestId)
                .contextWrite(ctx -> ctx.put("requestId", requestId));
    }


}
