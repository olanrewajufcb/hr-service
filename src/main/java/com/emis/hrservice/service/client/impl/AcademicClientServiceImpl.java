package com.emis.hrservice.service.client.impl;

import com.emis.hrservice.config.ServiceConfigurationProperties;
import com.emis.hrservice.domain.db.Staff;
import com.emis.hrservice.dto.request.StaffUpdateRequest;
import com.emis.hrservice.dto.response.ClassSectionResponse;
import com.emis.hrservice.exceptions.AcademicServiceException;
import com.emis.hrservice.exceptions.ResourceNotFoundException;
import com.emis.hrservice.exceptions.SchoolServiceUnavailableException;
import com.emis.hrservice.service.client.AcademicClientService;
import com.emis.hrservice.utils.ClientHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class AcademicClientServiceImpl implements AcademicClientService {

    private final ClientHelper client;
    private final ServiceConfigurationProperties properties;
    @Override
    public Mono<ClassSectionResponse> getClassSectionDetails(String schoolCode, Long classId,
                                                             Long sectionId, String staffCode) {
        var url = properties.getAcademicServiceProperties().getGetClassSectionUrl();
        var pathVariable = new ConcurrentHashMap<String, String>();
        var queryParams = new ConcurrentHashMap<String, String>();
        queryParams.put("staffCode", staffCode);
        pathVariable.put("schoolCode", schoolCode);
        pathVariable.put("classId", classId.toString());
        pathVariable.put("sectionId", sectionId.toString());
        return client
                .getRequestWithParameters(
                        url, pathVariable, queryParams, ClientHelper.getHeaders(), ClassSectionResponse.class)
                .map(
                        response -> {
                            log.info("class section Details Response: {}", response);
                            return response;
                        })
                .onErrorMap(
                        err -> {
                            log.error(
                                    "Exception occurred while trying to get class section details for classId: {}",
                                    sectionId,
                                    err);
                            if (err instanceof WebClientResponseException.NotFound ex) {
                                return new ResourceNotFoundException(
                                        "class section not found: " + sectionId + ex.getMessage());
                            } else if (err instanceof WebClientResponseException.ServiceUnavailable ex) {
                                log.error("Academic service unavailable: {}", schoolCode, ex);
                                return new SchoolServiceUnavailableException(
                                        "Academic service error: " + ex.getStatusCode(), ex.getResponseBodyAsString());
                            }

                            return new AcademicServiceException("Academic service error: ", err);
                        });
    }

    @Override
    public Mono<ClassSectionResponse> updateClassSectionWithTeacherInfo(
            String schoolCode, Long classId, Long sectionId, Staff staff) {
        var url = properties.getAcademicServiceProperties().getGetClassSectionUrl();
        var pathVariable = new ConcurrentHashMap<String, String>();
        StaffUpdateRequest staffUpdateRequest = new StaffUpdateRequest(
                staff.getStaffId(),
                staff.getStaffCode(),
                staff.getFullName());

        pathVariable.put("schoolCode", schoolCode);
        pathVariable.put("classId", classId.toString());
        pathVariable.put("sectionId", sectionId.toString());
        return client
                .putRequest(
                        url, staffUpdateRequest, pathVariable, ClientHelper.getHeaders(), ClassSectionResponse.class)
                .map(
                        response -> {
                            log.info("class section Details Response: {}", response);
                            return response;
                        })
                .onErrorMap(
                        err -> {
                            log.error(
                                    "Exception occurred while trying to get class section details for classId: {}",
                                    sectionId,
                                    err);
                            if (err instanceof WebClientResponseException.NotFound ex) {
                                return new ResourceNotFoundException(
                                        "class section not found: " + sectionId + ex.getMessage());
                            } else if (err instanceof WebClientResponseException.ServiceUnavailable ex) {
                                log.error("Academic service unavailable: {}", schoolCode, ex);
                                return new SchoolServiceUnavailableException(
                                        "Academic service error: " + ex.getStatusCode(), ex.getResponseBodyAsString());
                            }

                            return new AcademicServiceException("Academic service error: ", err);
                        });
    }
}
