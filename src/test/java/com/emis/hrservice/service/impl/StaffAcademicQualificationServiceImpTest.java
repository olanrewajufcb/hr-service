package com.emis.hrservice.service.impl;

import com.emis.hrservice.config.ServiceConfigurationProperties;
import com.emis.hrservice.domain.db.Staff;
import com.emis.hrservice.domain.db.StaffAcademicQualification;
import com.emis.hrservice.dto.request.AddStaffAcademicQualificationRequest;
import com.emis.hrservice.dto.response.AddStaffAcademicQualificationResponse;
import com.emis.hrservice.enums.QualificationLevel;
import com.emis.hrservice.exceptions.HrServiceException;
import com.emis.hrservice.exceptions.ResourceCreationException;
import com.emis.hrservice.exceptions.ResourceNotFoundException;
import com.emis.hrservice.exceptions.ResourceTimeoutException;
import com.emis.hrservice.mapper.StaffQualificationMapper;
import com.emis.hrservice.repository.StaffAcademicQualificationRepository;
import com.emis.hrservice.repository.StaffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaffAcademicQualificationServiceImpTest {

    @Mock
    private StaffAcademicQualificationRepository staffAcademicQualificationRepository;

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private TransactionalOperator transactionalOperator;

    @Mock
    private StaffQualificationMapper qualificationMapper;

    @Mock
    private ServiceConfigurationProperties properties;

    @InjectMocks
    private StaffAcademicQualificationServiceImp staffAcademicQualificationService;

    private final String staffCode = "STF-001";
    private final String schoolCode = "SCH-001";
    private final String requestId = "req-123";
    private Staff staff;

    @BeforeEach
    void setUp() {
        staff = Staff.builder()
                .staffId(1L)
                .staffCode(staffCode)
                .schoolCode(schoolCode)
                .build();
    }

    @Test
    @SuppressWarnings("unchecked")
    void addStaffAcademicQualification_ShouldAddNew_WhenNotExists() {
        AddStaffAcademicQualificationRequest request = new AddStaffAcademicQualificationRequest(
                QualificationLevel.BSC_HND, "B.Sc. Computer Science", "University of Lagos", 2020, "Computer Science");

        StaffAcademicQualification qualification = StaffAcademicQualification.builder()
                .qualificationLevel(QualificationLevel.BSC_HND)
                .qualificationName("B.Sc. Computer Science")
                .institution("University of Lagos")
                .yearObtained(2020)
                .subjectArea("Computer Science")
                .build();

        StaffAcademicQualification savedQualification = StaffAcademicQualification.builder()
                .qualificationId(100L)
                .staffId(1L)
                .qualificationLevel(QualificationLevel.BSC_HND)
                .qualificationName("B.Sc. Computer Science")
                .institution("University of Lagos")
                .yearObtained(2020)
                .subjectArea("Computer Science")
                .createdAt(LocalDateTime.now())
                .build();

        when(staffRepository.findByStaffCodeAndSchoolCodeAndIsDeletedFalse(staffCode, schoolCode))
                .thenReturn(Mono.just(staff));
        when(staffAcademicQualificationRepository.findByStaffIdAndQualificationLevelAndYearObtainedAndIsDeletedFalse(
                1L, "BSC_HND", 2020)).thenReturn(Mono.empty());
        when(qualificationMapper.toEntity(request)).thenReturn(qualification);
        when(staffAcademicQualificationRepository.save(any(StaffAcademicQualification.class)))
                .thenReturn(Mono.just(savedQualification));
        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Mono<AddStaffAcademicQualificationResponse> result = staffAcademicQualificationService.addStaffAcademicQualification(staffCode, schoolCode, request, requestId);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.qualificationId().equals(100L) && response.staffId().equals(1L))
                .verifyComplete();

        verify(staffRepository).findByStaffCodeAndSchoolCodeAndIsDeletedFalse(staffCode, schoolCode);
        verify(staffAcademicQualificationRepository).save(any(StaffAcademicQualification.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void addStaffAcademicQualification_ShouldReturnExisting_WhenAlreadyExists() {
        AddStaffAcademicQualificationRequest request = new AddStaffAcademicQualificationRequest(
                QualificationLevel.BSC_HND, "B.Sc. Computer Science", "University of Lagos", 2020, "Computer Science");

        StaffAcademicQualification existingQualification = StaffAcademicQualification.builder()
                .qualificationId(100L)
                .staffId(1L)
                .qualificationLevel(QualificationLevel.BSC_HND)
                .qualificationName("B.Sc. Computer Science")
                .institution("University of Lagos")
                .yearObtained(2020)
                .subjectArea("Computer Science")
                .createdAt(LocalDateTime.now())
                .build();

        when(staffRepository.findByStaffCodeAndSchoolCodeAndIsDeletedFalse(staffCode, schoolCode))
                .thenReturn(Mono.just(staff));
        // Use matching qualification level as string as expected by the repository method
        when(staffAcademicQualificationRepository.findByStaffIdAndQualificationLevelAndYearObtainedAndIsDeletedFalse(
                1L, "BSC_HND", 2020)).thenReturn(Mono.just(existingQualification));
        when(qualificationMapper.toEntity(any())).thenReturn(existingQualification); // Mock mapper to avoid NPE in switchIfEmpty evaluation
        when(staffAcademicQualificationRepository.save(any())).thenReturn(Mono.just(existingQualification)); // Avoid NPE in map
        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Mono<AddStaffAcademicQualificationResponse> result = staffAcademicQualificationService.addStaffAcademicQualification(staffCode, schoolCode, request, requestId);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.qualificationId().equals(100L))
                .verifyComplete();
    }

    @Test
    @SuppressWarnings("unchecked")
    void addStaffAcademicQualification_ShouldThrowException_WhenStaffNotFound() {
        AddStaffAcademicQualificationRequest request = new AddStaffAcademicQualificationRequest(
                QualificationLevel.BSC_HND, "B.Sc. Computer Science", "University of Lagos", 2020, "Computer Science");

        when(staffRepository.findByStaffCodeAndSchoolCodeAndIsDeletedFalse(staffCode, schoolCode))
                .thenReturn(Mono.empty());
        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Mono<AddStaffAcademicQualificationResponse> result = staffAcademicQualificationService.addStaffAcademicQualification(staffCode, schoolCode, request, requestId);

        StepVerifier.create(result)
                .expectError(ResourceCreationException.class) // Because it wraps ResourceNotFoundException
                .verify();
    }

    @Test
    void retrieveStaffAcademicQualification_ShouldReturnPage_WhenExists() {
        Pageable pageable = PageRequest.of(0, 10);
        StaffAcademicQualification qual = StaffAcademicQualification.builder()
                .qualificationId(100L)
                .staffId(1L)
                .qualificationLevel(QualificationLevel.BSC_HND)
                .qualificationName("B.Sc.")
                .institution("Uni")
                .yearObtained(2020)
                .createdAt(LocalDateTime.now())
                .build();

        when(staffRepository.findByStaffCodeAndSchoolCodeAndIsDeletedFalse(staffCode, schoolCode))
                .thenReturn(Mono.just(staff));
        when(staffAcademicQualificationRepository.findByStaffIdAndIsDeletedFalse(1L, 10, 0L))
                .thenReturn(Flux.just(qual));
        when(staffAcademicQualificationRepository.countByStaffIdAndIsDeletedFalse(1L))
                .thenReturn(Mono.just(1L));
        when(properties.getTimeout()).thenReturn(5);

        Mono<Page<AddStaffAcademicQualificationResponse>> result = staffAcademicQualificationService.retrieveStaffAcademicQualification(staffCode, schoolCode, pageable, requestId);

        StepVerifier.create(result)
                .expectNextMatches(page -> page.getTotalElements() == 1 && page.getContent().get(0).qualificationId().equals(100L))
                .verifyComplete();
    }

    @Test
    void retrieveStaffAcademicQualification_ShouldReturnEmptyPage_WhenNoneExists() {
        Pageable pageable = PageRequest.of(0, 10);

        when(staffRepository.findByStaffCodeAndSchoolCodeAndIsDeletedFalse(staffCode, schoolCode))
                .thenReturn(Mono.just(staff));
        when(staffAcademicQualificationRepository.findByStaffIdAndIsDeletedFalse(1L, 10, 0L))
                .thenReturn(Flux.empty());
        when(staffAcademicQualificationRepository.countByStaffIdAndIsDeletedFalse(1L))
                .thenReturn(Mono.just(0L));
        when(properties.getTimeout()).thenReturn(5);

        Mono<Page<AddStaffAcademicQualificationResponse>> result = staffAcademicQualificationService.retrieveStaffAcademicQualification(staffCode, schoolCode, pageable, requestId);

        StepVerifier.create(result)
                .expectNextMatches(page -> page.getTotalElements() == 0 && page.getContent().isEmpty())
                .verifyComplete();
    }

    @Test
    void retrieveStaffAcademicQualification_ShouldThrowTimeoutException_WhenTimeout() {
        Pageable pageable = PageRequest.of(0, 10);

        when(staffRepository.findByStaffCodeAndSchoolCodeAndIsDeletedFalse(staffCode, schoolCode))
                .thenReturn(Mono.just(staff));
        // Ensure Mono.zip doesn't complete before timeout
        when(staffAcademicQualificationRepository.findByStaffIdAndIsDeletedFalse(1L, 10, 0L))
                .thenReturn(Flux.never()); // Never completes
        when(staffAcademicQualificationRepository.countByStaffIdAndIsDeletedFalse(1L))
                .thenReturn(Mono.never()); // Never completes
        when(properties.getTimeout()).thenReturn(1);

        Mono<Page<AddStaffAcademicQualificationResponse>> result = staffAcademicQualificationService.retrieveStaffAcademicQualification(staffCode, schoolCode, pageable, requestId);

        StepVerifier.withVirtualTime(() -> result)
                .expectSubscription()
                .thenAwait(java.time.Duration.ofSeconds(2))
                .expectError(ResourceTimeoutException.class)
                .verify();
    }

    @Test
    void retrieveStaffAcademicQualification_ShouldThrowNotFoundException_WhenStaffNotFound() {
        Pageable pageable = PageRequest.of(0, 10);

        when(staffRepository.findByStaffCodeAndSchoolCodeAndIsDeletedFalse(staffCode, schoolCode))
                .thenReturn(Mono.empty());

        Mono<Page<AddStaffAcademicQualificationResponse>> result = staffAcademicQualificationService.retrieveStaffAcademicQualification(staffCode, schoolCode, pageable, requestId);

        StepVerifier.create(result)
                .expectError(HrServiceException.class) // Because it's mapped in retrieveStaffAcademicQualification
                .verify();
    }
}
