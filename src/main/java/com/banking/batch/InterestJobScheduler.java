package com.banking.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class InterestJobScheduler {

    private static final Logger logger = LoggerFactory.getLogger(InterestJobScheduler.class);

    private final JobLauncher jobLauncher;
    private final Job interestCalculationJob;

    public InterestJobScheduler(JobLauncher jobLauncher,
            Job interestCalculationJob) {
        this.jobLauncher = jobLauncher;
        this.interestCalculationJob = interestCalculationJob;
    }

    // Runs every day at midnight
    @Scheduled(cron = "0 0 0 * * *")
    public void runInterestJob() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("run.id", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(interestCalculationJob, params);
            logger.info("Interest calculation job launched successfully");

        } catch (Exception e) {
            logger.error("Failed to launch interest calculation job: {}",
                    e.getMessage());
        }
    }
}