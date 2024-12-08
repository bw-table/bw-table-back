package com.zero.bwtableback.statistics.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class StatisticsScheduler {

    private final JobLauncher jobLauncher;
    private final Job dailyStatisticsJob;
    private final Job weeklyStatisticsJob;
    private final Job monthlyStatisticsJob;
    private final Job popularTimeSlotsJob;
    private final Job popularDatesJob;

    @Scheduled(cron = "0 0 1 * * ?") // 매일 새벽 1시 실행
    public void runDailyStatisticsJob() {
        try {
            jobLauncher.run(dailyStatisticsJob, createJobParameters());
            log.info("일별 예약 통계 저장을 스케쥴러 작업을 완료했습니다.");
        } catch (Exception e) {
            log.error("일별 예약 통계 스케쥴러 작업이 실패했습니다.", e);
        }
    }

    @Scheduled(cron = "0 0 2 ? * SUN") // 매주 일요일 새벽 2시 실행
    public void runWeeklyStatisticsJob() {
        try {
            jobLauncher.run(weeklyStatisticsJob, createJobParameters());
            log.info("주별 예약 통계 저장을 스케쥴러 작업을 완료했습니다.");
        } catch (Exception e) {
            log.error("주별 예약 통계 스케쥴러 작업이 실패했습니다.", e);
        }
    }

    @Scheduled(cron = "0 0 3 1 * ?") // 매월 1일 새벽 3시 실행
    public void runMonthlyStatisticsJob() {
        try {
            jobLauncher.run(monthlyStatisticsJob, createJobParameters());
            log.info("월별 예약 통계 저장을 스케쥴러 작업을 완료했습니다.");
        } catch (Exception e) {
            log.error("월별 예약 통계 스케쥴러 작업이 실패했습니다.", e);
        }
    }

    @Scheduled(cron = "0 0 4 * * ?") // 매일 새벽 4시 실행
    public void runPopularTimeSlotsJob() {
        try {
            jobLauncher.run(popularTimeSlotsJob, createJobParameters());
            log.info("인기 예약 시간대 통계 저장을 스케쥴러 작업을 완료했습니다.");
        } catch (Exception e) {
            log.error("인기 예약 시간대 통계 스케쥴러 작업이 실패했습니다.", e);
        }
    }

    @Scheduled(cron = "0 0 5 * * ?") // 매일 새벽 5시 실행
    public void runPopularDatesJob() {
        try {
            jobLauncher.run(popularDatesJob, createJobParameters());
            log.info("인기 예약 일자 통계 저장을 스케쥴러 작업을 완료했습니다.");
        } catch (Exception e) {
            log.error("인기 예약 일자 통계 스케쥴러 작업이 실패했습니다.", e);
        }
    }

    private JobParameters createJobParameters() {
        return new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
    }

}
