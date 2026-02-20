package org.example.mopl.user.scheduler;

import io.micrometer.core.annotation.Timed;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.example.mopl.user.entity.TemporaryPassword;
import org.example.mopl.user.repository.TemporaryPasswordRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
@EnableBatchProcessing
public class TemporaryPasswordBatchDelete {
    private final EntityManagerFactory entityManagerFactory;
    private final JobRepository jobRepository;
    private final TemporaryPasswordRepository temporaryPasswordRepository;
    private final PlatformTransactionManager transactionManager;
    private final TemporaryPasswordBatchListener temporaryPasswordBatchListener;

    @Bean
    public Job temporaryPasswordBatchDeleteJob() {
        return new JobBuilder("temporaryPasswordDeleteJob", jobRepository)
                .start(temporaryPasswordBatchDeleteStep())
                .build();
    }

    @Bean
    @Timed(value = "delete_temporary_password_batch_step_duration"
            ,extraTags = {"temporary_password_cleanup_duration"})
    public Step temporaryPasswordBatchDeleteStep() {

        return new StepBuilder("temporaryPasswordDeleteStep", jobRepository)
                .<TemporaryPassword,TemporaryPassword>chunk(1000,transactionManager)
                .reader(temporaryPasswordJpaPagingItemReader(null))
                .writer(temporaryPasswordItemWriter())
                .listener(temporaryPasswordBatchListener)
                .faultTolerant()
                .retry(org.springframework.dao.PessimisticLockingFailureException.class)
                .retryLimit(3)
                .build();
    }

    @StepScope
    @Bean
    public JpaPagingItemReader<TemporaryPassword> temporaryPasswordJpaPagingItemReader(
            @Value("#{jobParameters['currentTime']}") Long currentTime
    ) {
        Instant current = Instant.ofEpochMilli(currentTime);
        return new JpaPagingItemReaderBuilder<TemporaryPassword>()
                .name("temporaryPasswordJpaPagingItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT u FROM TemporaryPassword u WHERE u.createdAt <= :currentTime")
                .parameterValues(Map.of("currentTime", current))
                .pageSize(1000)
                .build();
    }

    @Bean
    public ItemWriter<TemporaryPassword> temporaryPasswordItemWriter() {
        return TemporaryPassword -> temporaryPasswordRepository.deleteAll(TemporaryPassword);
    }
}
