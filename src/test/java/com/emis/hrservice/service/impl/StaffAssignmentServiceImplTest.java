package com.emis.hrservice.service.impl;

import com.emis.hrservice.domain.db.Staff;
import com.emis.hrservice.domain.db.StaffAssignment;
import com.emis.hrservice.dto.request.CreateStaffAssignmentRequest;
import com.emis.hrservice.dto.response.StaffAssignmentResponse;
import com.emis.hrservice.enums.AssignmentRole;
import com.emis.hrservice.enums.StaffCategory;
import com.emis.hrservice.events.outbox.OutboxEvent;
import com.emis.hrservice.events.outbox.OutboxEventRepository;
import com.emis.hrservice.exceptions.ResourceNotFoundException;
import com.emis.hrservice.exceptions.ValidationException;
import com.emis.hrservice.mapper.StaffAssignmentMapper;
import com.emis.hrservice.repository.StaffAssignmentRepository;
import com.emis.hrservice.repository.StaffRepository;
import com.emis.hrservice.repository.StaffTeachingQualificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaffAssignmentServiceImplTest {

    @Mock
    private StaffAssignmentRepository staffAssignmentRepository;
    @Mock
    private StaffRepository staffRepository;
    @Mock
    private StaffAssignmentMapper assignmentMapper;
    @Mock
    private TransactionalOperator transactionalOperator;
    @Mock
    private StaffTeachingQualificationRepository staffTeachingQualificationRepository;
    @Mock
    private OutboxEventRepository outboxRepository;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private StaffAssignmentServiceImpl staffAssignmentService;

    private Staff staff;
    private CreateStaffAssignmentRequest request;
    private StaffAssignment assignment;

    @BeforeEach
    void setUp() {
        staff = Staff.builder()
                .staffId(1L)
                .staffCode("ST001")
                .schoolId(10L)
                .schoolCode("SCH001")
                .firstName("John")
                .lastName("Doe")
                .staffCategory(StaffCategory.TEACHING)
                .build();

        request = new CreateStaffAssignmentRequest(
                100L, 200L, 300L, AssignmentRole.SUBJECT_TEACHER, "2023/2024"
        );

        assignment = StaffAssignment.builder()
                .assignmentId(500L)
                .staffId(1L)
                .schoolId(10L)
                .classId(100L)
                .sectionId(200L)
                .subjectId(300L)
                .assignmentRole(AssignmentRole.SUBJECT_TEACHER.name())
                .academicYear("2023/2024")
                .assignmentStatus("ACTIVE")
                .build();

        // Mock transactionalOperator.transactional(Mono) - called during assembly
        lenient().when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void assignStaffToClass_Success() {
        when(staffRepository.findByStaffCodeAndSchoolCodeAndIsDeletedFalse("ST001", "SCH001"))
                .thenReturn(Mono.just(staff));
        when(staffTeachingQualificationRepository.staffHasQualification(1L))
                .thenReturn(Mono.just(true));
        when(staffAssignmentRepository.findByStaffIdAndClassIdAndSubjectIdAndAcademicYearAndIsDeletedFalse(
                1L, 100L, 300L, "2023/2024"))
                .thenReturn(Mono.empty());
        when(assignmentMapper.toEntity(any())).thenReturn(new StaffAssignment());
        when(staffAssignmentRepository.save(any())).thenReturn(Mono.just(assignment));
        when(outboxRepository.save(any())).thenReturn(Mono.just(new OutboxEvent()));
        when(objectMapper.valueToTree(any())).thenReturn(null);

        StepVerifier.create(staffAssignmentService.assignStaffToClass(request, "ST001", "SCH001", "req-123"))
                .assertNext(response -> {
                    assertEquals(500L, response.assignmentId());
                    assertEquals(1L, response.staffId());
                })
                .verifyComplete();

        verify(staffRepository).findByStaffCodeAndSchoolCodeAndIsDeletedFalse("ST001", "SCH001");
        verify(staffAssignmentRepository).save(any());
        verify(outboxRepository).save(any());
    }

    @Test
    void assignStaffToClass_NonTeachingRole_Success() {
        request = new CreateStaffAssignmentRequest(
                100L, 200L, 0L, AssignmentRole.CAREGIVER, "2023/2024"
        );
        assignment.setAssignmentRole(AssignmentRole.CAREGIVER.name());
        staff.setStaffCategory(StaffCategory.NON_TEACHING);

        when(staffRepository.findByStaffCodeAndSchoolCodeAndIsDeletedFalse("ST001", "SCH001"))
                .thenReturn(Mono.just(staff));
        when(staffAssignmentRepository.findByStaffIdAndClassIdAndSubjectIdAndAcademicYearAndIsDeletedFalse(
                1L, 100L, 0L, "2023/2024"))
                .thenReturn(Mono.empty());
        when(assignmentMapper.toEntity(any())).thenReturn(new StaffAssignment());
        when(staffAssignmentRepository.save(any())).thenReturn(Mono.just(assignment));
        when(outboxRepository.save(any())).thenReturn(Mono.just(new OutboxEvent()));
        when(objectMapper.valueToTree(any())).thenReturn(null);

        StepVerifier.create(staffAssignmentService.assignStaffToClass(request, "ST001", "SCH001", "req-123"))
                .assertNext(response -> {
                    assertEquals(500L, response.assignmentId());
                })
                .verifyComplete();
    }

    @Test
    void assignStaffToClass_StaffNotFound() {
        when(staffRepository.findByStaffCodeAndSchoolCodeAndIsDeletedFalse("ST001", "SCH001"))
                .thenReturn(Mono.empty());

        StepVerifier.create(staffAssignmentService.assignStaffToClass(request, "ST001", "SCH001", "req-123"))
                .expectErrorMatches(throwable -> throwable instanceof ResourceNotFoundException &&
                        throwable.getMessage().equals("Staff not found"))
                .verify();
    }

    @Test
    void assignStaffToClass_NonTeachingStaff_TeachingRole_Failure() {
        staff.setStaffCategory(StaffCategory.NON_TEACHING);
        when(staffRepository.findByStaffCodeAndSchoolCodeAndIsDeletedFalse("ST001", "SCH001"))
                .thenReturn(Mono.just(staff));

        StepVerifier.create(staffAssignmentService.assignStaffToClass(request, "ST001", "SCH001", "req-123"))
                .expectErrorMatches(throwable -> throwable instanceof ValidationException &&
                        throwable.getMessage().equals("Non-teaching staff can not be assigned teaching roles"))
                .verify();
    }

    @Test
    void assignStaffToClass_NoQualification_Failure() {
        when(staffRepository.findByStaffCodeAndSchoolCodeAndIsDeletedFalse("ST001", "SCH001"))
                .thenReturn(Mono.just(staff));
        when(staffTeachingQualificationRepository.staffHasQualification(1L))
                .thenReturn(Mono.just(false));

        StepVerifier.create(staffAssignmentService.assignStaffToClass(request, "ST001", "SCH001", "req-123"))
                .expectErrorMatches(throwable -> throwable instanceof ValidationException &&
                        throwable.getMessage().equals("Staff does not have a valid teaching qualification"))
                .verify();
    }

    @Test
    void assignStaffToClass_AlreadyAssigned_Failure() {
        when(staffRepository.findByStaffCodeAndSchoolCodeAndIsDeletedFalse("ST001", "SCH001"))
                .thenReturn(Mono.just(staff));
        when(staffTeachingQualificationRepository.staffHasQualification(1L))
                .thenReturn(Mono.just(true));
        when(staffAssignmentRepository.findByStaffIdAndClassIdAndSubjectIdAndAcademicYearAndIsDeletedFalse(
                1L, 100L, 300L, "2023/2024"))
                .thenReturn(Mono.just(assignment));
        // Mocking addNewStaffAssignment to return a valid Mono (even though it's not called)
        // Since we're using Mockito, we can't easily mock the private method,
        // but we can mock staffAssignmentRepository.save which is what it calls.
        lenient().when(staffAssignmentRepository.save(any())).thenReturn(Mono.just(assignment));
        lenient().when(assignmentMapper.toEntity(any())).thenReturn(assignment);

        StepVerifier.create(staffAssignmentService.assignStaffToClass(request, "ST001", "SCH001", "req-123"))
                .expectErrorMatches(throwable -> throwable instanceof ValidationException &&
                        throwable.getMessage().contains("Staff is already assigned"))
                .verify();
    }

    @Test
    void viewStaffAssignments_Success() {
        when(staffRepository.findByStaffCodeAndSchoolCodeAndIsDeletedFalse("ST001", "SCH001"))
                .thenReturn(Mono.just(staff));
        when(staffAssignmentRepository.findByStaffIdAndIsDeletedFalse(1L))
                .thenReturn(Flux.just(assignment));

        StepVerifier.create(staffAssignmentService.viewStaffAssignments("ST001", "SCH001"))
                .assertNext(response -> {
                    assertEquals(500L, response.assignmentId());
                })
                .verifyComplete();
    }

    @Test
    void viewStaffAssignmentById_Success() {
        when(staffAssignmentRepository.findById(500L))
                .thenReturn(Mono.just(assignment));

        StepVerifier.create(staffAssignmentService.viewStaffAssignmentById(500L))
                .assertNext(response -> {
                    assertEquals(500L, response.assignmentId());
                })
                .verifyComplete();
    }
}
