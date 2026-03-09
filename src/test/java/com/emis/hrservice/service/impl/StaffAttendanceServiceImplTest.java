package com.emis.hrservice.service.impl;

import com.emis.hrservice.domain.db.AttendancePolicy;
import com.emis.hrservice.domain.db.Staff;
import com.emis.hrservice.domain.db.StaffAttendance;
import com.emis.hrservice.domain.db.StaffAttendanceAudit;
import com.emis.hrservice.dto.request.*;
import com.emis.hrservice.enums.AttendanceStatus;
import com.emis.hrservice.exceptions.ResourceAlreadyExistsException;
import com.emis.hrservice.exceptions.ResourceNotFoundException;
import com.emis.hrservice.exceptions.ValidationException;
import com.emis.hrservice.repository.SchoolAttendancePolicyRepository;
import com.emis.hrservice.repository.StaffAttendanceAuditRepository;
import com.emis.hrservice.repository.StaffAttendanceRepository;
import com.emis.hrservice.repository.StaffRepository;
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
import reactor.util.context.Context;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaffAttendanceServiceImplTest {

    @Mock
    private StaffAttendanceRepository staffAttendanceRepository;
    @Mock
    private StaffRepository staffRepository;
    @Mock
    private TransactionalOperator transactionalOperator;
    @Mock
    private SchoolAttendancePolicyRepository schoolAttendancePolicyRepository;
    @Mock
    private StaffAttendanceAuditRepository staffAttendanceAuditRepository;

    @InjectMocks
    private StaffAttendanceServiceImpl staffAttendanceService;

    private final AuditContext auditContext = new AuditContext(1L, "ADMIN", "WEB");

    @BeforeEach
    void setUp() {
        lenient().when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(transactionalOperator.transactional(any(Flux.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void checkInStaff_NewCheckIn_Success() {
        StaffCheckInRequest request = new StaffCheckInRequest(
                1L, "ST001", "SCH001", LocalDate.now(), LocalTime.of(9, 30), "Morning check-in"
        );
        Staff staff = new Staff();
        staff.setStaffId(1L);
        staff.setSchoolId(10L);
        staff.setSchoolCode("SCH001");

        AttendancePolicy policy = AttendancePolicy.builder()
                .checkInTime(LocalTime.of(9, 0))
                .cutOffTime(LocalTime.of(17, 0))
                .schoolCode("SCH001")
                .build();

        when(staffRepository.findById(1L)).thenReturn(Mono.just(staff));
        when(staffAttendanceRepository.findByStaffIdAndSchoolIdAndAttendanceDateAndIsDeletedFalse(
                1L, 10L, request.attendanceDate())).thenReturn(Mono.empty());
        when(schoolAttendancePolicyRepository.findBySchoolCode("SCH001")).thenReturn(Mono.just(policy));
        when(staffAttendanceRepository.save(any(StaffAttendance.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        when(staffAttendanceAuditRepository.save(any(StaffAttendanceAudit.class))).thenReturn(Mono.just(new StaffAttendanceAudit()));

        staffAttendanceService.checkInStaff(request, "req-1")
                .contextWrite(Context.of("audit", auditContext))
                .as(StepVerifier::create)
                .expectNextMatches(response -> response.staffId().equals(1L))
                .verifyComplete();

        verify(staffAttendanceRepository).save(argThat(a -> 
                a.getAttendanceStatus().equals(AttendanceStatus.LATE.name()) &&
                a.getCheckInTime().equals(LocalTime.of(9, 30))
        ));
    }

    @Test
    void checkInStaff_AlreadyCheckedIn_Error() {
        StaffCheckInRequest request = new StaffCheckInRequest(
                1L, "ST001", "SCH001", LocalDate.now(), LocalTime.of(8, 30), "Morning check-in"
        );
        Staff staff = new Staff();
        staff.setStaffId(1L);
        staff.setSchoolId(10L);
        staff.setSchoolCode("SCH001");

        StaffAttendance existing = new StaffAttendance();
        existing.setCheckInTime(LocalTime.of(8, 0));

        AttendancePolicy policy = AttendancePolicy.builder().schoolCode("SCH001").build();

        when(staffRepository.findById(1L)).thenReturn(Mono.just(staff));
        when(staffAttendanceRepository.findByStaffIdAndSchoolIdAndAttendanceDateAndIsDeletedFalse(
                1L, 10L, request.attendanceDate())).thenReturn(Mono.just(existing));
        when(schoolAttendancePolicyRepository.findBySchoolCode("SCH001")).thenReturn(Mono.just(policy));

        staffAttendanceService.checkInStaff(request, "req-1")
                .contextWrite(Context.of("audit", auditContext))
                .as(StepVerifier::create)
                .expectError(ResourceAlreadyExistsException.class)
                .verify();
    }

    @Test
    void checkInStaff_LateCheckIn_Success() {
        StaffCheckInRequest request = new StaffCheckInRequest(
                1L, "ST001", "SCH001", LocalDate.now(), LocalTime.of(9, 30), "Late check-in"
        );
        Staff staff = new Staff();
        staff.setStaffId(1L);
        staff.setSchoolId(10L);
        staff.setSchoolCode("SCH001");

        AttendancePolicy policy = AttendancePolicy.builder()
                .checkInTime(LocalTime.of(9, 0))
                .cutOffTime(LocalTime.of(17, 0))
                .schoolCode("SCH001")
                .build();

        when(staffRepository.findById(1L)).thenReturn(Mono.just(staff));
        when(staffAttendanceRepository.findByStaffIdAndSchoolIdAndAttendanceDateAndIsDeletedFalse(
                1L, 10L, request.attendanceDate())).thenReturn(Mono.empty());
        when(schoolAttendancePolicyRepository.findBySchoolCode("SCH001")).thenReturn(Mono.just(policy));
        when(staffAttendanceRepository.save(any(StaffAttendance.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        when(staffAttendanceAuditRepository.save(any(StaffAttendanceAudit.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        staffAttendanceService.checkInStaff(request, "req-1")
                .contextWrite(Context.of("audit", auditContext))
                .as(StepVerifier::create)
                .expectNextMatches(response -> response.staffId().equals(1L))
                .verifyComplete();

        verify(staffAttendanceRepository).save(argThat(a ->
                a.getAttendanceStatus().equals(AttendanceStatus.LATE.name())
        ));
    }

    @Test
    void checkInStaff_OutsideWindow_Error() {
        StaffCheckInRequest request = new StaffCheckInRequest(
                1L, "ST001", "SCH001", LocalDate.now(), LocalTime.of(18, 0), "Too late"
        );
        Staff staff = new Staff();
        staff.setStaffId(1L);
        staff.setSchoolCode("SCH001");

        AttendancePolicy policy = AttendancePolicy.builder()
                .checkInTime(LocalTime.of(9, 0))
                .cutOffTime(LocalTime.of(17, 0))
                .schoolCode("SCH001")
                .build();

        when(staffRepository.findById(1L)).thenReturn(Mono.just(staff));
        when(staffAttendanceRepository.findByStaffIdAndSchoolIdAndAttendanceDateAndIsDeletedFalse(
                eq(1L), any(), eq(request.attendanceDate()))).thenReturn(Mono.empty());
        when(schoolAttendancePolicyRepository.findBySchoolCode("SCH001")).thenReturn(Mono.just(policy));

        staffAttendanceService.checkInStaff(request, "req-1")
                .contextWrite(Context.of("audit", auditContext))
                .as(StepVerifier::create)
                .expectError(ValidationException.class)
                .verify();
    }

    @Test
    void markStaffAttendance_Success() {
        StaffAttendanceRequest request = new StaffAttendanceRequest(
                "ST001", "CONF01", 100L, true, LocalDate.now(), "Confirmed", "Notes"
        );

        Staff confirmer = new Staff();
        confirmer.setStaffId(2L);
        confirmer.setStaffCode("CONF01");
        confirmer.setSchoolId(10L);

        Staff targetStaff = new Staff();
        targetStaff.setStaffId(1L);
        targetStaff.setStaffCode("ST001");
        targetStaff.setSchoolId(10L);

        StaffAttendance attendance = new StaffAttendance();
        attendance.setAttendanceId(100L);
        attendance.setStaffId(1L);
        attendance.setCheckInTime(LocalTime.of(8, 0));
        attendance.setAttendanceStatus(AttendanceStatus.PENDING_CONFIRMATION.name());

        when(staffRepository.findByStaffCodeAndSchoolCodeAndIsDeletedFalse("CONF01", "SCH001")).thenReturn(Mono.just(confirmer));
        when(staffRepository.findByStaffCodeAndSchoolCodeAndIsDeletedFalse("ST001", "SCH001")).thenReturn(Mono.just(targetStaff));
        when(staffAttendanceRepository.findByStaffIdAndSchoolIdAndAttendanceDateAndIsDeletedFalse(1L, 10L, request.attendanceDate()))
                .thenReturn(Mono.just(attendance));
        when(staffAttendanceRepository.save(any(StaffAttendance.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        when(staffAttendanceAuditRepository.save(any(StaffAttendanceAudit.class))).thenReturn(Mono.just(new StaffAttendanceAudit()));

        staffAttendanceService.markStaffAttendance(request, "SCH001", "req-1")
                .contextWrite(Context.of("audit", auditContext))
                .as(StepVerifier::create)
                .expectNextMatches(response -> response.attendanceStatus().equals(AttendanceStatus.PRESENT.name()))
                .verifyComplete();
    }

    @Test
    void markStaffAttendance_SelfConfirmation_Error() {
        StaffAttendanceRequest request = new StaffAttendanceRequest(
                "ST001", "ST001", 100L, true, LocalDate.now(), "Confirmed", "Notes"
        );

        staffAttendanceService.markStaffAttendance(request, "SCH001", "req-1")
                .as(StepVerifier::create)
                .expectErrorMatches(t -> t instanceof ValidationException && t.getMessage().contains("own attendance"))
                .verify();
    }

    @Test
    void confirmBulkAttendance_Success() {
        BulkStaffAttendanceConfirmation item1 = new BulkStaffAttendanceConfirmation(100L, "ST001", AttendanceStatus.PRESENT, "Note1");
        BulkAttendanceConfirmationRequest request = new BulkAttendanceConfirmationRequest(
                LocalDate.now(), "CONF01", true, List.of(item1)
        );

        Staff confirmer = new Staff();
        confirmer.setStaffId(2L);
        confirmer.setStaffCode("CONF01");
        confirmer.setSchoolId(10L);

        StaffAttendance attendance = new StaffAttendance();
        attendance.setAttendanceId(100L);
        attendance.setSchoolId(10L);
        attendance.setAttendanceDate(request.attendanceDate());
        attendance.setStaffCode("ST001");
        attendance.setCheckInTime(LocalTime.of(8, 0));
        attendance.setAttendanceStatus(AttendanceStatus.PENDING_CONFIRMATION.name());

        when(staffRepository.findByStaffCodeAndSchoolCodeAndIsDeletedFalse("CONF01", "SCH001")).thenReturn(Mono.just(confirmer));
        when(staffAttendanceRepository.findById(100L)).thenReturn(Mono.just(attendance));
        when(staffAttendanceRepository.save(any(StaffAttendance.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        when(staffAttendanceAuditRepository.save(any(StaffAttendanceAudit.class))).thenReturn(Mono.just(new StaffAttendanceAudit()));

        staffAttendanceService.confirmBulkAttendance(request, "SCH001", "req-1")
                .contextWrite(Context.of("audit", auditContext))
                .as(StepVerifier::create)
                .expectNextMatches(response -> response.confirmed() == 1)
                .verifyComplete();
    }

    @Test
    void finalizeTodayAttendance_Success() {
        AttendancePolicy policy = AttendancePolicy.builder()
                .schoolCode("SCH001")
                .build();
        Staff staff = new Staff();
        staff.setStaffId(1L);
        staff.setSchoolCode("SCH001");

        when(schoolAttendancePolicyRepository.findAllActive()).thenReturn(Flux.just(policy));
        when(staffRepository.findActiveStaffBySchoolCode("SCH001")).thenReturn(Flux.just(staff));
        when(staffAttendanceRepository.findByStaffIdAndAttendanceDateAndIsDeletedFalse(eq(1L), any(LocalDate.class)))
                .thenReturn(Mono.empty()); // No attendance recorded yet
        when(staffAttendanceRepository.save(any(StaffAttendance.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        staffAttendanceService.finalizeTodayAttendance();

        // Since it's a void method returning nothing but calling other reactive methods, 
        // we might want to test the underlying methods or wait a bit.
        // However, finalizeTodayAttendance just calls finalizeAttendance(LocalDate.now()).subscribe();
        // and finalizeAttendance returns Mono<Void>.
        // To properly test this, we should test finalizeAttendance(date)
    }

    @Test
    void finalizeAttendance_ProcessesStaff() {
        AttendancePolicy policy = AttendancePolicy.builder()
                .schoolCode("SCH001")
                .build();
        Staff staff = new Staff();
        staff.setStaffId(1L);
        staff.setSchoolCode("SCH001");

        when(schoolAttendancePolicyRepository.findAllActive()).thenReturn(Flux.just(policy));
        when(staffRepository.findActiveStaffBySchoolCode("SCH001")).thenReturn(Flux.just(staff));
        when(staffAttendanceRepository.findByStaffIdAndAttendanceDateAndIsDeletedFalse(eq(1L), any(LocalDate.class)))
                .thenReturn(Mono.empty());
        when(staffAttendanceRepository.save(any(StaffAttendance.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        when(staffAttendanceAuditRepository.save(any(StaffAttendanceAudit.class))).thenReturn(Mono.just(new StaffAttendanceAudit()));

        staffAttendanceService.finalizeAttendance(LocalDate.now())
                .as(StepVerifier::create)
                .verifyComplete();

        verify(staffAttendanceRepository).save(argThat(a -> 
                a.getAttendanceStatus().equals(AttendanceStatus.ABSENT.name())
        ));
    }
}
