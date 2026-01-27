package com.emis.hrservice.dto.response;

import com.emis.hrservice.domain.db.Staff;

public record EmergencyContactResponse(String staffCode,
                                       String contactName,
                                       String relationship,
                                       String phoneNumber) {
    public static EmergencyContactResponse from(Staff staff){
        return new EmergencyContactResponse(
                staff.getStaffCode(),
                staff.getEmergencyContactName(),
                staff.getEmergencyContactRelationship(),
                staff.getEmergencyContactPhone()
        );
    }
}
