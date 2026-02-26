package com.emis.hrservice.controller;

import com.emis.hrservice.dto.request.AuditContext;
import com.emis.hrservice.dto.request.BulkAttendanceConfirmationRequest;
import com.emis.hrservice.dto.request.StaffAttendanceRequest;
import com.emis.hrservice.dto.request.StaffCheckInRequest;
import com.emis.hrservice.dto.response.BulkAttendanceConfirmationResponse;
import com.emis.hrservice.dto.response.StaffAttendanceResponse;
import com.emis.hrservice.dto.response.StaffCheckInResponse;
import com.emis.hrservice.service.StaffAttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
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
public class StaffAttendanceController {

    private final StaffAttendanceService staffAttendanceService;
    @Operation(summary = "Check in a staff member")
    @PostMapping("/attendance/check-in")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<StaffCheckInResponse> checkInStaff(
            @RequestBody @Valid StaffCheckInRequest request
    ) {
        String requestId = UUID.randomUUID().toString();
        return staffAttendanceService.checkInStaff(request, requestId)
                .contextWrite(ctx -> ctx.put("audit", new AuditContext(1L, "Staff", "WEB")));
    }

    @Operation(summary = "Mark a staff member attendance")
    @PostMapping("/schools/{schoolCode}/attendance/confirm")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<StaffAttendanceResponse> confirmAttendance(
            @PathVariable String schoolCode,
            @RequestBody StaffAttendanceRequest request
    ) {
        String requestId = UUID.randomUUID().toString();
        return staffAttendanceService.markStaffAttendance(request, schoolCode, requestId)
                .contextWrite(ctx -> ctx.put("audit", new AuditContext(1L, "Head_Teacher", "WEB")));
    }

    @Operation(summary = "Bulk confirmation of staff attendance at once")
    @PostMapping("/schools/{schoolCode}/attendance/confirm/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<BulkAttendanceConfirmationResponse> confirmAttendanceBulk(
            @PathVariable String schoolCode,
            @RequestBody BulkAttendanceConfirmationRequest request
    ) {
        String requestId = UUID.randomUUID().toString();
        return staffAttendanceService.confirmBulkAttendance(request, schoolCode, requestId)
                .contextWrite(ctx -> ctx.put("audit", new AuditContext(1L, "Head_Teacher", "WEB")));
    }
}
