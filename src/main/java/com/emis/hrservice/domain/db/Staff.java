package com.emis.hrservice.domain.db;

import com.emis.hrservice.enums.*;
import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Table(name = "staff", schema = "hr_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Staff {
    @Id
    private Long staffId;

    @Column("school_id")
    private Long schoolId;

    @Column("school_code")
    private String schoolCode;

    @Column("school_name")
    private String schoolName;

    @Column("lga")
    private String lga;

    @Column("staff_code")
    private String staffCode;

    @Column("first_name")
    private String firstName;

    @Column("last_name")
    private String lastName;

    @Column("other_names")
    private String otherNames;

    @Column("gender")
    private Gender gender;

    @Column("date_of_birth")
    private LocalDate dateOfBirth;

    @Column("email")
    private String email;

    @Column("phone")
    private String phone;

    @Column("address")
    private String address;

    @Column("city")
    private String city;

    @Column("state")
    private String state;

    @Column("nationality")
    private String nationality;

    @Column("staff_category")
    private StaffCategory staffCategory;

    @Column("staff_role")
    private StaffRole staffRole;

    @Column("employment_type")
    private EmploymentType employmentType;

    @Column("salary_source")
    private SalarySource salarySource;

    @Column("employment_date")
    private LocalDate employmentDate;

    @Column("appointment_date")
    private LocalDate appointmentDate;

    @Column("main_subject_taught")
    private String mainSubjectTaught;

    @Column("secondary_subjects")
    private List<String> secondarySubjects;

    @Column("current_school_posting_date")
    private LocalDate currentSchoolPostingDate;

    @Column("caregiver_type")
    private CaregiverType caregiverType;

    @Column("staff_photo_url")
    private String staffPhotoUrl;

    @Column("trcn_number")
    private String trcnNumber;

    @Column("date_of_first_appointment")
    private LocalDate dateOfFirstAppointment;

    @Column("years_of_experience")
    private Integer yearsOfExperience;

    @Column("emergency_contact_name")
    private String emergencyContactName;

    @Column("emergency_contact_phone")
    private String emergencyContactPhone;

    @Column("emergency_contact_relationship")
    private String emergencyContactRelationship;

    @Column("status")
    @Builder.Default
    private Status status = Status.ACTIVE;

    @Column("is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;

    @Column("deleted_at")
    private LocalDateTime deletedAt;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;

    // Transient fields for relationships (not stored in DB)
    @Transient
    private List<StaffAcademicQualification> academicQualifications;

    @Transient
    private List<StaffTeachingQualification> teachingQualifications;

    @Transient
    private List<StaffSubjectSpecialization> subjectSpecializations;

    @Transient
    private List<StaffAttendance> attendances;

    @Transient
    private List<StaffAssignment> assignments;

    @Transient
    private List<StaffServiceHistory> serviceHistory;

    public String getFullName() {
        if (otherNames != null && !otherNames.isEmpty()) {
            return String.format("%s %s %s", firstName, otherNames, lastName);
        }
        return String.format("%s %s", firstName, lastName);
    }
}