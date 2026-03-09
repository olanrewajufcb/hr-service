package com.emis.hrservice.service.impl;

import com.emis.hrservice.domain.db.Staff;
import com.emis.hrservice.domain.db.StaffSubjectSpecialization;
import com.emis.hrservice.dto.request.SubjectSpecializationRequest;
import com.emis.hrservice.dto.response.SubjectSpecializationResponse;
import com.emis.hrservice.enums.ProficiencyLevel;
import com.emis.hrservice.enums.StaffCategory;
import com.emis.hrservice.exceptions.ResourceNotFoundException;
import com.emis.hrservice.exceptions.ValidationException;
import com.emis.hrservice.repository.StaffRepository;
import com.emis.hrservice.repository.StaffSubjectSpecializationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StaffSubjectSpecializationServiceImplTest {

    @Mock
    private StaffSubjectSpecializationRepository staffSubjectSpecializationRepository;

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private TransactionalOperator transactionalOperator;

    @InjectMocks
    private StaffSubjectSpecializationServiceImpl specializationService;

    private Staff teachingStaff;
    private SubjectSpecializationRequest request;
    private final String staffCode = "ST001";
    private final String schoolCode = "SCH001";
    private final String requestId = "req-123";

    @BeforeEach
    void setUp() {
        teachingStaff = Staff.builder()
                .staffId(1L)
                .staffCode(staffCode)
                .schoolCode(schoolCode)
                .staffCategory(StaffCategory.TEACHING)
                .isDeleted(false)
                .build();

        request = new SubjectSpecializationRequest(
                ProficiencyLevel.ADVANCED,
                "SUB01",
                "Mathematics",
                false
        );
    }

    @Test
    @DisplayName("Should successfully add a new subject specialization")
    void addSubjectSpecialization_Success() {
        when(staffRepository.findByStaffCodeAndSchoolCodeAndIsDeletedFalse(staffCode, schoolCode))
                .thenReturn(Mono.just(teachingStaff));
        when(staffSubjectSpecializationRepository.findByStaffIdAndSubjectCodeAndIsDeletedFalse(1L, "SUB01"))
                .thenReturn(Mono.empty());
        
        StaffSubjectSpecialization savedSpecialization = StaffSubjectSpecialization.builder()
                .specializationId(10L)
                .staffId(1L)
                .subjectCode("SUB01")
                .subjectName("Mathematics")
                .proficiencyLevel("ADVANCED")
                .isMainTeachingSubject(false)
                .build();
        
        when(staffSubjectSpecializationRepository.save(any(StaffSubjectSpecialization.class)))
                .thenReturn(Mono.just(savedSpecialization));

        StepVerifier.create(specializationService.addSubjectSpecialization(staffCode, schoolCode, request, requestId))
                .expectNextMatches(response -> 
                        response.specializationId().equals(10L) &&
                        response.subjectCode().equals("SUB01") &&
                        response.proficiencyLevel().equals("ADVANCED"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return existing specialization if already present")
    void addSubjectSpecialization_AlreadyExists_ReturnsExisting() {
        when(staffRepository.findByStaffCodeAndSchoolCodeAndIsDeletedFalse(staffCode, schoolCode))
                .thenReturn(Mono.just(teachingStaff));
        
        StaffSubjectSpecialization existingSpecialization = StaffSubjectSpecialization.builder()
                .specializationId(10L)
                .staffId(1L)
                .subjectCode("SUB01")
                .subjectName("Mathematics")
                .proficiencyLevel("ADVANCED")
                .isMainTeachingSubject(false)
                .build();

        when(staffSubjectSpecializationRepository.findByStaffIdAndSubjectCodeAndIsDeletedFalse(1L, "SUB01"))
                .thenReturn(Mono.just(existingSpecialization));

        StepVerifier.create(specializationService.addSubjectSpecialization(staffCode, schoolCode, request, requestId))
                .expectNextMatches(response -> response.specializationId().equals(10L))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should fail when staff is not found")
    void addSubjectSpecialization_StaffNotFound() {
        when(staffRepository.findByStaffCodeAndSchoolCodeAndIsDeletedFalse(staffCode, schoolCode))
                .thenReturn(Mono.empty());

        StepVerifier.create(specializationService.addSubjectSpecialization(staffCode, schoolCode, request, requestId))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("Should fail for non-teaching staff")
    void addSubjectSpecialization_NonTeachingStaff() {
        Staff nonTeachingStaff = Staff.builder()
                .staffId(2L)
                .staffCode(staffCode)
                .schoolCode(schoolCode)
                .staffCategory(StaffCategory.NON_TEACHING)
                .build();

        when(staffRepository.findByStaffCodeAndSchoolCodeAndIsDeletedFalse(staffCode, schoolCode))
                .thenReturn(Mono.just(nonTeachingStaff));

        StepVerifier.create(specializationService.addSubjectSpecialization(staffCode, schoolCode, request, requestId))
                .expectErrorMatches(throwable -> 
                        throwable instanceof ValidationException &&
                        throwable.getMessage().equals("Only teaching staff can have subject specializations"))
                .verify();
    }

    @Test
    @DisplayName("Should successfully add main teaching subject if none exists")
    void addSubjectSpecialization_MainTeachingSubject_Success() {
        request = new SubjectSpecializationRequest(ProficiencyLevel.ADVANCED, "SUB01", "Mathematics", true);

        when(staffRepository.findByStaffCodeAndSchoolCodeAndIsDeletedFalse(staffCode, schoolCode))
                .thenReturn(Mono.just(teachingStaff));
        when(staffSubjectSpecializationRepository.existsByStaffIdAndIsMainTeachingSubjectTrueAndIsDeletedFalse(1L))
                .thenReturn(Mono.just(false));
        when(staffSubjectSpecializationRepository.findByStaffIdAndSubjectCodeAndIsDeletedFalse(1L, "SUB01"))
                .thenReturn(Mono.empty());
        
        StaffSubjectSpecialization savedSpecialization = StaffSubjectSpecialization.builder()
                .specializationId(11L)
                .staffId(1L)
                .subjectCode("SUB01")
                .subjectName("Mathematics")
                .proficiencyLevel("ADVANCED")
                .isMainTeachingSubject(true)
                .build();
        
        when(staffSubjectSpecializationRepository.save(any(StaffSubjectSpecialization.class)))
                .thenReturn(Mono.just(savedSpecialization));

        StepVerifier.create(specializationService.addSubjectSpecialization(staffCode, schoolCode, request, requestId))
                .expectNextMatches(response -> response.specializationId().equals(11L))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should fail if staff already has a main teaching subject")
    void addSubjectSpecialization_MainTeachingSubject_AlreadyPresent() {
        request = new SubjectSpecializationRequest(ProficiencyLevel.ADVANCED, "SUB01", "Mathematics", true);

        when(staffRepository.findByStaffCodeAndSchoolCodeAndIsDeletedFalse(staffCode, schoolCode))
                .thenReturn(Mono.just(teachingStaff));
        when(staffSubjectSpecializationRepository.existsByStaffIdAndIsMainTeachingSubjectTrueAndIsDeletedFalse(1L))
                .thenReturn(Mono.just(true));

        StepVerifier.create(specializationService.addSubjectSpecialization(staffCode, schoolCode, request, requestId))
                .expectErrorMatches(throwable -> 
                        throwable instanceof ValidationException &&
                        throwable.getMessage().equals("Staff already has a main teaching subject"))
                .verify();
    }

    @Test
    @DisplayName("Should handle DuplicateKeyException from repository")
    void addSubjectSpecialization_DuplicateKeyException() {
        when(staffRepository.findByStaffCodeAndSchoolCodeAndIsDeletedFalse(staffCode, schoolCode))
                .thenReturn(Mono.just(teachingStaff));
        when(staffSubjectSpecializationRepository.findByStaffIdAndSubjectCodeAndIsDeletedFalse(1L, "SUB01"))
                .thenReturn(Mono.empty());
        when(staffSubjectSpecializationRepository.save(any(StaffSubjectSpecialization.class)))
                .thenReturn(Mono.error(new DuplicateKeyException("Duplicate entry")));

        StepVerifier.create(specializationService.addSubjectSpecialization(staffCode, schoolCode, request, requestId))
                .expectErrorMatches(throwable -> 
                        throwable instanceof ValidationException &&
                        throwable.getMessage().equals("Subject specialization already exists"))
                .verify();
    }
}
