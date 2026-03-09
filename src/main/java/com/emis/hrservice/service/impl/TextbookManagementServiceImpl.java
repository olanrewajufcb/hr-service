package com.emis.hrservice.service.impl;

import com.emis.hrservice.config.ServiceConfigurationProperties;
import com.emis.hrservice.domain.db.TextbookInventory;
import com.emis.hrservice.domain.db.TextbookInventoryTransaction;
import com.emis.hrservice.dto.request.*;
import com.emis.hrservice.dto.response.AddTextbookResponse;
import com.emis.hrservice.dto.response.SchoolDetailsResponse;
import com.emis.hrservice.dto.response.TextbookInventoryResponse;
import com.emis.hrservice.exceptions.*;
import com.emis.hrservice.repository.TextbookInventoryRepository;
import com.emis.hrservice.repository.TextbookTransactionRepository;
import com.emis.hrservice.service.TextbookManagementService;
import com.emis.hrservice.service.cache.SchoolCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
@Slf4j
public class TextbookManagementServiceImpl implements TextbookManagementService {

    private final TextbookInventoryRepository textbookInventoryRepository;
    private final TransactionalOperator transactionalOperator;
    private final SchoolCacheService schoolCacheService;
    private final ServiceConfigurationProperties properties;
    private final TextbookTransactionRepository textbookTransactionRepository;
    @Override
    public Mono<AddTextbookResponse> addTextbookToInventory(AddTextbookRequest request, String schoolCode, String requestId) {
        return schoolCacheService
                .getSchoolDetails(schoolCode)
                .flatMap(
                        school -> createTextbookInventory(request, school)
                                .onErrorResume(DuplicateKeyException.class,
                                        ex -> textbookInventoryRepository
                                        .findBySchoolIdAndTitleAndSubjectAreaAndEditionAndGradeLevelAndIsDeletedFalse(
                                                school.schoolId(),
                                                request.title(),
                                                request.subjectArea().name(),
                                                request.edition(),
                                                request.gradeLevel().name())
                                                .switchIfEmpty(Mono.error(new ResourceAlreadyExistsException
                                                        ("Duplicate textbook detected but not found")))
                                                .map(AddTextbookResponse::from))

                );

    }

    @Override
    public Mono<Page<TextbookInventoryResponse>> retrieveTextbooksFromInventory(String schoolCode, Pageable pageable, String requestId) {
        int size = pageable.getPageSize();
        long offset = pageable.getOffset();
    return schoolCacheService
        .getSchoolDetails(schoolCode)
        .flatMap(
            school ->
                Mono.zip(
                        textbookInventoryRepository
                            .findBySchoolIdAndIsDeletedFalse(school.schoolId(), size, offset)
                            .collectList(),
                        textbookInventoryRepository.countBySchoolIdAndIsDeletedFalse(
                            school.schoolId()))
                    .timeout(Duration.ofSeconds(properties.getTimeout()))
                    .map(
                        tuple -> {
                          List<TextbookInventory> textbooks = tuple.getT1();
                          long total = tuple.getT2();
                          List<TextbookInventoryResponse> textbookResponses =
                              total == 0
                                  ? List.of()
                                  : textbooks.stream()
                                      .map(TextbookInventoryResponse::from)
                                      .toList();
                          return (Page<TextbookInventoryResponse>)
                              new PageImpl<>(textbookResponses, pageable, total);
                        }))
        .onErrorMap(
            ex -> {
              log.error("[{}] Error retrieving textbooks with code {}", requestId, schoolCode, ex);
              if (ex instanceof TimeoutException) {
                return new ResourceTimeoutException("DB timeout :::", ex);
              }
              return new HrServiceException("Error fetching textbooks ", ex);
            });
    }

