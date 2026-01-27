package com.emis.hrservice.service.cache;

import com.emis.hrservice.dto.response.SchoolDetailsResponse;
import reactor.core.publisher.Mono;

public interface SchoolCacheService {

    Mono<Long> getSchoolIdByCode(String schoolCode);

    Mono<SchoolDetailsResponse> getSchoolDetails(String schoolCode);

}
