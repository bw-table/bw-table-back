package com.zero.bwtableback.reservation.batch;

import com.zero.bwtableback.reservation.entity.Notification;
import com.zero.bwtableback.reservation.entity.NotificationType;
import com.zero.bwtableback.reservation.entity.Reservation;
import com.zero.bwtableback.reservation.entity.ReservationStatus;
import com.zero.bwtableback.reservation.repository.ReservationRepository;
import com.zero.bwtableback.reservation.service.NotificationScheduleService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DayOfVisitNotificationJobConfig {

    private final JobRepository jobRepository;
    private final NotificationScheduleService notificationScheduleService;
    private final PlatformTransactionManager platformTransactionManager;

    @Bean
    public Job sendDayOfVisitNotificationJob(Step sendDayOfVisitNotificationStep) {
        return new JobBuilder("sendDayOfVisitNotificationJob", jobRepository)
                .start(sendDayOfVisitNotificationStep)
                .build();
    }

    @Bean
    public Step sendDayOfVisitNotificationStep(ReservationRepository reservationRepository) {
        return new StepBuilder("sendDayOfVisitNotificationStep", jobRepository)
                .<Reservation, Notification>chunk(50, platformTransactionManager)
                .reader(todayReservationsReader(reservationRepository))
                .processor(dayOfVisitNotificationProcessor())
                .writer(dayOfVisitNotificationWriter())
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<Reservation> todayReservationsReader(ReservationRepository reservationRepository) {
        return new ListItemReader<>(
                reservationRepository.findByReservationDateAndReservationStatus(
                        LocalDate.now(),
                        ReservationStatus.CONFIRMED
                )
        );
    }

    @Bean
    public ItemProcessor<Reservation, Notification> dayOfVisitNotificationProcessor() {
        return reservation -> {
            Notification notification = notificationScheduleService.createAndSaveNotification(
                    reservation, NotificationType.DAY_OF_VISIT);
            log.info("알림 생성 완료: 예약 id {}", reservation.getId());
            return notification;
        };
    }

    @Bean
    public ItemWriter<Notification> dayOfVisitNotificationWriter() {
        return notifications -> notifications.forEach(notification -> {
            notificationScheduleService.sendNotification(notification);
            log.info("알림 전송 완료: 예약 알림 id {}", notification.getId());
        });
    }

}
