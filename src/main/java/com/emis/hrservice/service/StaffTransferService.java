package com.emis.hrservice.service;

import com.emis.hrservice.dto.request.StaffTransferRequest;
import com.emis.hrservice.dto.response.StaffTransferResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface StaffTransferService {

    Mono<StaffTransferResponse> transferStaff(String staffCode, StaffTransferRequest request, String requestId);

    Flux<StaffTransferResponse> getStaffServiceHistory(Long staffId);
}
