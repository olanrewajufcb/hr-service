package com.emis.hrservice.service.impl;

import com.emis.hrservice.events.outbox.OutboxEvent;
import com.emis.hrservice.domain.db.Staff;
import com.emis.hrservice.domain.db.StaffServiceHistory;
import com.emis.hrservice.dto.request.StaffTransferRequest;
import com.emis.hrservice.dto.response.SchoolDetailsResponse;
import com.emis.hrservice.dto.response.StaffTransferResponse;
import com.emis.hrservice.enums.ChangeType;
import com.emis.hrservice.enums.SchoolLevel;
import com.emis.hrservice.enums.SchoolStatus;
import com.emis.hrservice.enums.SchoolType;
import com.emis.hrservice.events.outbox.OutboxEventRepository;
import com.emis.hrservice.exceptions.BadRequestException;
import com.emis.hrservice.exceptions.ResourceNotFoundException;
import com.emis.hrservice.exceptions.ValidationException;
import com.emis.hrservice.mapper.StaffTransferMapper;
import com.emis.hrservice.repository.StaffRepository;
import com.emis.hrservice.repository.StaffServiceHistoryRepository;
import com.emis.hrservice.service.cache.SchoolCacheService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StaffTransferServiceImplTest {

    @Mock
    private StaffRepository staffRepository;
    @Mock
    private StaffServiceHistoryRepository staffServiceHistoryRepository;
    @Mock
    private StaffTransferMapper staffTransferMapper;
    @Mock
    private SchoolCacheService schoolCacheService;
    @Mock
    private TransactionalOperator transactionalOperator;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private OutboxEventRepository outboxRepository;

    @InjectMocks
    private StaffTransferServiceImpl staffTransferService;

    private StaffTransferRequest transferRequest;
    private Staff staff;
    private StaffServiceHistory history;
    private SchoolDetailsResponse schoolDetails;

    @BeforeEach
    void setUp() {
        transferRequest = new StaffTransferRequest(
                ChangeType.TRANSFER,
                "New Position",
                "SCH001",
                "SCH002",
                LocalDate.now(),
                "Transfer remarks"
        );

        staff = new Staff();
        staff.setStaffId(1L);
        staff.setStaffCode("STF001");
        staff.setSchoolCode("SCH001");
        staff.setSchoolId(101L);
        staff.setFirstName("John");
        staff.setLastName("Doe");

        history = new StaffServiceHistory();
        history.setHistoryId(1L);
        history.setStaffId(1L);

        schoolDetails = new SchoolDetailsResponse(
                202L, "SCH002", "School Two", SchoolType.PUBLIC, SchoolLevel.SECONDARY,
                "Address", "Phone", "Email", "Principal", 30, 1000L, "Calendar",
                LocalDate.now(), "City", "Ward", "LGA", "State", SchoolStatus.ACTIVE,
                LocalDateTime.now(), LocalDateTime.now()
        );
    }

    @Test
    void transferStaff_Success() {
        when(staffTransferMapper.toEntity(any())).thenReturn(new StaffServiceHistory());
        when(schoolCacheService.getSchoolDetails("SCH002")).thenReturn(Mono.just(schoolDetails));
        when(staffRepository.findByStaffCodeAndSchoolCodeAndIsDeletedFalse("STF001", "SCH001"))
                .thenReturn(Mono.just(staff));
        when(staffServiceHistoryRepository.existsActiveTransfer(anyLong(), anyLong(), any()))
                .thenReturn(Mono.just(false));
        when(staffServiceHistoryRepository.updateStaffServiceHistory(anyLong())).thenReturn(Mono.empty());
        when(staffServiceHistoryRepository.save(any())).thenReturn(Mono.just(history));
        when(staffRepository.updateStaffSchool(anyLong(), anyString(), anyLong(), any()))
                .thenReturn(Mono.just(1));
        when(outboxRepository.save(any())).thenReturn(Mono.just(new OutboxEvent()));
        when(objectMapper.valueToTree(any())).thenReturn(null);

        // Mock TransactionalOperator
        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(staffTransferService.transferStaff("STF001", transferRequest, "req-123"))
                .expectNextMatches(response -> response.staffId().equals(1L))
                .verifyComplete();
    }

    @Test
    void transferStaff_InvalidChangeType() {
        StaffTransferRequest invalidRequest = new StaffTransferRequest(
                ChangeType.PROMOTION, // Not TRANSFER
                "New Position",
                "SCH001",
                "SCH002",
                LocalDate.now(),
                "Remarks"
        );
        when(staffTransferMapper.toEntity(invalidRequest)).thenReturn(new StaffServiceHistory());

        StepVerifier.create(staffTransferService.transferStaff("STF001", invalidRequest, "req-123"))
                .expectError(BadRequestException.class)
                .verify();
    }

    @Test
    void transferStaff_SameSchoolCodes() {
        StaffTransferRequest sameSchoolRequest = new StaffTransferRequest(
                ChangeType.TRANSFER,
                "New Position",
                "SCH001",
                "SCH001", // Same
                LocalDate.now(),
                "Remarks"
        );
        when(staffTransferMapper.toEntity(sameSchoolRequest)).thenReturn(new StaffServiceHistory());

        StepVerifier.create(staffTransferService.transferStaff("STF001", sameSchoolRequest, "req-123"))
                .expectError(ValidationException.class)
                .verify();
    }

    @Test
    void transferStaff_StaffNotFound() {
        when(staffTransferMapper.toEntity(any())).thenReturn(new StaffServiceHistory());
        when(schoolCacheService.getSchoolDetails("SCH002")).thenReturn(Mono.just(schoolDetails));
        when(staffRepository.findByStaffCodeAndSchoolCodeAndIsDeletedFalse("STF001", "SCH001"))
                .thenReturn(Mono.empty());

        // Mock TransactionalOperator
        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(staffTransferService.transferStaff("STF001", transferRequest, "req-123"))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void transferStaff_ActiveTransferExists() {
        when(staffTransferMapper.toEntity(any())).thenReturn(new StaffServiceHistory());
        when(schoolCacheService.getSchoolDetails("SCH002")).thenReturn(Mono.just(schoolDetails));
        when(staffRepository.findByStaffCodeAndSchoolCodeAndIsDeletedFalse("STF001", "SCH001"))
                .thenReturn(Mono.just(staff));
        when(staffServiceHistoryRepository.existsActiveTransfer(anyLong(), anyLong(), any()))
                .thenReturn(Mono.just(true));

        // Mock TransactionalOperator
        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(staffTransferService.transferStaff("STF001", transferRequest, "req-123"))
                .expectError(BadRequestException.class)
                .verify();
    }

    @Test
    void getStaffServiceHistory_Success() {
        when(staffRepository.findById(1L)).thenReturn(Mono.just(staff));
        when(staffServiceHistoryRepository.findByStaffId(1L)).thenReturn(Flux.just(history));

        StepVerifier.create(staffTransferService.getStaffServiceHistory(1L))
                .expectNextMatches(response -> response.staffId().equals(1L))
                .verifyComplete();
    }

    @Test
    void getStaffServiceHistory_StaffNotFound() {
        when(staffRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(staffTransferService.getStaffServiceHistory(1L))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }
}
