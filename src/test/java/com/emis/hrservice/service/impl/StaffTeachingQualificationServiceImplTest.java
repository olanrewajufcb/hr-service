package com.emis.hrservice.service.impl;

import com.emis.hrservice.config.ServiceConfigurationProperties;
import com.emis.hrservice.domain.db.Staff;
import com.emis.hrservice.domain.db.StaffTeachingQualification;
import com.emis.hrservice.dto.request.AddStaffTeachingQualificationRequest;
import com.emis.hrservice.dto.response.StaffTeachingQualificationResponse;
import com.emis.hrservice.enums.SubjectOfQualification;
import com.emis.hrservice.enums.TeachingQualification;
import com.emis.hrservice.exceptions.HrServiceException;
import com.emis.hrservice.exceptions.ResourceCreationException;
import com.emis.hrservice.exceptions.ResourceNotFoundException;
import com.emis.hrservice.exceptions.ResourceTimeoutException;
import com.emis.hrservice.mapper.StaffTeachingQualificationMapper;
import com.emis.hrservice.repository.StaffRepository;
import com.emis.hrservice.repository.StaffTeachingQualificationRepository;
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

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaffTeachingQualificationServiceImplTest {

    @Mock
    private StaffTeachingQualificationRepository staffTeachingQualificationRepository;
    @Mock
    private StaffRepository staffRepository;
    @Mock
    private ServiceConfigurationProperties properties;
    @Mock
    private TransactionalOperator transactionalOperator;
    @Mock
    private StaffTeachingQualificationMapper teachingQualificationMapper;

    @InjectMocks
    private StaffTeachingQualificationServiceImpl staffTeachingQualificationService;

    private static final String STAFF_CODE = "STAFF001";
    private static final String SCHOOL_CODE = "SCH001";
    private static final String REQUEST_ID = "REQ001";
    private static final Long STAFF_ID = 1L;

    @BeforeEach
    void setUp() {
        lenient().when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(properties.getTimeout()).thenReturn(5);
    }

    @Test
    void addStaffTeachingQualification_Success_NewQualification() {
        // Arrange
        AddStaffTeachingQualificationRequest request = new AddStaffTeachingQualificationRequest(
                TeachingQualification.PGDE,
                SubjectOfQualification.ENGLISH,
                "Lagos University",
                2021
        );

        Staff staff = Staff.builder().staffId(STAFF_ID).staffCode(STAFF_CODE).schoolCode(SCHOOL_CODE).build();
        StaffTeachingQualification entity = new StaffTeachingQualification();
        entity.setTeachingQualification(TeachingQualification.PGDE.name());
        entity.setSubjectOfQualification(SubjectOfQualification.ENGLISH.name());
        entity.setInstitution("Lagos University");
        entity.setYearObtained(2021);

        StaffTeachingQualification savedEntity = StaffTeachingQualification.builder()
                .teachingQualificationId(101L)
                .staffId(STAFF_ID)
                .teachingQualification(TeachingQualification.PGDE.name())
                .subjectOfQualification(SubjectOfQualification.ENGLISH.name())
                .institution("Lagos University")
                .yearObtained(2021)
                .build();

        when(staffRepository.findByStaffCodeAndSchoolCodeAndIsDeletedFalse(STAFF_CODE, SCHOOL_CODE))
                .thenReturn(Mono.just(staff));
        when(staffTeachingQualificationRepository.findByStaffIdAndTeachingQualificationAndSubjectOfQualificationAndIsDeletedFalse(
                STAFF_ID, TeachingQualification.PGDE.name(), SubjectOfQualification.ENGLISH.name()))
                .thenReturn(Mono.empty());
        when(teachingQualificationMapper.toEntity(request)).thenReturn(entity);
        when(staffTeachingQualificationRepository.save(any(StaffTeachingQualification.class)))
                .thenReturn(Mono.just(savedEntity));

        // Act & Assert
        staffTeachingQualificationService.addStaffTeachingQualification(STAFF_CODE, SCHOOL_CODE, request, REQUEST_ID)
                .as(StepVerifier::create)
                .expectNextMatches(response -> response.teachingQualificationId().equals(101L) &&
                        response.teachingQualification().equals("PGDE"))
                .verifyComplete();

        verify(staffTeachingQualificationRepository).save(any(StaffTeachingQualification.class));
    }

    @Test
    void addStaffTeachingQualification_Success_ExistingQualification() {
        // Arrange
        AddStaffTeachingQualificationRequest request = new AddStaffTeachingQualificationRequest(
                TeachingQualification.NCE,
                SubjectOfQualification.MATHEMATICS,
                "Test Uni",
                2020
        );

        Staff staff = Staff.builder().staffId(STAFF_ID).staffCode(STAFF_CODE).schoolCode(SCHOOL_CODE).build();
        StaffTeachingQualification existingQualification = StaffTeachingQualification.builder()
                .teachingQualificationId(10L)
                .staffId(STAFF_ID)
                .teachingQualification(TeachingQualification.NCE.name())
                .subjectOfQualification(SubjectOfQualification.MATHEMATICS.name())
                .build();

        when(staffRepository.findByStaffCodeAndSchoolCodeAndIsDeletedFalse(STAFF_CODE, SCHOOL_CODE))
                .thenReturn(Mono.just(staff));
        when(staffTeachingQualificationRepository.findByStaffIdAndTeachingQualificationAndSubjectOfQualificationAndIsDeletedFalse(
                STAFF_ID, TeachingQualification.NCE.name(), SubjectOfQualification.MATHEMATICS.name()))
                .thenReturn(Mono.just(existingQualification));

        // Act & Assert
        staffTeachingQualificationService.addStaffTeachingQualification(STAFF_CODE, SCHOOL_CODE, request, REQUEST_ID)
                .as(StepVerifier::create)
                .expectNextMatches(response -> response.teachingQualificationId().equals(10L))
                .verifyComplete();

        verify(staffTeachingQualificationRepository, never()).save(any());
    }

    @Test
    void addStaffTeachingQualification_StaffNotFound() {
        // Arrange
        AddStaffTeachingQualificationRequest request = new AddStaffTeachingQualificationRequest(
                TeachingQualification.NCE,
                SubjectOfQualification.MATHEMATICS,
                "Test Uni",
                2020
        );

        when(staffRepository.findByStaffCodeAndSchoolCodeAndIsDeletedFalse(STAFF_CODE, SCHOOL_CODE))
                .thenReturn(Mono.empty());

        // Act & Assert
        staffTeachingQualificationService.addStaffTeachingQualification(STAFF_CODE, SCHOOL_CODE, request, REQUEST_ID)
                .as(StepVerifier::create)
                .expectErrorMatches(throwable -> throwable instanceof ResourceCreationException &&
                        throwable.getCause() instanceof ResourceNotFoundException &&
                        throwable.getCause().getMessage().contains("Staff not found"))
                .verify();
    }

    @Test
    void retrieveStaffTeachingQualifications_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Staff staff = Staff.builder().staffId(STAFF_ID).staffCode(STAFF_CODE).schoolCode(SCHOOL_CODE).build();
        StaffTeachingQualification q1 = StaffTeachingQualification.builder().teachingQualificationId(1L).staffId(STAFF_ID).build();
        StaffTeachingQualification q2 = StaffTeachingQualification.builder().teachingQualificationId(2L).staffId(STAFF_ID).build();

        when(staffRepository.findByStaffCodeAndSchoolCodeAndIsDeletedFalse(STAFF_CODE, SCHOOL_CODE))
                .thenReturn(Mono.just(staff));
        when(staffTeachingQualificationRepository.findByStaffIdAndIsDeletedFalse(STAFF_ID, 10, 0L))
                .thenReturn(Flux.just(q1, q2));
        when(staffTeachingQualificationRepository.countByStaffIdAndIsDeletedFalse(STAFF_ID))
                .thenReturn(Mono.just(2L));

        // Act & Assert
        staffTeachingQualificationService.retrieveStaffTeachingQualifications(STAFF_CODE, SCHOOL_CODE, pageable, REQUEST_ID)
                .as(StepVerifier::create)
                .assertNext(page -> {
                    assertEquals(2, page.getTotalElements());
                    assertEquals(2, page.getContent().size());
                })
                .verifyComplete();
    }

    @Test
    void retrieveStaffTeachingQualifications_Empty() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Staff staff = Staff.builder().staffId(STAFF_ID).staffCode(STAFF_CODE).schoolCode(SCHOOL_CODE).build();

        when(staffRepository.findByStaffCodeAndSchoolCodeAndIsDeletedFalse(STAFF_CODE, SCHOOL_CODE))
                .thenReturn(Mono.just(staff));
        when(staffTeachingQualificationRepository.findByStaffIdAndIsDeletedFalse(STAFF_ID, 10, 0L))
                .thenReturn(Flux.empty());
        when(staffTeachingQualificationRepository.countByStaffIdAndIsDeletedFalse(STAFF_ID))
                .thenReturn(Mono.just(0L));

        // Act & Assert
        staffTeachingQualificationService.retrieveStaffTeachingQualifications(STAFF_CODE, SCHOOL_CODE, pageable, REQUEST_ID)
                .as(StepVerifier::create)
                .assertNext(page -> {
                    assertEquals(0, page.getTotalElements());
                    assertTrue(page.getContent().isEmpty());
                })
                .verifyComplete();
    }

    @Test
    void retrieveStaffTeachingQualifications_Timeout() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Staff staff = Staff.builder().staffId(STAFF_ID).staffCode(STAFF_CODE).schoolCode(SCHOOL_CODE).build();

        when(staffRepository.findByStaffCodeAndSchoolCodeAndIsDeletedFalse(STAFF_CODE, SCHOOL_CODE))
                .thenReturn(Mono.just(staff));
        when(staffTeachingQualificationRepository.findByStaffIdAndIsDeletedFalse(STAFF_ID, 10, 0L))
                .thenReturn(Flux.never());
        when(staffTeachingQualificationRepository.countByStaffIdAndIsDeletedFalse(STAFF_ID))
                .thenReturn(Mono.never());
        when(properties.getTimeout()).thenReturn(1);

        // Act & Assert
        staffTeachingQualificationService.retrieveStaffTeachingQualifications(STAFF_CODE, SCHOOL_CODE, pageable, REQUEST_ID)
                .as(StepVerifier::create)
                .expectError(ResourceTimeoutException.class)
                .verify();
    }

    @Test
    void retrieveStaffTeachingQualifications_StaffNotFound() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        when(staffRepository.findByStaffCodeAndSchoolCodeAndIsDeletedFalse(STAFF_CODE, SCHOOL_CODE))
                .thenReturn(Mono.empty());

        // Act & Assert
        staffTeachingQualificationService.retrieveStaffTeachingQualifications(STAFF_CODE, SCHOOL_CODE, pageable, REQUEST_ID)
                .as(StepVerifier::create)
                .expectErrorMatches(throwable -> throwable instanceof HrServiceException &&
                        throwable.getCause() instanceof ResourceNotFoundException)
                .verify();
    }
}
