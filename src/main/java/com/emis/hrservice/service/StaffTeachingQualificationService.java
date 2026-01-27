package com.emis.hrservice.service;

import com.emis.hrservice.dto.request.AddStaffTeachingQualificationRequest;
import com.emis.hrservice.dto.response.StaffTeachingQualificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

public interface StaffTeachingQualificationService {

    Mono<StaffTeachingQualificationResponse> addStaffTeachingQualification(
            String staffCode, String schoolCode, AddStaffTeachingQualificationRequest request, String requestId);

    Mono<Page<StaffTeachingQualificationResponse>> retrieveStaffTeachingQualifications(
            String staffCode, String schoolCode,
            Pageable pageable, String requestId);

}
