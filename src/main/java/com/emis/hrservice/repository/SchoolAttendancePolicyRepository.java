package com.emis.hrservice.repository;

import com.emis.hrservice.domain.db.AttendancePolicy;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SchoolAttendancePolicyRepository extends ReactiveCrudRepository<AttendancePolicy, Long>{

    Mono<AttendancePolicy> findBySchoolCode(String schoolCode);

    @Query("SELECT * FROM school_attendance_policy WHERE status = 'active' AND is_deleted = FALSE")
    Flux<AttendancePolicy> findAllActive();
}
