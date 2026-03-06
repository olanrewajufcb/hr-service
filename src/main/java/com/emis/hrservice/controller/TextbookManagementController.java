package com.emis.hrservice.controller;

import com.emis.hrservice.dto.request.*;
import com.emis.hrservice.dto.response.AddTextbookResponse;
import com.emis.hrservice.dto.response.TextbookInventoryResponse;
import com.emis.hrservice.security.CanAccessRestrictedResource;
import com.emis.hrservice.security.CanCreateResource;
import com.emis.hrservice.security.CanViewResource;
import com.emis.hrservice.service.TextbookManagementService;
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
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/hr")
@Validated
public class TextbookManagementController {

    private final TextbookManagementService textbookManagementService;

    @CanCreateResource
    @Operation(summary = "Add a new textbook to the inventory")
    @PostMapping("/schools/{schoolCode}/textbooks")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<AddTextbookResponse> addTextbookToInventory(
            @PathVariable String schoolCode,
            @RequestBody @Valid AddTextbookRequest request
    ) {
        String requestId = UUID.randomUUID().toString();
        return textbookManagementService.addTextbookToInventory(request, schoolCode, requestId)
                .contextWrite(ctx -> ctx.put("requestId", requestId));
    }

    @CanViewResource
    @Operation(summary = "retrieve all textbooks from the inventory")
    @GetMapping("/schools/{schoolCode}/textbooks")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Page<TextbookInventoryResponse>> retrieveTextbooksFromInventory(
            @PathVariable String schoolCode,
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
        return textbookManagementService.retrieveTextbooksFromInventory(schoolCode, pageable, requestId)
                .contextWrite(ctx -> ctx.put("requestId", requestId));
    }

    @CanCreateResource
    @Operation(summary = "textbook received by a school")
    @PostMapping("/textbooks/{textbookId}/received")
    @ResponseStatus(HttpStatus.OK)
    public Mono<TextbookInventoryResponse> receiveTextbook(
            @RequestHeader(required = false) String schoolCode,
            @PathVariable Long textbookId,
            @RequestBody @Valid ReceiveTextbookRequest request) {

        log.info("Receiving textbook with id {} and  {}", textbookId, schoolCode);
        String requestId = UUID.randomUUID().toString();
        return textbookManagementService.receiveTextbooks(textbookId, request, requestId)
                .contextWrite(ctx -> ctx.put("requestId", requestId));
    }

    @CanCreateResource
    @Operation(summary = "issue textbooks to someone")
    @PostMapping("/textbooks/{textbookId}/issue")
    @ResponseStatus(HttpStatus.OK)
    public Mono<TextbookInventoryResponse> issueTextbooks(
            @RequestHeader(required = false) String schoolCode,
            @PathVariable Long textbookId,
            @RequestBody @Valid IssueTextbookRequest request) {

        log.info("Issuing textbook with id {} and  {}", textbookId, schoolCode);
        String requestId = UUID.randomUUID().toString();
        return textbookManagementService.issueTextbooks(textbookId, request)
                .contextWrite(ctx -> ctx.put("requestId", requestId));
    }

    @CanViewResource
    @Operation(summary = "return textbooks to a school")
    @PostMapping("/textbooks/{textbookId}/return")
    @ResponseStatus(HttpStatus.OK)
    public Mono<TextbookInventoryResponse> returnTextbooks(
            @RequestHeader(required = false) String schoolCode,
            @PathVariable Long textbookId,
            @RequestBody @Valid ReturnTextbookRequest request) {

        log.info("Returning textbook with id {} and  {}", textbookId, schoolCode);
        String requestId = UUID.randomUUID().toString();
        return textbookManagementService.returnTextbooks(textbookId, request)
                .contextWrite(ctx -> ctx.put("requestId", requestId));
    }

    @CanCreateResource
    @Operation(summary = "record damaged textbooks")
    @PostMapping("/textbooks/{textbookId}/damage")
    @ResponseStatus(HttpStatus.OK)
    public Mono<TextbookInventoryResponse> markDamagedTextbooks(
            @RequestHeader(required = false) String schoolCode,
            @PathVariable Long textbookId,
            @RequestBody @Valid DamageTextbookRequest request) {

        log.info("Marking textbook with id {} as damaged and  {}", textbookId, schoolCode);
        String requestId = UUID.randomUUID().toString();
        return textbookManagementService.markDamaged(textbookId, request)
                .contextWrite(ctx -> ctx.put("requestId", requestId));
    }
}
