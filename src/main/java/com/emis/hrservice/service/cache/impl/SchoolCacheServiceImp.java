package com.emis.hrservice.service.cache.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.emis.hrservice.dto.response.SchoolDetailsResponse;
import com.emis.hrservice.service.cache.SchoolCacheService;
import com.emis.hrservice.service.client.SchoolClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
public class SchoolCacheServiceImp implements SchoolCacheService {

    private final Map<String, Long> schoolCodeToIdCache = new ConcurrentHashMap<>();
    private final Map<String, SchoolDetailsResponse> schoolCodeToDetailsCache = new ConcurrentHashMap<>();
    private final SchoolClientService schoolClientService;

    public Mono<Long> getSchoolIdByCode(String schoolCode) {
        Long cachedId = schoolCodeToIdCache.get(schoolCode);
        if (cachedId != null) {
            return Mono.just(cachedId);
        }

        return schoolClientService.getSchoolDetails(schoolCode)
                .flatMap(schoolDetailsResponse -> {
                    Long schoolId = schoolDetailsResponse.schoolId();
                    schoolCodeToIdCache.put(schoolCode, schoolId);
                    return Mono.just(schoolId);
                });
    }

    @Override
    public Mono<SchoolDetailsResponse> getSchoolDetails(String schoolCode) {
        SchoolDetailsResponse cachedSchoolDetails = schoolCodeToDetailsCache.get(schoolCode);
        if (cachedSchoolDetails != null) {
            return Mono.just(cachedSchoolDetails);
        }

        return schoolClientService.getSchoolDetails(schoolCode)
                .flatMap(schoolDetailsResponse -> {
                    schoolCodeToDetailsCache.put(schoolCode, schoolDetailsResponse);
                    return Mono.just(schoolDetailsResponse);
                });
    }

    @Scheduled(fixedRate = 300000)
    public void clearSchoolCache() {
        schoolCodeToIdCache.clear();
    }
}
