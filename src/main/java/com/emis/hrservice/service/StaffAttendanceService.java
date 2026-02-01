package com.emis.hrservice.service;

import com.emis.hrservice.dto.request.BulkAttendanceConfirmationRequest;
import com.emis.hrservice.dto.request.StaffAttendanceRequest;
import com.emis.hrservice.dto.request.StaffCheckInRequest;
import com.emis.hrservice.dto.response.BulkAttendanceConfirmationResponse;
import com.emis.hrservice.dto.response.StaffAttendanceResponse;
import com.emis.hrservice.dto.response.StaffCheckInResponse;
import reactor.core.publisher.Mono;

public interface StaffAttendanceService {
    Mono<StaffCheckInResponse> checkInStaff(StaffCheckInRequest request, String requestId);

    Mono<StaffAttendanceResponse> markStaffAttendance(StaffAttendanceRequest request,
                                                      String schoolCode, String requestId);

    Mono<BulkAttendanceConfirmationResponse> confirmBulkAttendance(BulkAttendanceConfirmationRequest request,
                                                                   String schoolCode, String requestId);
}
