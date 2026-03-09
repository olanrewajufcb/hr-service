package com.emis.hrservice.service.impl;

import com.emis.hrservice.dto.response.*;
import com.emis.hrservice.repository.StaffAttendanceRepository;
import com.emis.hrservice.service.cache.SchoolCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HrAnalyticsServiceImplTest {

    @Mock
    private StaffAttendanceRepository staffAttendanceRepository;

    @Mock
    private SchoolCacheService schoolCacheService;

    @InjectMocks
    private HrAnalyticsServiceImpl hrAnalyticsService;

    private final String schoolCode = "SCH-001";
    private final Long schoolId = 1L;
    private final LocalDate today = LocalDate.now();

    @Test
    void getDailyAttendanceAnalytics_ShouldReturnResponse() {
        SchoolDetailsResponse schoolDetails = mock(SchoolDetailsResponse.class);
        when(schoolDetails.schoolId()).thenReturn(schoolId);
        when(schoolDetails.schoolName()).thenReturn("Test School");
        when(schoolDetails.schoolCode()).thenReturn(schoolCode);

        AttendanceAnalyticsResponse analyticsView = AttendanceAnalyticsResponse.builder()
                .schoolId(schoolId)
                .attendanceDate(today)
                .presentCount(10L)
                .absentCount(2L)
                .lateCount(1L)
                .totalStaff(13L)
                .attendanceRate(77)
                .build();

        when(schoolCacheService.getSchoolDetails(schoolCode)).thenReturn(Mono.just(schoolDetails));
        when(staffAttendanceRepository.getDailyAttendanceAnalytics(schoolId, today)).thenReturn(Mono.just(analyticsView));

        Mono<DailyAttendanceAnalyticsResponse> result = hrAnalyticsService.getDailyAttendanceAnalytics(schoolCode, today, "req-123");

        StepVerifier.create(result)
                .expectNextMatches(response -> 
                        response.schoolId().equals(schoolId) &&
                        response.schoolName().equals("Test School") &&
                        response.present().equals(10L) &&
                        response.absent().equals(2L) &&
                        response.attendancePercentage() == 77
                )
                .verifyComplete();

        verify(schoolCacheService).getSchoolDetails(schoolCode);
        verify(staffAttendanceRepository).getDailyAttendanceAnalytics(schoolId, today);
    }

    @Test
    void getPeriodicAttendanceAnalytics_ShouldReturnResponse() {
        SchoolDetailsResponse schoolDetails = mock(SchoolDetailsResponse.class);
        when(schoolDetails.schoolId()).thenReturn(schoolId);

        PeriodicAttendanceAnalytics risk1 = PeriodicAttendanceAnalytics.builder()
                .staffId(101L)
                .staffName("John Doe")
                .absentDays(5)
                .riskLevel("HIGH")
                .build();

        PeriodicAttendanceAnalytics risk2 = PeriodicAttendanceAnalytics.builder()
                .staffId(102L)
                .staffName("Jane Doe")
                .absentDays(3)
                .riskLevel("MEDIUM")
                .build();

        when(schoolCacheService.getSchoolDetails(schoolCode)).thenReturn(Mono.just(schoolDetails));
        when(staffAttendanceRepository.getAttendanceRisk(schoolId)).thenReturn(Flux.just(risk1, risk2));

        Mono<PeriodicAttendanceAnalyticsResponse> result = hrAnalyticsService.getPeriodicAttendanceAnalytics(schoolCode, 30, "req-123");

        StepVerifier.create(result)
                .expectNextMatches(response -> 
                        response.schoolCode().equals(schoolCode) &&
                        response.periodInDays() == 30 &&
                        response.highRiskCount() == 1 &&
                        response.mediumRiskCount() == 1 &&
                        response.lowRiskCount() == 0 &&
                        response.staffDetails().size() == 2
                )
                .verifyComplete();

        verify(schoolCacheService).getSchoolDetails(schoolCode);
        verify(staffAttendanceRepository).getAttendanceRisk(schoolId);
    }

    @Test
    void refreshAnalytics_ShouldCallRepository() {
        when(staffAttendanceRepository.refreshDailyAttendanceAnalyticsView()).thenReturn(Mono.empty());

        Mono<Void> result = hrAnalyticsService.refreshAnalytics();

        StepVerifier.create(result)
                .verifyComplete();

        verify(staffAttendanceRepository).refreshDailyAttendanceAnalyticsView();
    }

    @Test
    void refreshDailyAnalytics_ShouldCallRefreshAnalyticsAndSubscribe() {
        when(staffAttendanceRepository.refreshDailyAttendanceAnalyticsView()).thenReturn(Mono.empty());

        hrAnalyticsService.refreshDailyAnalytics();

        verify(staffAttendanceRepository).refreshDailyAttendanceAnalyticsView();
    }

    @Test
    void refreshAttendanceRisk_ShouldCallRepositoryAndSubscribe() {
        when(staffAttendanceRepository.refreshAttendanceRiskView()).thenReturn(Mono.empty());

        hrAnalyticsService.refreshAttendanceRisk();

        verify(staffAttendanceRepository).refreshAttendanceRiskView();
    }
}
