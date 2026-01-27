package com.emis.hrservice.mapper;

import com.emis.hrservice.domain.db.StaffAcademicQualification;
import com.emis.hrservice.dto.request.AddStaffAcademicQualificationRequest;

import java.time.LocalDateTime;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = {LocalDateTime.class})
public interface StaffQualificationMapper {
    @Mapping(target = "qualificationId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", constant = "false")
    @Mapping(target = "staffId", ignore = true)
    StaffAcademicQualification  toEntity(AddStaffAcademicQualificationRequest request);

}
