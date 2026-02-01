package com.emis.hrservice.controller;

import com.emis.hrservice.dto.request.StaffTransferRequest;
import com.emis.hrservice.dto.response.StaffTransferResponse;
import com.emis.hrservice.service.StaffTransferService;
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
public class StaffTransferController {

    private final StaffTransferService staffTransferService;

    @Operation(summary = "Transfer a staff member to a new school")
    @PostMapping("/staff/{staffCode}/transfer")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<StaffTransferResponse> transferStaff(
            @PathVariable String staffCode,
            @RequestBody StaffTransferRequest request) {

        String requestId = UUID.randomUUID().toString();
        log.info(
                "[{}] Transferring staff {} from {} to {}",
                requestId, staffCode, request.fromSchoolCode(), request.toSchoolCode()
        );
        return staffTransferService.transferStaff(staffCode, request, requestId)
                .contextWrite(ctx -> ctx.put("requestId", requestId));
    }

    @Operation(summary = "Get staff service history")
    @GetMapping("/staff/{staffId}/service-history")
    public Flux<StaffTransferResponse> getStaffServiceHistory(
            @PathVariable Long staffId) {

        String requestId = UUID.randomUUID().toString();
        log.info(
                "[{}] Getting staff service history for staff {}",
                requestId, staffId
        );
        return staffTransferService.getStaffServiceHistory(staffId)
                .contextWrite(ctx -> ctx.put("requestId", requestId));
    }
}
