package com.emis.hrservice.service.impl;

import com.emis.hrservice.dto.response.DailyAttendanceAnalyticsResponse;
import com.emis.hrservice.dto.response.PeriodicAttendanceAnalyticsResponse;
import com.emis.hrservice.repository.StaffAttendanceRepository;
import com.emis.hrservice.service.HrAnalyticsService;
import com.emis.hrservice.service.cache.SchoolCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class HrAnalyticsServiceImpl implements HrAnalyticsService {

    private final StaffAttendanceRepository staffAttendanceRepository;
    private final SchoolCacheService  schoolCacheService;
    @Override
    public Mono<DailyAttendanceAnalyticsResponse> getDailyAttendanceAnalytics(String schoolCode, LocalDate attendanceDate, String requestId) {
        return schoolCacheService.getSchoolDetails(schoolCode)
                .flatMap(school ->
                        staffAttendanceRepository.getDailyAttendanceAnalytics(school.schoolId(), attendanceDate)
                        .map(analyticsView -> DailyAttendanceAnalyticsResponse.from(analyticsView, school)));
    }

    @Override
    public Mono<PeriodicAttendanceAnalyticsResponse> getPeriodicAttendanceAnalytics(
            String schoolCode, Integer days, String requestId) {
        return schoolCacheService.getSchoolDetails(schoolCode)
                .flatMapMany(school ->
                        staffAttendanceRepository.getAttendanceRisk(school.schoolId())
                )
                                .collectList()
                        .map(list ->
                                PeriodicAttendanceAnalyticsResponse.from(schoolCode, days,  list));
    }

    public Mono<Void> refreshAnalytics() {
        return staffAttendanceRepository
                .refreshDailyAttendanceAnalyticsView()
                .then();
    }

    @Scheduled(cron = "0 */15 * * * ?")
    public void refreshDailyAnalytics() {
        refreshAnalytics()
                .subscribe();
    }

    @Scheduled(cron = "0 30 23 * * ?") // every day 11:30pm
    public void refreshAttendanceRisk() {
        staffAttendanceRepository.refreshAttendanceRiskView().subscribe();
    }


}
