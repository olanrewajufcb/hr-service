package com.emis.hrservice.service.cache;

import com.emis.hrservice.dto.response.ClassSectionResponse;
import reactor.core.publisher.Mono;

public interface AcademicCacheService {

    Mono<ClassSectionResponse> getClassSectionDetailsFromCache(String schoolCode, Long classId,
                                                      Long sectionId, String staffCode);


}
