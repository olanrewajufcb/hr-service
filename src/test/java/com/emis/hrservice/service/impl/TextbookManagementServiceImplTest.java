package com.emis.hrservice.service.impl;

import com.emis.hrservice.config.ServiceConfigurationProperties;
import com.emis.hrservice.domain.db.TextbookInventory;
import com.emis.hrservice.domain.db.TextbookInventoryTransaction;
import com.emis.hrservice.dto.request.*;
import com.emis.hrservice.dto.response.SchoolDetailsResponse;
import com.emis.hrservice.enums.*;
import com.emis.hrservice.exceptions.*;
import com.emis.hrservice.repository.TextbookInventoryRepository;
import com.emis.hrservice.repository.TextbookTransactionRepository;
import com.emis.hrservice.service.cache.SchoolCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TextbookManagementServiceImplTest {

    @Mock
    private TextbookInventoryRepository textbookInventoryRepository;
    @Mock
    private TransactionalOperator transactionalOperator;
    @Mock
    private SchoolCacheService schoolCacheService;
    @Mock
    private ServiceConfigurationProperties properties;
    @Mock
    private TextbookTransactionRepository textbookTransactionRepository;

    @Captor
    private ArgumentCaptor<TextbookInventory> inventoryCaptor;

    @InjectMocks
    private TextbookManagementServiceImpl textbookManagementService;

    private final String schoolCode = "SCH001";
    private final String requestId = UUID.randomUUID().toString();
    private SchoolDetailsResponse schoolDetails;

    @BeforeEach
    void setUp() {
        schoolDetails = new SchoolDetailsResponse(
                1L, "SCH001", "Test School", SchoolType.PUBLIC, SchoolLevel.PRIMARY,
                "Address", "Phone", "Email", "Principal", 40, 1000L,
                "Calendar", null, "City", "Ward", "LGA", "State",
                SchoolStatus.ACTIVE, null, null
        );
    }

    @Test
    void addTextbookToInventory_Success() {
        AddTextbookRequest request = new AddTextbookRequest(
                "Math Grade 1", "Author", "Publisher", "1st Edition", "ISBN123",
                2023, BookType.PUPIL_BOOK, GradeLevel.PRY1, SubjectArea.MATHEMATICS, ProvidedBy.GOVERNMENT
        );

        when(schoolCacheService.getSchoolDetails(schoolCode)).thenReturn(Mono.just(schoolDetails));
        when(textbookInventoryRepository.save(any(TextbookInventory.class))).thenAnswer(invocation -> {
            TextbookInventory inv = invocation.getArgument(0);
            inv.setTextbookId(1L);
            return Mono.just(inv);
        });
        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(textbookManagementService.addTextbookToInventory(request, schoolCode, requestId))
                .assertNext(response -> {
                    assertEquals(1L, response.textbookId());
                })
                .verifyComplete();
    }

    @Test
    void addTextbookToInventory_DuplicateKey_Handled() {
        AddTextbookRequest request = new AddTextbookRequest(
                "Math Grade 1", "Author", "Publisher", "1st Edition", "ISBN123",
                2023, BookType.PUPIL_BOOK, GradeLevel.PRY1, SubjectArea.MATHEMATICS, ProvidedBy.GOVERNMENT
        );

        when(schoolCacheService.getSchoolDetails(schoolCode)).thenReturn(Mono.just(schoolDetails));
        when(textbookInventoryRepository.save(any(TextbookInventory.class))).thenReturn(Mono.error(new DuplicateKeyException("Duplicate")));
        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TextbookInventory existingInv = TextbookInventory.builder()
                .textbookId(10L)
                .title("Math Grade 1")
                .schoolId(1L)
                .build();

        when(textbookInventoryRepository.findBySchoolIdAndTitleAndSubjectAreaAndEditionAndGradeLevelAndIsDeletedFalse(
                eq(1L), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.just(existingInv));

        StepVerifier.create(textbookManagementService.addTextbookToInventory(request, schoolCode, requestId))
                .assertNext(response -> {
                    assertEquals(10L, response.textbookId());
                })
                .verifyComplete();
    }

    @Test
    void retrieveTextbooksFromInventory_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        TextbookInventory inv = TextbookInventory.builder()
                .textbookId(1L)
                .title("Book")
                .bookType("PUPIL_BOOK")
                .providedBy("GOVERNMENT")
                .subjectArea("MATHEMATICS")
                .gradeLevel("PRY1")
                .build();

        when(schoolCacheService.getSchoolDetails(schoolCode)).thenReturn(Mono.just(schoolDetails));
        when(textbookInventoryRepository.findBySchoolIdAndIsDeletedFalse(anyLong(), anyInt(), anyLong()))
                .thenReturn(Flux.just(inv));
        when(textbookInventoryRepository.countBySchoolIdAndIsDeletedFalse(anyLong())).thenReturn(Mono.just(1));
        when(properties.getTimeout()).thenReturn(5);

        StepVerifier.create(textbookManagementService.retrieveTextbooksFromInventory(schoolCode, pageable, requestId))
                .assertNext(page -> {
                    assertEquals(1, page.getTotalElements());
                    assertEquals("Book", page.getContent().get(0).title());
                })
                .verifyComplete();
    }

    @Test
    void retrieveTextbooksFromInventory_Timeout() {
        Pageable pageable = PageRequest.of(0, 10);

        when(schoolCacheService.getSchoolDetails(schoolCode)).thenReturn(Mono.just(schoolDetails));
        when(textbookInventoryRepository.findBySchoolIdAndIsDeletedFalse(anyLong(), anyInt(), anyLong()))
                .thenReturn(Flux.never());
        when(textbookInventoryRepository.countBySchoolIdAndIsDeletedFalse(anyLong())).thenReturn(Mono.never());
        when(properties.getTimeout()).thenReturn(1);

        StepVerifier.withVirtualTime(() -> textbookManagementService.retrieveTextbooksFromInventory(schoolCode, pageable, requestId))
                .expectSubscription()
                .thenAwait(Duration.ofSeconds(2))
                .expectErrorMatches(throwable -> throwable instanceof ResourceTimeoutException)
                .verify();
    }

    @Test
    void receiveTextbooks_Success() {
        Long textbookId = 1L;
        ReceiveTextbookRequest request = new ReceiveTextbookRequest(10, "User", "REF", "Notes");
        TextbookInventory inv = TextbookInventory.builder()
                .textbookId(textbookId)
                .totalQuantity(50)
                .availableQuantity(50)
                .bookType("PUPIL_BOOK")
                .providedBy("GOVERNMENT")
                .subjectArea("MATHEMATICS")
                .gradeLevel("PRY1")
                .build();

        when(textbookInventoryRepository.findById(textbookId)).thenReturn(Mono.just(inv));
        when(textbookInventoryRepository.save(any(TextbookInventory.class))).thenReturn(Mono.just(inv));
        when(textbookTransactionRepository.save(any(TextbookInventoryTransaction.class))).thenReturn(Mono.just(new TextbookInventoryTransaction()));
        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(textbookManagementService.receiveTextbooks(textbookId, request, requestId))
                .assertNext(response -> {
                    assertEquals(1L, response.textbookId());
                    verify(textbookInventoryRepository).save(inventoryCaptor.capture());
                    TextbookInventory saved = inventoryCaptor.getValue();
                    assertEquals(60, saved.getTotalQuantity());
                    assertEquals(60, saved.getAvailableQuantity());
                })
                .verifyComplete();
    }

    @Test
    void issueTextbooks_Success() {
        Long textbookId = 1L;
        IssueTextbookRequest request = new IssueTextbookRequest(5, IssuedToType.STUDENT, "Student A", "S001", "User");
        TextbookInventory inv = TextbookInventory.builder()
                .textbookId(textbookId)
                .availableQuantity(10)
                .issuedQuantity(0)
                .bookType("PUPIL_BOOK")
                .providedBy("GOVERNMENT")
                .subjectArea("MATHEMATICS")
                .gradeLevel("PRY1")
                .build();

        when(textbookInventoryRepository.findById(textbookId)).thenReturn(Mono.just(inv));
        when(textbookInventoryRepository.save(any(TextbookInventory.class))).thenReturn(Mono.just(inv));
        when(textbookTransactionRepository.save(any(TextbookInventoryTransaction.class))).thenReturn(Mono.just(new TextbookInventoryTransaction()));
        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(textbookManagementService.issueTextbooks(textbookId, request))
                .assertNext(response -> {
                    assertEquals(1L, response.textbookId());
                    verify(textbookInventoryRepository).save(inventoryCaptor.capture());
                    TextbookInventory saved = inventoryCaptor.getValue();
                    assertEquals(5, saved.getAvailableQuantity());
                    assertEquals(5, saved.getIssuedQuantity());
                })
                .verifyComplete();
    }

    @Test
    void issueTextbooks_InsufficientStock() {
        Long textbookId = 1L;
        IssueTextbookRequest request = new IssueTextbookRequest(20, IssuedToType.STUDENT, "Student A", "S001", "User");
        TextbookInventory inv = TextbookInventory.builder()
                .textbookId(textbookId)
                .availableQuantity(10)
                .build();

        when(textbookInventoryRepository.findById(textbookId)).thenReturn(Mono.just(inv));
        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(textbookManagementService.issueTextbooks(textbookId, request))
                .expectError(ValidationException.class)
                .verify();
    }

    @Test
    void returnTextbooks_Success() {
        Long textbookId = 1L;
        ReturnTextbookRequest request = new ReturnTextbookRequest(5, "User");
        TextbookInventory inv = TextbookInventory.builder()
                .textbookId(textbookId)
                .availableQuantity(10)
                .issuedQuantity(10)
                .bookType("PUPIL_BOOK")
                .providedBy("GOVERNMENT")
                .subjectArea("MATHEMATICS")
                .gradeLevel("PRY1")
                .build();

        when(textbookInventoryRepository.findById(textbookId)).thenReturn(Mono.just(inv));
        when(textbookInventoryRepository.save(any(TextbookInventory.class))).thenReturn(Mono.just(inv));
        when(textbookTransactionRepository.save(any(TextbookInventoryTransaction.class))).thenReturn(Mono.just(new TextbookInventoryTransaction()));
        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(textbookManagementService.returnTextbooks(textbookId, request))
                .assertNext(response -> {
                    assertEquals(1L, response.textbookId());
                    verify(textbookInventoryRepository).save(inventoryCaptor.capture());
                    TextbookInventory saved = inventoryCaptor.getValue();
                    assertEquals(15, saved.getAvailableQuantity());
                    assertEquals(5, saved.getIssuedQuantity());
                })
                .verifyComplete();
    }

    @Test
    void markDamaged_Success() {
        Long textbookId = 1L;
        DamageTextbookRequest request = new DamageTextbookRequest(2, "Broken", "User");
        TextbookInventory inv = TextbookInventory.builder()
                .textbookId(textbookId)
                .availableQuantity(10)
                .damagedQuantity(1)
                .bookType("PUPIL_BOOK")
                .providedBy("GOVERNMENT")
                .subjectArea("MATHEMATICS")
                .gradeLevel("PRY1")
                .build();

        when(textbookInventoryRepository.findById(textbookId)).thenReturn(Mono.just(inv));
        when(textbookInventoryRepository.save(any(TextbookInventory.class))).thenReturn(Mono.just(inv));
        when(textbookTransactionRepository.save(any(TextbookInventoryTransaction.class))).thenReturn(Mono.just(new TextbookInventoryTransaction()));
        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(textbookManagementService.markDamaged(textbookId, request))
                .assertNext(response -> {
                    assertEquals(1L, response.textbookId());
                    verify(textbookInventoryRepository).save(inventoryCaptor.capture());
                    TextbookInventory saved = inventoryCaptor.getValue();
                    assertEquals(8, saved.getAvailableQuantity());
                    assertEquals(3, saved.getDamagedQuantity());
                })
                .verifyComplete();
    }

    @Test
    void markDamaged_InsufficientStock() {
        Long textbookId = 1L;
        DamageTextbookRequest request = new DamageTextbookRequest(20, "Broken", "User");
        TextbookInventory inv = TextbookInventory.builder()
                .textbookId(textbookId)
                .availableQuantity(10)
                .build();

        when(textbookInventoryRepository.findById(textbookId)).thenReturn(Mono.just(inv));
        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(textbookManagementService.markDamaged(textbookId, request))
                .expectError(ValidationException.class)
                .verify();
    }

    @Test
    void receiveTextbooks_NotFound() {
        Long textbookId = 1L;
        ReceiveTextbookRequest request = new ReceiveTextbookRequest(10, "User", "REF", "Notes");

        when(textbookInventoryRepository.findById(textbookId)).thenReturn(Mono.empty());
        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(textbookManagementService.receiveTextbooks(textbookId, request, requestId))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }
}
