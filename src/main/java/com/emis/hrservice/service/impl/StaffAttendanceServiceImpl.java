package com.emis.hrservice.service.impl;

import com.emis.hrservice.domain.db.AttendancePolicy;
import com.emis.hrservice.domain.db.Staff;
import com.emis.hrservice.domain.db.StaffAttendance;
import com.emis.hrservice.domain.db.StaffAttendanceAudit;
import com.emis.hrservice.dto.request.*;
import com.emis.hrservice.dto.response.AttendanceConfirmationResult;
import com.emis.hrservice.dto.response.BulkAttendanceConfirmationResponse;
import com.emis.hrservice.dto.response.StaffAttendanceResponse;
import com.emis.hrservice.dto.response.StaffCheckInResponse;
import com.emis.hrservice.enums.AttendanceStatus;
import com.emis.hrservice.exceptions.ResourceAlreadyExistsException;
import com.emis.hrservice.exceptions.ResourceNotFoundException;
import com.emis.hrservice.exceptions.ValidationException;
import com.emis.hrservice.repository.SchoolAttendancePolicyRepository;
import com.emis.hrservice.repository.StaffAttendanceAuditRepository;
import com.emis.hrservice.repository.StaffAttendanceRepository;
import com.emis.hrservice.repository.StaffRepository;
import com.emis.hrservice.service.StaffAttendanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.AuthorizationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaffAttendanceServiceImpl implements StaffAttendanceService {

    private final StaffAttendanceRepository staffAttendanceRepository;
    private final StaffRepository staffRepository;
    private final TransactionalOperator transactionalOperator;
    private final SchoolAttendancePolicyRepository schoolAttendancePolicyRepository;
    private final StaffAttendanceAuditRepository staffAttendanceAuditRepository;
    private static final String STAFF_NOT_CHECKED_IN = "Staff did not check in";
    @Override
    public Mono<StaffCheckInResponse> checkInStaff(StaffCheckInRequest request, String requestId) {

        log.info("Checking in staff with request: {}", request);
        return Mono.deferContextual(ctx -> {
            AuditContext audit = ctx.get("audit");
    return staffRepository
        .findById(request.staffId())
        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Staff not found")))
        .flatMap(
            staff ->
                staffAttendanceRepository
                    .findByStaffIdAndSchoolIdAndAttendanceDateAndIsDeletedFalse(
                        staff.getStaffId(), staff.getSchoolId(), request.attendanceDate())
                        .flatMap(existing -> {
                            // Explicit: error if already checked in
                            if (existing.getCheckInTime() != null) {
                                return Mono.error(new ResourceAlreadyExistsException("Staff already checked in"));
                            }
                            // if we found a row without checkInTime (edge-case), set it
                            existing.setCheckInTime(request.checkInTime());
                            existing.setNotes(request.notes());
                            existing.setAttendanceStatus(AttendanceStatus.CHECKED_IN.name());
                            return staffAttendanceRepository.save(existing)
                                    .as(transactionalOperator::transactional)
                                    .map(StaffCheckInResponse::from);
                        })
                    .switchIfEmpty(createCheckIn(staff, request, audit))
    );
        });
    }

    @Override
    public Mono<StaffAttendanceResponse> markStaffAttendance(StaffAttendanceRequest request,
                                  String schoolCode, String requestId) {
        return Mono.deferContextual(ctx -> {
            AuditContext audit = ctx.get("audit");

                return staffRepository.findByStaffCodeAndSchoolCodeAndIsDeletedFalse(request.confirmerStaffCode(), schoolCode)
                .switchIfEmpty(Mono.error(new AuthorizationException("Confirmer not found")))
                .flatMap(confirmer -> {
                    // TODO: check confirmer role e.g. HEAD_TEACHER or SUPERVISOR
                    return staffRepository.findByStaffCodeAndSchoolCodeAndIsDeletedFalse(request.staffCode(), schoolCode)
                            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Target staff not found")))
                            .flatMap(targetStaff ->
                                    staffAttendanceRepository.findByStaffIdAndSchoolIdAndAttendanceDateAndIsDeletedFalse(
                                                    targetStaff.getStaffId(), targetStaff.getSchoolId(), request.attendanceDate())
                                            .switchIfEmpty(Mono.error(new ValidationException(STAFF_NOT_CHECKED_IN)))
                                            .flatMap(attendance -> confirmAttendance(attendance, request, audit))
                            );
                });
   })
                .map(StaffAttendanceResponse::from);
    }

    @Override
    public Mono<BulkAttendanceConfirmationResponse> confirmBulkAttendance(
            BulkAttendanceConfirmationRequest request,
            String schoolCode,
            String requestId
    ) {

        return validateConfirmer(request.confirmedByStaffCode(), schoolCode)
                .flatMapMany(schoolId -> Flux.fromIterable(request.confirmations())
                .flatMap(item ->
                        confirmSingleAttendance(item, request, schoolId))
                )
                .collectList()
                .map(results ->
                        BulkAttendanceConfirmationResponse.from(results, requestId));
    }

    private Mono<AttendanceConfirmationResult> confirmSingleAttendance(
            BulkStaffAttendanceConfirmation item,
            BulkAttendanceConfirmationRequest request,
            Long schoolId
    ) {
        return staffAttendanceRepository
                .findById(item.attendanceId())
                .filter(att -> att.getSchoolId().equals(schoolId))
                .filter(att -> att.getAttendanceDate().equals(request.attendanceDate()))
                .filter(att -> att.getStaffId().equals(item.staffCode()))
                .flatMap(attendance -> {

                    if (AttendanceStatus.PRESENT.name().equals(attendance.getAttendanceStatus())) {
                        return Mono.just(AttendanceConfirmationResult
                                .skipped(attendance.getAttendanceId(),  "Already confirmed"));
                    }
                    if (attendance.getCheckInTime() == null){
                        return Mono.just(AttendanceConfirmationResult
                                .skipped(attendance.getAttendanceId(), STAFF_NOT_CHECKED_IN));
                    }

                    attendance.setAttendanceStatus(item.status().name());
                    attendance.setConfirmedBy(request.confirmedByStaffCode());
                    attendance.setConfirmedAt(LocalDateTime.now());
                    attendance.setNotes(item.notes());

                    return staffAttendanceRepository.save(attendance)
                            .then(saveAudit(attendance, item))
                            .thenReturn(AttendanceConfirmationResult.confirmed(attendance.getAttendanceId()));
                })
                .switchIfEmpty(
                        Mono.just(AttendanceConfirmationResult
                                .failed(item.attendanceId(), "Invalid attendance record"))
                );
    }

    private Mono<Void> saveAudit(
            StaffAttendance attendance,
            BulkStaffAttendanceConfirmation item
    ) {
        return staffAttendanceAuditRepository.save(
                StaffAttendanceAudit.builder()
                        .attendanceId(attendance.getAttendanceId())
                        .previousStatus(attendance.getAttendanceStatus())
                        .newStatus(item.status().name())
                        .changedBy(attendance.getConfirmedBy())
                        .changedAt(LocalDateTime.now())
                        .reason(item.notes())
                        .build()
        ).then();
    }


    //validate confirmer
    private Mono<Long> validateConfirmer(String confirmerStaffCode, String schoolCode) {
        return staffRepository.findByStaffCodeAndSchoolCodeAndIsDeletedFalse(confirmerStaffCode, schoolCode)
                .switchIfEmpty(Mono.error(new AuthorizationException("Confirmer not found")))
                .map(Staff::getSchoolId);
    }



    private Mono<Void> saveAttendanceAudit(StaffAttendance attendance,
                     String previous, String current, AuditContext auditContext, String reason) {

        return staffAttendanceAuditRepository.save(StaffAttendanceAudit.builder()
                .attendanceId(attendance.getAttendanceId())
                .previousStatus(previous)
                .newStatus(current)
                .changedBy(auditContext.userId())
                .reason(reason)
                .changedAt(LocalDateTime.now())
                .build())
                .then();
    }
    private Mono<StaffCheckInResponse> createCheckIn(
            Staff staff, StaffCheckInRequest request, AuditContext audit) {

            return schoolAttendancePolicyRepository
                    .findBySchoolCode(staff.getSchoolCode())
                    .switchIfEmpty(Mono.error(new ResourceNotFoundException("Attendance policy not configured for school")))
                    .flatMap(
                            policy -> {
                                if (request
                                        .checkInTime()
                                        .isBefore(policy.getCheckInTime())
                                        || request
                                        .checkInTime()
                                        .isAfter(policy.getCutOffTime())) {
                                    return Mono.error(
                                            new ValidationException(
                                                    "Check-in time outside allowed window"));
                                }
                                StaffAttendance attendance = new  StaffAttendance();
                                attendance.setAttendanceStatus(AttendanceStatus.CHECKED_IN.name());
                                attendance.setStaffId(staff.getStaffId());
                                attendance.setCheckInTime(request.checkInTime());
                                attendance.setAttendanceDate(request.attendanceDate());
                                attendance.setCheckInBy(audit.userId());
                                attendance.setCheckedInAt(LocalDate.now());
                                attendance.setSource(audit.source());

                                String newStatus = request.checkInTime().isAfter(policy.getCheckInTime())
                                        ? AttendanceStatus.LATE.name()
                                        : AttendanceStatus.PENDING_CONFIRMATION.name();

                                attendance.setAttendanceStatus(newStatus);
                                attendance.setNotes(request.notes());

                                return staffAttendanceRepository.save(attendance)
                                        .flatMap(saved ->
                                                saveAttendanceAudit(
                                                        saved,
                                                null,
                                                        newStatus,
                                                        audit,
                                                "staff check-in")
                                        .thenReturn(saved));
                            })
                    .as(transactionalOperator::transactional)
                    .map(StaffCheckInResponse::from);

  }

  private Mono<StaffAttendance> confirmAttendance(StaffAttendance attendance,
                       StaffAttendanceRequest request, AuditContext audit) {

      if (attendance.getCheckInTime() == null) {
          return Mono.error(new ValidationException(STAFF_NOT_CHECKED_IN));
      }

      if (AttendanceStatus.PRESENT.name().equals(attendance.getAttendanceStatus())) {
          return Mono.error(new ResourceAlreadyExistsException("Attendance already confirmed"));
      }
      String previousStatus = attendance.getAttendanceStatus();
      String newStatus =  AttendanceStatus.LATE.name().equals(previousStatus)
                      ? AttendanceStatus.LATE.name()
                      : AttendanceStatus.PRESENT.name();
      attendance.setAttendanceStatus(newStatus);
      attendance.setConfirmedBy(audit.userId());
      attendance.setConfirmedByRole(audit.role());
      attendance.setConfirmedAt(LocalDateTime.now());
      attendance.setNotes(request.notes());

      return staffAttendanceRepository.save(attendance)
              .flatMap(saved ->
                      saveAttendanceAudit(
                              saved,
                              previousStatus,
                              newStatus,
                              audit,
                              "staff attendance confirmed")
              .thenReturn(saved))
              .as(transactionalOperator::transactional);

  }

    public Mono<Void> finalizeAttendance(LocalDate date) {
        return schoolAttendancePolicyRepository
                .findAllActive()
                .flatMap(policy -> finalizeSchool(policy, date))
                .then();
    }
    private Mono<Void> finalizeSchool(
            AttendancePolicy policy,
            LocalDate date
    ) {
        return staffRepository
                .findActiveStaffBySchoolCode(policy.getSchoolCode())
                .flatMap(staff -> finalizeStaff(staff, date))
                .then();
    }
    private Mono<Void> finalizeStaff(
            Staff staff,
            LocalDate date
    ) {
        return staffAttendanceRepository
                .findByStaffIdAndAttendanceDateAndIsDeletedFalse(staff.getStaffId(), date)
                .hasElement()  // Check if element exists
                .flatMap(exists -> {
                    if (exists) {
                        // If attendance exists, find and process it
                        return staffAttendanceRepository
                                .findByStaffIdAndAttendanceDateAndIsDeletedFalse(staff.getStaffId(), date)
                                .flatMap(this::finalizeExistingAttendance);
                    } else {
                        // If no attendance exists, create absent record
                        return createAbsentAttendance(staff, date);
                    }
                });
    }

    private Mono<Void> finalizeExistingAttendance(StaffAttendance att) {

        if (att.getFinalizedAt() != null) {
            return Mono.empty(); // already finalized
        }
        if (AttendanceStatus.PRESENT.name().equalsIgnoreCase(att.getAttendanceStatus())
                || AttendanceStatus.LATE.name().equalsIgnoreCase(att.getAttendanceStatus())
                || AttendanceStatus.LEAVE.name().equalsIgnoreCase(att.getAttendanceStatus())) {
            return Mono.empty();
        }
        String previous = att.getAttendanceStatus();
        att.setAttendanceStatus(AttendanceStatus.ABSENT.name());
        att.setFinalizedAt(LocalDateTime.now());

        return staffAttendanceRepository.save(att)
                .then(saveAttendanceAudit(att, previous,
                        att.getAttendanceStatus(),
                        new AuditContext("",  "System",
                                "attendance finalized by the system"), ""));
    }
    private Mono<Void> createAbsentAttendance(Staff staff, LocalDate date) {

        StaffAttendance absent = StaffAttendance.builder()
                .staffId(staff.getStaffId())
                .schoolId(staff.getSchoolId())
                .attendanceDate(date)
                .attendanceStatus(AttendanceStatus.ABSENT.name())
                .finalizedAt(LocalDateTime.now())
                .build();
        return staffAttendanceRepository.save(absent)
                .then(saveAttendanceAudit(absent, "Absent", absent.getAttendanceStatus(),
                        new AuditContext("",  "System","system"), ""));
    }

    @Scheduled(cron = "0 0 12 * * ?")
    public void finalizeTodayAttendance() {
        finalizeAttendance(LocalDate.now()).subscribe();
    }
}
