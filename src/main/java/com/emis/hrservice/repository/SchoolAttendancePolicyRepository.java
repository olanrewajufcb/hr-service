package com.emis.hrservice.repository;

import com.emis.hrservice.domain.db.AttendancePolicy;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SchoolAttendancePolicyRepository extends ReactiveCrudRepository<AttendancePolicy, Long>{

    Mono<AttendancePolicy> findBySchoolCode(String schoolCode);

    Flux<AttendancePolicy> findAllActive();
}
