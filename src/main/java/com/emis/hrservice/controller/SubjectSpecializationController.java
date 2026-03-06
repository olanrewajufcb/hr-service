package com.emis.hrservice.controller;

import com.emis.hrservice.dto.request.SubjectSpecializationRequest;
import com.emis.hrservice.dto.response.SubjectSpecializationResponse;
import com.emis.hrservice.security.CanCreateResource;
import com.emis.hrservice.service.StaffSubjectSpecializationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hr")
@RequiredArgsConstructor
@Slf4j
public class SubjectSpecializationController {

    private final StaffSubjectSpecializationService subjectSpecializationService;

    @CanCreateResource
    @Operation(summary = "Add subject specialization for a staff")
    @PostMapping("/schools/{schoolCode}/staff/{staffCode}/subject-specialization")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<SubjectSpecializationResponse> addSubjectSpecialization(
            @PathVariable String schoolCode,
            @PathVariable String staffCode,
            @RequestBody @Valid SubjectSpecializationRequest request) {

        String requestId = UUID.randomUUID().toString();
        return subjectSpecializationService.addSubjectSpecialization(
                        staffCode, schoolCode, request, requestId)
                .contextWrite(ctx -> ctx.put("requestId", requestId));

    }
}
