package com.emis.hrservice.controller;

import com.emis.hrservice.dto.request.*;
import com.emis.hrservice.dto.response.AddTextbookResponse;
import com.emis.hrservice.dto.response.TextbookInventoryResponse;
import com.emis.hrservice.service.TextbookManagementService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/hr")
@Validated
public class TextbookManagementController {

    private final TextbookManagementService textbookManagementService;

    @Operation(summary = "Add a new textbook to the inventory")
    @PostMapping("/schools/{schoolCode}/textbooks")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<AddTextbookResponse> addTextbookToInventory(@RequestBody @Valid AddTextbookRequest request,
                                                            @PathVariable String schoolCode) {
        String requestId = UUID.randomUUID().toString();
        return textbookManagementService.addTextbookToInventory(request, schoolCode, requestId)
                .contextWrite(ctx -> ctx.put("requestId", requestId));
    }

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

    @Operation(summary = "textbook received by a school")
    @PostMapping("/textbooks/{textbookId}/received")
    @ResponseStatus(HttpStatus.OK)
    public Mono<TextbookInventoryResponse> receiveTextbook(@PathVariable Long textbookId,
                                      @RequestBody @Valid ReceiveTextbookRequest request) {
        String requestId = UUID.randomUUID().toString();
        return textbookManagementService.receiveTextbooks(textbookId, request, requestId)
                .contextWrite(ctx -> ctx.put("requestId", requestId));
    }

    @Operation(summary = "issue textbooks to someone")
    @PostMapping("/textbooks/{textbookId}/issue")
    @ResponseStatus(HttpStatus.OK)
    public Mono<TextbookInventoryResponse> issueTextbooks(@PathVariable Long textbookId,
                                      @RequestBody @Valid IssueTextbookRequest request) {
        String requestId = UUID.randomUUID().toString();
        return textbookManagementService.issueTextbooks(textbookId, request)
                .contextWrite(ctx -> ctx.put("requestId", requestId));
    }

    @Operation(summary = "return textbooks to a school")
    @PostMapping("/textbooks/{textbookId}/return")
    @ResponseStatus(HttpStatus.OK)
    public Mono<TextbookInventoryResponse> returnTextbooks(@PathVariable Long textbookId,
                                      @RequestBody @Valid ReturnTextbookRequest request) {
        String requestId = UUID.randomUUID().toString();
        return textbookManagementService.returnTextbooks(textbookId, request)
                .contextWrite(ctx -> ctx.put("requestId", requestId));
    }

    @Operation(summary = "record damaged textbooks")
    @PostMapping("/textbooks/{textbookId}/damage")
    @ResponseStatus(HttpStatus.OK)
    public Mono<TextbookInventoryResponse> markDamagedTextbooks(@PathVariable Long textbookId,
                                      @RequestBody @Valid DamageTextbookRequest request) {
        String requestId = UUID.randomUUID().toString();
        return textbookManagementService.markDamaged(textbookId, request)
                .contextWrite(ctx -> ctx.put("requestId", requestId));
    }
}
