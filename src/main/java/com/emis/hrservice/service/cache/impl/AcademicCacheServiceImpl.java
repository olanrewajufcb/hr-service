package com.emis.hrservice.service.cache.impl;

import com.emis.hrservice.dto.response.ClassSectionResponse;
import com.emis.hrservice.service.cache.AcademicCacheService;
import com.emis.hrservice.service.client.AcademicClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@RequiredArgsConstructor
@Component
public class AcademicCacheServiceImpl implements AcademicCacheService {

    private final Map<Long, ClassSectionResponse> classSectionCache = new ConcurrentHashMap<>();
    private final AcademicClientService academicClientService;
    @Override
    public Mono<ClassSectionResponse> getClassSectionDetailsFromCache(
            String schoolCode, Long classId, Long sectionId, String staffCode) {

            ClassSectionResponse cachedDetails = classSectionCache.get(classId);
            if (cachedDetails != null) {
                return Mono.just(cachedDetails);
            }
            return academicClientService.getClassSectionDetails(schoolCode, classId, sectionId, staffCode)
                    .flatMap(classSectionResponse -> {
                        classSectionCache.put(classId, classSectionResponse);
                        return Mono.just(classSectionResponse);
                    });
        }
    @Scheduled(fixedRate = 300000)
        public void clearSectionCache() {
            classSectionCache.clear();
        }

}
