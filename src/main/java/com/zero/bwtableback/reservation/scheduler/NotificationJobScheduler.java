package com.zero.bwtableback.reservation.scheduler;

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
public class NotificationJobScheduler {

    private final JobLauncher jobLauncher;
    private final Job sendDayOfVisitNotificationJob;
    private final Job deleteOldNotificationsJob;

    @Scheduled(cron = "0 0 8 * * ?") // 매일 아침 8시에 실행
    public void scheduleSendDayOfVisitNotifications() {
        try {
            jobLauncher.run(sendDayOfVisitNotificationJob, createJobParameters());
            log.info("당일 예약 알림 스케쥴러 작업을 완료했습니다.");
        } catch (Exception e) {
            log.error("당일 예약 알림 스케쥴러 작업이 실패했습니다.", e);
        }
    }


    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정에 실행
    public void scheduleDeleteOldNotifications() {
        try {
            jobLauncher.run(deleteOldNotificationsJob, createJobParameters());
            log.info("일주일 지난 알림 삭제 스케쥴러 작업을 완료했습니다.");
        } catch (Exception e) {
            log.error("일주일 지난 알림 삭제 스케쥴러 작업이 실패했습니다.", e);
        }
    }

    private JobParameters createJobParameters() {
        return new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
    }

}
