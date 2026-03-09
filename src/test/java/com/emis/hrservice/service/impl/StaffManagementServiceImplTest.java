package com.emis.hrservice.service.impl;

import com.emis.hrservice.config.ServiceConfigurationProperties;
import com.emis.hrservice.domain.db.Staff;
import com.emis.hrservice.domain.db.StaffServiceHistory;
import com.emis.hrservice.dto.request.CreateStaffRequest;
import com.emis.hrservice.dto.request.UpdateEmergencyContactRequest;
import com.emis.hrservice.dto.request.UpdateStaffBioRequest;
import com.emis.hrservice.dto.response.CreateStaffResponse;
import com.emis.hrservice.dto.response.EmergencyContactResponse;
import com.emis.hrservice.dto.response.SchoolDetailsResponse;
import com.emis.hrservice.dto.response.UpdateStaffBioResponse;
import com.emis.hrservice.enums.*;
import com.emis.hrservice.exceptions.HrServiceException;
import com.emis.hrservice.exceptions.ResourceNotFoundException;
import com.emis.hrservice.exceptions.ResourceTimeoutException;
import com.emis.hrservice.mapper.StaffMapper;
import com.emis.hrservice.repository.StaffRepository;
import com.emis.hrservice.repository.StaffServiceHistoryRepository;
import com.emis.hrservice.service.cache.SchoolCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.TimeoutException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaffManagementServiceImplTest {

    @Mock
    private StaffRepository staffRepository;
    @Mock
    private StaffServiceHistoryRepository staffServiceHistoryRepository;
    @Mock
    private SchoolCacheService schoolCacheService;
    @Mock
    private StaffMapper staffMapper;
    @Mock
    private TransactionalOperator transactionalOperator;
    @Mock
    private ServiceConfigurationProperties properties;

    @InjectMocks
    private StaffManagementServiceImpl staffManagementService;

    private Staff staff;
    private CreateStaffRequest createStaffRequest;
    private SchoolDetailsResponse schoolDetails;

    @BeforeEach
    void setUp() {
        staff = Staff.builder()
                .staffId(1L)
                .staffCode("STF001")
                .schoolId(10L)
                .schoolCode("SCH001")
                .schoolName("Test School")
                .firstName("John")
                .lastName("Doe")
                .gender(Gender.MALE)
                .staffCategory(StaffCategory.TEACHING)
                .staffRole(StaffRole.TEACHER)
                .employmentType(EmploymentType.FULL_TIME)
                .salarySource(SalarySource.STATE_GOVERNMENT_SCHOOL_PAYROLL)
                .status(Status.ACTIVE)
                .createdAt(LocalDateTime.now())
                .appointmentDate(LocalDate.now())
                .isDeleted(false)
                .build();

        createStaffRequest = new CreateStaffRequest(
                "SCH001", "STF001", "John", "Doe", Gender.MALE,
                StaffCategory.TEACHING, StaffRole.TEACHER, EmploymentType.FULL_TIME,
                SalarySource.STATE_GOVERNMENT_SCHOOL_PAYROLL, LocalDate.now(), "Math", "LGA"
        );

        schoolDetails = new SchoolDetailsResponse(
                10L, "SCH001", "Test School", SchoolType.PUBLIC, SchoolLevel.PRIMARY,
                "Address", "Phone", "Email", "Principal", 40, 500L,
                "Calendar", LocalDate.now(), "City", "Ward", "LGA", "State",
                SchoolStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now()
        );

        // Lenient mocks for transactionalOperator as it's used in many tests
        lenient().when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Default mocks to avoid NPE during Reactor assembly of unused paths
        lenient().when(staffRepository.save(any(Staff.class))).thenReturn(Mono.empty());
        lenient().when(staffServiceHistoryRepository.save(any(StaffServiceHistory.class))).thenReturn(Mono.empty());
    }

    @Test
    void createStaff_WhenStaffAlreadyExists_ShouldReturnExistingStaff() {
        when(schoolCacheService.getSchoolDetails("SCH001")).thenReturn(Mono.just(schoolDetails));
        when(staffMapper.toEntity(createStaffRequest)).thenReturn(staff);
        when(staffRepository.findByStaffCodeAndSchoolCodeAndIsDeletedFalse("STF001", "SCH001"))
                .thenReturn(Mono.just(staff));

        StepVerifier.create(staffManagementService.createStaff(createStaffRequest, "req-1"))
                .expectNextMatches(response -> response.staffCode().equals("STF001"))
                .verifyComplete();

        verify(staffRepository, never()).save(any());
        verify(staffServiceHistoryRepository, never()).save(any());
    }

    @Test
    void createStaff_WhenStaffDoesNotExist_ShouldCreateNewStaff() {
        when(schoolCacheService.getSchoolDetails("SCH001")).thenReturn(Mono.just(schoolDetails));
        when(staffMapper.toEntity(createStaffRequest)).thenReturn(staff);
        when(staffRepository.findByStaffCodeAndSchoolCodeAndIsDeletedFalse("STF001", "SCH001"))
                .thenReturn(Mono.empty());
        when(staffRepository.save(any(Staff.class))).thenReturn(Mono.just(staff));
        when(staffServiceHistoryRepository.save(any(StaffServiceHistory.class)))
                .thenReturn(Mono.just(new StaffServiceHistory()));

        StepVerifier.create(staffManagementService.createStaff(createStaffRequest, "req-1"))
                .expectNextMatches(response -> response.staffCode().equals("STF001"))
                .verifyComplete();

        verify(staffRepository).save(any());
        verify(staffServiceHistoryRepository).save(any());
    }

    @Test
    void retrieveStaff_WhenExists_ShouldReturnStaff() {
        when(staffRepository.findByStaffCodeAndSchoolCodeAndIsDeletedFalse("STF001", "SCH001"))
                .thenReturn(Mono.just(staff));

        StepVerifier.create(staffManagementService.retrieveStaff("SCH001", "STF001", "req-1"))
                .expectNextMatches(response -> response.staffCode().equals("STF001"))
                .verifyComplete();
    }

    @Test
    void retrieveStaff_WhenNotFound_ShouldError() {
        when(staffRepository.findByStaffCodeAndSchoolCodeAndIsDeletedFalse("STF001", "SCH001"))
                .thenReturn(Mono.empty());

        StepVerifier.create(staffManagementService.retrieveStaff("SCH001", "STF001", "req-1"))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void retrieveStaffById_WhenExists_ShouldReturnStaff() {
        when(staffRepository.findById(1L)).thenReturn(Mono.just(staff));

        StepVerifier.create(staffManagementService.retrieveStaffById(1L, "req-1"))
                .expectNextMatches(response -> response.staffId().equals(1L))
                .verifyComplete();
    }

    @Test
    void retrieveStaffById_WhenNotFound_ShouldError() {
        when(staffRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(staffManagementService.retrieveStaffById(1L, "req-1"))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void retrieveStaffsBySchoolCode_ShouldReturnPaginatedResults() {
        Pageable pageable = PageRequest.of(0, 10);
        when(staffRepository.findBySchoolCodeAndIsDeletedFalse("SCH001", 10, 0L))
                .thenReturn(Flux.just(staff));
        when(staffRepository.countBySchoolCodeAndIsDeletedFalse("SCH001"))
                .thenReturn(Mono.just(1L));
        when(properties.getTimeout()).thenReturn(5);

        StepVerifier.create(staffManagementService.retrieveStaffsBySchoolCode("SCH001", pageable, "req-1"))
                .expectNextMatches(page -> page.getTotalElements() == 1 && page.getContent().size() == 1)
                .verifyComplete();
    }

    @Test
    void retrieveStaffsBySchoolCode_WhenTimeout_ShouldThrowResourceTimeoutException() {
        Pageable pageable = PageRequest.of(0, 10);
        when(staffRepository.findBySchoolCodeAndIsDeletedFalse(anyString(), anyInt(), anyLong()))
                .thenReturn(Flux.never());
        when(staffRepository.countBySchoolCodeAndIsDeletedFalse(anyString()))
                .thenReturn(Mono.never());
        when(properties.getTimeout()).thenReturn(1);

        StepVerifier.withVirtualTime(() -> staffManagementService.retrieveStaffsBySchoolCode("SCH001", pageable, "req-1"))
                .thenAwait(java.time.Duration.ofSeconds(2))
                .expectError(ResourceTimeoutException.class)
                .verify();
    }

    @Test
    void retrieveStaffsBySchoolCode_WhenGenericError_ShouldThrowHrServiceException() {
        Pageable pageable = PageRequest.of(0, 10);
        when(staffRepository.findBySchoolCodeAndIsDeletedFalse(anyString(), anyInt(), anyLong()))
                .thenReturn(Flux.error(new RuntimeException("DB Error")));
        when(staffRepository.countBySchoolCodeAndIsDeletedFalse(anyString()))
                .thenReturn(Mono.just(1L));
        when(properties.getTimeout()).thenReturn(5);

        StepVerifier.create(staffManagementService.retrieveStaffsBySchoolCode("SCH001", pageable, "req-1"))
                .expectError(HrServiceException.class)
                .verify();
    }

    @Test
    void updateStaffBio_WhenExists_ShouldUpdateAndReturnResponse() {
        UpdateStaffBioRequest request = new UpdateStaffBioRequest(
                LocalDate.of(1990, 1, 1), "SCH001", "new@email.com", "123456789", "New Address", "Nigerian"
        );
        when(staffRepository.findByStaffCodeAndSchoolCodeAndIsDeletedFalse("STF001", "SCH001"))
                .thenReturn(Mono.just(staff));
        when(staffRepository.save(any(Staff.class))).thenReturn(Mono.just(staff));

        StepVerifier.create(staffManagementService.updateStaffBio("STF001", request, "req-1"))
                .expectNextMatches(response -> response.staffCode().equals("STF001"))
                .verifyComplete();

        verify(staffRepository).save(argThat(s -> s.getEmail().equals("new@email.com")));
    }

    @Test
    void updateStaffBio_WhenNotFound_ShouldError() {
        UpdateStaffBioRequest request = new UpdateStaffBioRequest(
                LocalDate.now(), "SCH001", "email", "phone", "address", "nationality"
        );
        when(staffRepository.findByStaffCodeAndSchoolCodeAndIsDeletedFalse("STF001", "SCH001"))
                .thenReturn(Mono.empty());

        StepVerifier.create(staffManagementService.updateStaffBio("STF001", request, "req-1"))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void updateStaffEmergencyContact_WhenExists_ShouldUpdateAndReturnResponse() {
        UpdateEmergencyContactRequest request = new UpdateEmergencyContactRequest(
                "SCH001", "Jane Doe", "987654321", "Spouse"
        );
        when(staffRepository.findByStaffCodeAndSchoolCodeAndIsDeletedFalse("STF001", "SCH001"))
                .thenReturn(Mono.just(staff));
        when(staffRepository.save(any(Staff.class))).thenReturn(Mono.just(staff));

        StepVerifier.create(staffManagementService.updateStaffEmergencyContact("STF001", request, "req-1"))
                .expectNextMatches(response -> response.staffCode().equals("STF001"))
                .verifyComplete();

        verify(staffRepository).save(argThat(s -> s.getEmergencyContactName().equals("Jane Doe")));
    }

    @Test
    void updateStaffEmergencyContact_WhenNotFound_ShouldError() {
        UpdateEmergencyContactRequest request = new UpdateEmergencyContactRequest(
                "SCH001", "Jane Doe", "987654321", "Spouse"
        );
        when(staffRepository.findByStaffCodeAndSchoolCodeAndIsDeletedFalse("STF001", "SCH001"))
                .thenReturn(Mono.empty());

        StepVerifier.create(staffManagementService.updateStaffEmergencyContact("STF001", request, "req-1"))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }
}
