package com.emis.hrservice.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;

@Configuration
@Getter
@Setter
@EnableR2dbcAuditing
@ConfigurationProperties(prefix = "emis.services")
public class ServiceConfigurationProperties {

    @NestedConfigurationProperty
    private SchoolServiceProperties schoolServiceProperties;

    @NestedConfigurationProperty
    private AcademicServiceProperties academicServiceProperties;

    @NestedConfigurationProperty
    private StudentServiceProperties studentServiceProperties;

    private int timeout;


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

}
