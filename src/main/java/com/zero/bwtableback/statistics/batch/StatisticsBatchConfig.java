package com.zero.bwtableback.statistics.batch;

import com.zero.bwtableback.restaurant.entity.Restaurant;
import com.zero.bwtableback.restaurant.repository.RestaurantRepository;
import com.zero.bwtableback.statistics.batch.processor.DailyStatisticsProcessor;
import com.zero.bwtableback.statistics.batch.processor.MonthlyStatisticsProcessor;
import com.zero.bwtableback.statistics.batch.processor.PopularDatesProcessor;
import com.zero.bwtableback.statistics.batch.processor.PopularTimeSlotsProcessor;
import com.zero.bwtableback.statistics.batch.processor.WeeklyStatisticsProcessor;
import com.zero.bwtableback.statistics.batch.writer.DailyStatisticsWriter;
import com.zero.bwtableback.statistics.batch.writer.MonthlyStatisticsWriter;
import com.zero.bwtableback.statistics.batch.writer.PopularDatesWriter;
import com.zero.bwtableback.statistics.batch.writer.PopularTimeSlotsWriter;
import com.zero.bwtableback.statistics.batch.writer.WeeklyStatisticsWriter;
import com.zero.bwtableback.statistics.entity.Statistics;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@RequiredArgsConstructor
@Configuration
public class StatisticsBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final RestaurantRepository restaurantRepository;

    private final DailyStatisticsProcessor dailyStatisticsProcessor;
    private final WeeklyStatisticsProcessor weeklyStatisticsProcessor;
    private final MonthlyStatisticsProcessor monthlyStatisticsProcessor;
    private final PopularTimeSlotsProcessor popularTimeSlotsProcessor;
    private final PopularDatesProcessor popularDatesProcessor;

    private final DailyStatisticsWriter dailyStatisticsWriter;
    private final WeeklyStatisticsWriter weeklyStatisticsWriter;
    private final MonthlyStatisticsWriter monthlyStatisticsWriter;
    private final PopularTimeSlotsWriter popularTimeSlotsWriter;
    private final PopularDatesWriter popularDatesWriter;


    @Bean
    public Job dailyStatisticsJob(Step dailyStatisticsStep) {
        return new JobBuilder("dailyStatisticsJob", jobRepository)
                .start(dailyStatisticsStep)
                .build();
    }

    @Bean
    public Job weeklyStatisticsJob(Step weeklyStatisticsStep) {
        return new JobBuilder("weeklyStatisticsJob", jobRepository)
                .start(weeklyStatisticsStep)
                .build();
    }

    @Bean
    public Job monthlyStatisticsJob(Step monthlyStatisticsStep) {
        return new JobBuilder("monthlyStatisticsJob", jobRepository)
                .start(monthlyStatisticsStep)
                .build();
    }

    @Bean
    public Job popularTimeSlotsJob(Step popularTimeSlotsStep) {
        return new JobBuilder("popularTimeSlotsJob", jobRepository)
                .start(popularTimeSlotsStep)
                .build();
    }

    @Bean
    public Job popularDatesJob(Step popularDatesStep) {
        return new JobBuilder("popularDatesJob", jobRepository)
                .start(popularDatesStep)
                .build();
    }


    @Bean
    public Step dailyStatisticsStep() {
        return new StepBuilder("dailyStatisticsStep", jobRepository)
                .<Restaurant, List<Statistics>>chunk(100, transactionManager)
                .reader(restaurantReader())
                .processor(dailyStatisticsProcessor)
                .writer(dailyStatisticsWriter)
                .build();
    }

    @Bean
    public Step weeklyStatisticsStep() {
        return new StepBuilder("weeklyStatisticsStep", jobRepository)
                .<Restaurant, List<Statistics>>chunk(100, transactionManager)
                .reader(restaurantReader())
                .processor(weeklyStatisticsProcessor)
                .writer(weeklyStatisticsWriter)
                .build();
    }

    @Bean
    public Step monthlyStatisticsStep() {
        return new StepBuilder("monthlyStatisticsStep", jobRepository)
                .<Restaurant, List<Statistics>>chunk(100, transactionManager)
                .reader(restaurantReader())
                .processor(monthlyStatisticsProcessor)
                .writer(monthlyStatisticsWriter)
                .build();
    }

    @Bean
    public Step popularTimeSlotsStep() {
        return new StepBuilder("popularTimeSlotsStep", jobRepository)
                .<Restaurant, List<Statistics>>chunk(100, transactionManager)
                .reader(restaurantReader())
                .processor(popularTimeSlotsProcessor)
                .writer(popularTimeSlotsWriter)
                .build();
    }

    @Bean
    public Step popularDatesStep() {
        return new StepBuilder("popularDatesStep", jobRepository)
                .<Restaurant, List<Statistics>>chunk(100, transactionManager)
                .reader(restaurantReader())
                .processor(popularDatesProcessor)
                .writer(popularDatesWriter)
                .build();
    }

    @Bean
    public ItemReader<Restaurant> restaurantReader() {
        return new ListItemReader<>(restaurantRepository.findAll());
    }

}
