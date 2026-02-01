package com.emis.hrservice.mapper;

import com.emis.hrservice.domain.db.StaffServiceHistory;
import com.emis.hrservice.dto.request.StaffTransferRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface StaffTransferMapper {

    @Mapping(target = "historyId", ignore = true)
    @Mapping(target = "staffId", ignore = true)
    @Mapping(target = "fromSchoolId", ignore = true)
    @Mapping(target = "toSchoolId", ignore = true)
    @Mapping(target = "fromSchoolCode", ignore = true)
    @Mapping(target = "toSchoolCode", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", constant = "false")
    @Mapping(source = "changeType", target = "changeType")
    @Mapping(source = "newPosition", target = "newPosition")
    @Mapping(source = "startDate", target = "startDate")
    @Mapping(source = "remarks", target = "remarks")
    StaffServiceHistory toEntity(StaffTransferRequest request);
}
