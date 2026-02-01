package com.emis.hrservice.repository;

import com.emis.hrservice.domain.db.StaffAttendanceAudit;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface StaffAttendanceAuditRepository extends ReactiveCrudRepository<StaffAttendanceAudit, Long> {}
