package com.emis.hrservice.dto.response;

import com.emis.hrservice.enums.SchoolLevel;
import com.emis.hrservice.enums.SchoolStatus;
import com.emis.hrservice.enums.SchoolType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SchoolDetailsResponse(Long schoolId,
                                    String schoolCode,          // Unique identifier "SCH-001"
                                    String schoolName,
                                    SchoolType type,
                                    SchoolLevel schoolLevel,
                                    String address,
                                    String phone,
                                    String email,
                                    String principalName,
                                    Integer maxStudentsPerClass,
                                    Long schoolCapacity,
                                    String academicCalendar,    // "FIRST TERM", "SECOND"
                                    LocalDate establishedYear,
                                    String city,
                                    String ward,
                                    String lga,
                                    String state,
                                    SchoolStatus status,
                                    LocalDateTime createdAt,
                                    LocalDateTime updatedAt) {}
