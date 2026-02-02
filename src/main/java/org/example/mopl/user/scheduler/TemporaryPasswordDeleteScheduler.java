package org.example.mopl.user.scheduler;


import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class TemporaryPasswordDeleteScheduler {
    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;

    //@Scheduled(cron = "0 0 3 * * *")
    @Scheduled(fixedRate = 60000)
    public void runTemporaryPasswordDeleteJob() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException, NoSuchJobException {
        Job job = jobRegistry.getJob("temporaryPasswordDeleteJob");
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .addLong("currentTime", Instant.now().minus(3, ChronoUnit.MINUTES).toEpochMilli())
                .toJobParameters();

        jobLauncher.run(job,jobParameters);
    }
}
