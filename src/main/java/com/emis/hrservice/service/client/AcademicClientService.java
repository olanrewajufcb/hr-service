package com.emis.hrservice.service.client;

import com.emis.hrservice.domain.db.Staff;
import com.emis.hrservice.dto.response.ClassSectionResponse;
import reactor.core.publisher.Mono;

public interface AcademicClientService {

    Mono<ClassSectionResponse> getClassSectionDetails(String schoolCode,  Long classId, Long sectionId, String staffCode);

    Mono<ClassSectionResponse> updateClassSectionWithTeacherInfo(
            String schoolCode, Long classId, Long sectionId, Staff staff);

}
