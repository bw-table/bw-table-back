package com.zero.bwtableback.reservation.batch;

import com.zero.bwtableback.reservation.entity.Notification;
import com.zero.bwtableback.reservation.entity.NotificationType;
import com.zero.bwtableback.reservation.entity.Reservation;
import com.zero.bwtableback.reservation.repository.ReservationRepository;
import com.zero.bwtableback.reservation.service.NotificationScheduleService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
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
@EnableBatchProcessing
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
        LocalDate today = LocalDate.now();
        List<Reservation> reservations = reservationRepository.findReservationsByDate(today);
        return new ListItemReader<>(reservations);
    }
    // 예약 알림 생성 Processor
    @Bean
    public ItemProcessor<Reservation, Notification> dayOfVisitNotificationProcessor() {
        return reservation -> {
            Notification notification = notificationScheduleService.createAndSaveNotification(
                    reservation, NotificationType.DAY_OF_VISIT);
            log.info("예약 알림 생성 완료: {}", reservation.getId());
            return notification;
        };
    }

    // 예약 알림 전송 Writer
    @Bean
    public ItemWriter<Notification> dayOfVisitNotificationWriter() {
        return notifications -> notifications.forEach(notification -> {
            notificationScheduleService.sendNotification(notification);
            log.info("예약 알림 전송 완료: {}", notification.getId());
        });
    }

}
