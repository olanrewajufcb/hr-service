package com.emis.hrservice.mapper;

import com.emis.hrservice.domain.db.StaffTeachingQualification;

import java.time.LocalDateTime;

import com.emis.hrservice.dto.request.AddStaffTeachingQualificationRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = {LocalDateTime.class})
public interface StaffTeachingQualificationMapper {
    @Mapping(target = "teachingQualificationId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", constant = "false")
    @Mapping(target = "staffId", ignore = true)
    @Mapping(source = "teachingQualification", target = "teachingQualification")
    @Mapping(source = "subjectOfQualification", target = "subjectOfQualification")
    StaffTeachingQualification toEntity(AddStaffTeachingQualificationRequest request);

}
