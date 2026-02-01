package com.emis.hrservice.service.impl;

import com.emis.hrservice.config.ServiceConfigurationProperties;
import com.emis.hrservice.domain.db.TextbookInventory;
import com.emis.hrservice.dto.request.AddTextbookRequest;
import com.emis.hrservice.dto.response.AddTextbookResponse;
import com.emis.hrservice.dto.response.TextbookInventoryResponse;
import com.emis.hrservice.exceptions.HrServiceException;
import com.emis.hrservice.exceptions.ResourceAlreadyExistsException;
import com.emis.hrservice.exceptions.ResourceTimeoutException;
import com.emis.hrservice.repository.TextbookInventoryRepository;
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
    @Override
    public Mono<AddTextbookResponse> addTextbookToInventory(AddTextbookRequest request, String schoolCode, String requestId) {
        return schoolCacheService
                .getSchoolDetails(schoolCode)
                .flatMap(
                        school -> createTextbookInventory(request, school.schoolId())
                                .onErrorResume(DuplicateKeyException.class,
                                        ex -> textbookInventoryRepository
                                        .findBySchoolIdAndTitleAndSubjectAreaAndEditionAndGradeLevelAndIsDeletedFalse(
                                                school.schoolId(),
                                                request.title(),
                                                request.subjectArea(),
                                                request.edition(),
                                                request.gradeLevel())
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

    private Mono<AddTextbookResponse> createTextbookInventory(AddTextbookRequest request, Long schoolId) {
        return textbookInventoryRepository
                .save(
                        TextbookInventory.builder()
                                .schoolId(schoolId)
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
