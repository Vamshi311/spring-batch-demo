package com.example.springbatchdemo.config;

import javax.annotation.PreDestroy;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableScheduling
@Slf4j
public class JobSchedulerConfig {

	@Autowired
	private JobExplorer jobExplorer;

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private Job linesJob;

	@Autowired
	private JobOperator operator;

	@PreDestroy
	public void destroy() throws Exception {
		jobExplorer.getJobNames().forEach(name -> log.info("job name: {}", name));
		jobExplorer.getJobInstances("linesJob", 0, jobExplorer.getJobInstanceCount("linesJob")).forEach(jobInstance -> {
			log.info("job instance id {}", jobInstance.getInstanceId());
		});
	}

	@Scheduled(fixedRate = 5000000)
	public void run() throws Exception {
		JobInstance lastJobInstance = jobExplorer.getLastJobInstance("linesJob");
		if (lastJobInstance == null) {
			jobLauncher.run(linesJob, new JobParameters());
		} else {

			JobExecution lastJobExecution = jobExplorer.getLastJobExecution(lastJobInstance);
			if (lastJobExecution != null && lastJobExecution.getStatus() == BatchStatus.FAILED) {
				operator.restart(lastJobExecution.getId());
			} else {
				operator.startNextInstance("linesJob");
			}
		}
	}
}
