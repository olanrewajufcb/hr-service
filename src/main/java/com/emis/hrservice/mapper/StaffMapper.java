package com.emis.hrservice.mapper;

import com.emis.hrservice.domain.db.Staff;
import com.emis.hrservice.dto.request.CreateStaffRequest;
import com.emis.hrservice.dto.response.CreateStaffResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring", imports = {LocalDateTime.class})
public interface StaffMapper {
    @Mapping(target = "staffId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "photoUrl", ignore = true)
    @Mapping(source = "employmentType", target = "employmentType")
    @Mapping(source = "salarySource", target = "salarySource")
    @Mapping(target = "yearsOfExperience", ignore = true)
    @Mapping(target = "currentSchoolPostingDate", ignore = true)
    @Mapping(target = "isDeleted", constant = "false")
    Staff toEntity(CreateStaffRequest request);

    @Mapping(target = "fullName", expression = "java(createFullName(staff))")
    CreateStaffResponse toDto(Staff staff);

    default String createFullName(Staff staff) {
        String firstName = staff.getFirstName() != null ? staff.getFirstName() : "";
        String lastName = staff.getLastName() != null ? staff.getLastName() : "";
        return (firstName + " " + lastName).trim();
    }
}
