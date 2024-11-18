package com.zero.bwtableback.reservation.batch;

import com.zero.bwtableback.reservation.entity.NotificationStatus;
import com.zero.bwtableback.reservation.repository.NotificationRepository;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class DeleteNotificationsJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    @Bean
    public Job deleteOldNotificationsJob(Step deleteOldNotificationsStep) {
        return new JobBuilder("deleteOldNotificationsJob", jobRepository)
                .start(deleteOldNotificationsStep)
                .build();
    }

    @Bean
    public Step deleteOldNotificationsStep(NotificationRepository notificationRepository) {
        return new StepBuilder("deleteOldNotificationsStep", jobRepository)
                .<Long, Long>chunk(50, platformTransactionManager)
                .reader(oldNotificationIdsReader(notificationRepository))
                .writer(oldNotificationsWriter(notificationRepository))
                .build();
    }

    @Bean
    @StepScope
    public RepositoryItemReader<Long> oldNotificationIdsReader(NotificationRepository notificationRepository) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusWeeks(1);

        return new RepositoryItemReaderBuilder<Long>()
                .name("oldNotificationIdsReader")
                .repository(notificationRepository)
                .methodName("findIdsBySentTimeBeforeAndStatus")
                .arguments(cutoffDate, NotificationStatus.SENT)
                .pageSize(50)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemWriter<Long> oldNotificationsWriter(NotificationRepository notificationRepository) {
        return ids -> {
            notificationRepository.deleteAllByIdInBatch((Iterable<Long>) ids);
            log.info("일주일 지난 알림 {}건 삭제 완료", ids.size());
        };
    }
    
}
