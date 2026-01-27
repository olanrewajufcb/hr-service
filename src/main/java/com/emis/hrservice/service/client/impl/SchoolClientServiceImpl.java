package com.emis.hrservice.service.client.impl;

import java.util.concurrent.ConcurrentHashMap;

import com.emis.hrservice.config.ServiceConfigurationProperties;
import com.emis.hrservice.dto.response.SchoolDetailsResponse;
import com.emis.hrservice.exceptions.SchoolNotFoundException;
import com.emis.hrservice.exceptions.SchoolServiceException;
import com.emis.hrservice.exceptions.SchoolServiceUnavailableException;
import com.emis.hrservice.service.client.SchoolClientService;
import com.emis.hrservice.utils.ClientHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class SchoolClientServiceImpl implements SchoolClientService {

    private final ClientHelper client;
    private final ServiceConfigurationProperties properties;

    @Override
    public Mono<SchoolDetailsResponse> getSchoolDetails(String schoolCode) {
        var url = properties.getSchoolServiceProperties().getGetSchoolDetailsUrl();
        var pathVariable = new ConcurrentHashMap<String, String>();
        pathVariable.put("schoolCode", schoolCode);
    return client
        .getRequestWithPathVariables(
            url, pathVariable, ClientHelper.getHeaders(), SchoolDetailsResponse.class)
        .map(
            response -> {
              log.info("School Details Response: {}", response);
              return response;
            })
        .onErrorMap(
            err -> {
              log.error(
                  "Exception occurred while trying to get school details for schoolId: {}",
                  schoolCode,
                  err);
              if (err instanceof WebClientResponseException.NotFound ex) {
                return new SchoolNotFoundException(
                    "School not found: " + schoolCode + ex.getMessage());
              } else if (err instanceof WebClientResponseException.ServiceUnavailable ex) {
                log.error("School service unavailable: {}", schoolCode, ex);
                return new SchoolServiceUnavailableException(
                    "School service error: " + ex.getStatusCode(), ex.getResponseBodyAsString());
              }

              return new SchoolServiceException("School service error: ", err);
            });
    }

    @Override
    public Mono<Boolean> validateSchoolExists(Long schoolId) {
        var url = properties.getSchoolServiceProperties().getValidateSchoolExistsUrl();
        var pathVariable = new ConcurrentHashMap<String, Long>();
        pathVariable.put("schoolId", schoolId);
        return client
                .getRequestWithPathVariables(
                        url, pathVariable, ClientHelper.getHeaders(), Boolean.class)
                .map(
                        response -> {
                            log.info("School Details Response: {}", response);
                            return response;
                        })
             .onErrorMap(err -> {
                 log.error("Error validating school existence for schoolId: {}", schoolId, err);
                 if (err instanceof WebClientResponseException && ((WebClientResponseException) err).getStatusCode().is4xxClientError()){
                     return new SchoolServiceUnavailableException(
                         "School service unavailable: " +
                                 ((WebClientResponseException) err).getStatusCode(), ((WebClientResponseException) err).getResponseBodyAsString());
                 }
               return new SchoolServiceException("School service error :::: " + schoolId,  err);
             });
    }

    @Override
    public Mono<Boolean> validateSchoolExistsByCode(String schoolCode) {
        var url = properties.getSchoolServiceProperties().getValidateSchoolExistsUrl();
        var pathVariable = new ConcurrentHashMap<String, String>();
        pathVariable.put("schoolCode", schoolCode);
    return client
        .getRequestWithPathVariables(url, pathVariable, ClientHelper.getHeaders(), Boolean.class)
        .map(
            response -> {
              log.info("School Details Response: {}", response);
              return response;
            })
        .onErrorMap(
            err -> {
              log.error("Error validating school existence for schoolId: {}", schoolCode, err);
              if (err instanceof WebClientResponseException ex){
                  return new SchoolServiceUnavailableException(
                      "School service error: " +
                              ex.getStatusCode(), ex.getResponseBodyAsString());
              }
              return new SchoolServiceException(
                  "School service error ::: " + schoolCode , err);
            });
    }
}
