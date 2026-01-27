package com.emis.hrservice.service;

import com.emis.hrservice.dto.request.CreateStaffRequest;
import com.emis.hrservice.dto.request.UpdateEmergencyContactRequest;
import com.emis.hrservice.dto.request.UpdateStaffBioRequest;
import com.emis.hrservice.dto.response.CreateStaffResponse;
import com.emis.hrservice.dto.response.EmergencyContactResponse;
import com.emis.hrservice.dto.response.UpdateStaffBioResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

public interface StaffManagementService {

    Mono<CreateStaffResponse> createStaff(CreateStaffRequest request, String requestId);

    Mono<CreateStaffResponse> retrieveStaff(String schoolCode, String staffCode, String requestId);

    Mono<CreateStaffResponse> retrieveStaffById(Long staffId, String requestId);

    Mono<Page<CreateStaffResponse>> retrieveStaffsBySchoolCode(String schoolCode, Pageable pageable, String requestId);

    Mono<UpdateStaffBioResponse> updateStaffBio(String staffCode, UpdateStaffBioRequest request, String requestId);

    Mono<EmergencyContactResponse> updateStaffEmergencyContact(String staffCode, UpdateEmergencyContactRequest request, String requestId);
}
