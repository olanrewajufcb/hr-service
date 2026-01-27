package com.emis.hrservice.mapper;

import com.emis.hrservice.domain.db.StaffAssignment;

import java.time.LocalDateTime;

import com.emis.hrservice.dto.request.CreateStaffAssignmentRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = {LocalDateTime.class})
public interface StaffAssignmentMapper {
    @Mapping(target = "assignmentId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", constant = "false")
    @Mapping(target = "staffId", ignore = true)
    StaffAssignment toEntity(CreateStaffAssignmentRequest request);

}