    public Mono<TextbookInventoryResponse> receiveTextbooks(
            Long textbookId,
            ReceiveTextbookRequest request, String requestId
    ) {
        log.info("[{}] Receiving textbook with id {}", requestId, textbookId);
        return textbookInventoryRepository
                .findById(textbookId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Textbook not found")))
                .flatMap(inv -> {

                    inv.setTotalQuantity(inv.getTotalQuantity() + request.quantity());
                    inv.setAvailableQuantity(inv.getAvailableQuantity() + request.quantity());

                    TextbookInventoryTransaction tx =
                            TextbookInventoryTransaction.builder()
                                    .textbookId(inv.getTextbookId())
                                    .transactionType("RECEIVE")
                                    .quantity(request.quantity())
                                    .reference(request.reference())
                                    .notes(request.notes())
                                    .performedBy(request.receivedBy())
                                    .build();

                    return textbookInventoryRepository.save(inv)
                            .then(textbookTransactionRepository.save(tx))
                            .thenReturn(inv);
                })
                .as(transactionalOperator::transactional)
                .map(TextbookInventoryResponse::from);
    }


    public Mono<TextbookInventoryResponse> issueTextbooks(
            Long textbookId,
            IssueTextbookRequest request
    ) {
        return textbookInventoryRepository
                .findById(textbookId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Textbook not found")))
                .flatMap(inv -> {

                    if (inv.getAvailableQuantity() < request.quantity()) {
                        return Mono.error(new ValidationException("Insufficient stock"));
                    }

                    inv.setAvailableQuantity(inv.getAvailableQuantity() - request.quantity());
                    inv.setIssuedQuantity(inv.getIssuedQuantity() + request.quantity());

                    TextbookInventoryTransaction tx =
                            TextbookInventoryTransaction.builder()
                                    .textbookId(inv.getTextbookId())
                                    .transactionType("ISSUE")
                                    .quantity(request.quantity())
                                    .issuedToType(request.issuedToType().name())
                                    .issuedToCode(request.issuedToCode())
                                    .issuedToName(request.issuedToName())
                                    .performedBy(request.issuedBy())
                                    .build();

                    return textbookInventoryRepository.save(inv)
                            .then(textbookTransactionRepository.save(tx))
                            .thenReturn(inv);
                })
                .as(transactionalOperator::transactional)
                .map(TextbookInventoryResponse::from);
    }

    public Mono<TextbookInventoryResponse> returnTextbooks(
            Long textbookId,
            ReturnTextbookRequest request
    ) {
        return textbookInventoryRepository.findById(textbookId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Textbook not found")))
                .flatMap(inv -> {

                    inv.setIssuedQuantity(inv.getIssuedQuantity() - request.quantity());
                    inv.setAvailableQuantity(inv.getAvailableQuantity() + request.quantity());

                    TextbookInventoryTransaction tx =
                            TextbookInventoryTransaction.builder()
                                    .textbookId(inv.getTextbookId())
                                    .transactionType("RETURN")
                                    .quantity(request.quantity())
                                    .performedBy(request.returnedBy())
                                    .build();

                    return textbookInventoryRepository.save(inv)
                            .then(textbookTransactionRepository.save(tx))
                            .thenReturn(inv);
                })
                .as(transactionalOperator::transactional)
                .map(TextbookInventoryResponse::from);
    }

    public Mono<TextbookInventoryResponse> markDamaged(
            Long textbookId,
            DamageTextbookRequest request
    ) {
        return textbookInventoryRepository.findById(textbookId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Textbook not found")))
                .flatMap(inv -> {

                    if (inv.getAvailableQuantity() < request.quantity()) {
                        return Mono.error(new ValidationException("Not enough stock"));
                    }

                    inv.setAvailableQuantity(inv.getAvailableQuantity() - request.quantity());
                    inv.setDamagedQuantity(inv.getDamagedQuantity() + request.quantity());

                    TextbookInventoryTransaction tx =
                            TextbookInventoryTransaction.builder()
                                    .textbookId(inv.getTextbookId())
                                    .transactionType("DAMAGE")
                                    .quantity(request.quantity())
                                    .notes(request.reason())
                                    .performedBy(request.reportedBy())
                                    .build();

                    return textbookInventoryRepository.save(inv)
                            .then(textbookTransactionRepository.save(tx))
                            .thenReturn(inv);
                })
                .as(transactionalOperator::transactional)
                .map(TextbookInventoryResponse::from);
    }




    private Mono<AddTextbookResponse> createTextbookInventory(AddTextbookRequest request, SchoolDetailsResponse school) {
        return textbookInventoryRepository
                .save(
                        TextbookInventory.builder()
                                .schoolId(school.schoolId())
                                .schoolCode(school.schoolCode())
                                .bookType(request.bookType().name())
                                .providedBy(request.providedBy().name())
                                .subjectArea(request.subjectArea().name())
                                .gradeLevel(request.gradeLevel().name())
                                .title(request.title())
                                .author(request.author())
                                .publisher(request.publisher())
                                .edition(request.edition())
                                .isbn(request.isbn())
                                .publicationYear(request.publicationYear())
                                .build())
                .as(transactionalOperator::transactional)
                .map(AddTextbookResponse::from);
    }
}
