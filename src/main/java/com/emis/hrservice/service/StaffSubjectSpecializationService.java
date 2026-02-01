package com.emis.hrservice.service;

import com.emis.hrservice.dto.request.SubjectSpecializationRequest;
import com.emis.hrservice.dto.response.SubjectSpecializationResponse;
import reactor.core.publisher.Mono;

public interface StaffSubjectSpecializationService {

    Mono<SubjectSpecializationResponse> addSubjectSpecialization(
            String staffCode, String schoolCode, SubjectSpecializationRequest request, String requestId);
}
