package com.emis.hrservice.service;

import com.emis.hrservice.dto.request.AddStaffAcademicQualificationRequest;
import com.emis.hrservice.dto.response.AddStaffAcademicQualificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

public interface StaffAcademicQualificationService {

    Mono<AddStaffAcademicQualificationResponse> addStaffAcademicQualification(String staffCode,
                                                                               String schoolCode,
                                 AddStaffAcademicQualificationRequest request, String requestId);

    Mono<Page<AddStaffAcademicQualificationResponse>> retrieveStaffAcademicQualification(
            String staffCode, String schoolCode, Pageable pageable, String requestId);
}
