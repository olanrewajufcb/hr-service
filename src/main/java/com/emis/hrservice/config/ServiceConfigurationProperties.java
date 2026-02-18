package com.emis.hrservice.config;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.validation.annotation.Validated;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Configuration
@Getter
@Setter
@EnableR2dbcAuditing
@Validated
@ConfigurationProperties(prefix = "emis.services")
public class ServiceConfigurationProperties {

    @NestedConfigurationProperty
    private SchoolServiceProperties schoolServiceProperties;

    @NestedConfigurationProperty
    private AcademicServiceProperties academicServiceProperties;

    @NestedConfigurationProperty
    private StudentServiceProperties studentServiceProperties;

    @NestedConfigurationProperty
    private SchoolAttendanceConfiguration schoolAttendanceConfiguration;

    private int timeout;

    private String storageBaseUrl;




    @Getter
    @Setter
    public static class SchoolServiceProperties {
        private String baseUrl;
        private String getSchoolDetailsUrl;
        private String validateSchoolExistsUrl;

    }

    @Getter
    @Setter
    public static class StudentServiceProperties{
        private String baseUrl;
        private String getStudentDetailsUrl;
        private String batchStudentDetailsUrl;
    }

    @Getter
    @Setter
    public static class AcademicServiceProperties{
        private String baseUrl;
        private String getClassSectionUrl;
    }

    @Getter
    @Setter
    public static class SchoolAttendanceConfiguration {
        @NotBlank
        String schoolCode;
        @NotNull
        private LocalTime checkInTime;
        @NotNull
        private LocalTime cutOffTime;
    }

}
