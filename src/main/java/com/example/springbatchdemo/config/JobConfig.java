package com.example.springbatchdemo.config;

import java.time.format.DateTimeFormatter;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Scheduled;

import com.example.springbatchdemo.processor.LinesProcessor;
import com.example.springbatchdemo.reader.LinesReader;
import com.example.springbatchdemo.writer.LinesWriter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class JobConfig {

	public static final String DATETIME_FORMAT = "dd-MM-yyyy HH:mm";
	public static LocalDateTimeSerializer LOCAL_DATETIME_SERIALIZER = new LocalDateTimeSerializer(
			DateTimeFormatter.ofPattern(DATETIME_FORMAT));

	public static final String DATE_FORMAT = "dd-MM-yyyy";
	public static LocalDateSerializer LOCAL_DATE_SERIALIZER = new LocalDateSerializer(
			DateTimeFormatter.ofPattern(DATE_FORMAT));

	@Autowired
	private LinesReader linesReader;

	@Autowired
	private LinesProcessor linesProcessor;

	@Autowired
	private LinesWriter linesWriter;

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Autowired
	private JobLauncher jobLauncher;

	@Bean
	public Step lineReaderTask() {
		return stepBuilderFactory.get("linesReader").tasklet(linesReader).build();
	}

	@Bean
	public Step lineProcessorTask() {
		return stepBuilderFactory.get("linesProcessor").tasklet(linesProcessor).build();
	}

	@Bean
	public Step lineWriterTask() {
		return stepBuilderFactory.get("linesWriter").tasklet(linesWriter).build();
	}

	@Bean
	public Job linesJob() {
		return jobBuilderFactory.get("linesJob").incrementer(new RunIdIncrementer()).start(lineReaderTask())
				.next(lineProcessorTask()).next(lineWriterTask()).build();
	}

	@Scheduled(fixedRate = 5000000)
	public void run() throws Exception {
		JobParameters params = new JobParametersBuilder().addString("JobID", String.valueOf(System.currentTimeMillis()))
				.toJobParameters();
		JobExecution execution = jobLauncher.run(linesJob(),
				params);
		log.info("Exit status: {}", execution.getStatus());
	}

	// Not sure why below object mapper is not picked while saving job data to job
	// repository. So created custom serializer and used it for line#dob.
	@Bean
	@Primary
	public ObjectMapper objectMapper() {
		JavaTimeModule module = new JavaTimeModule();
		module.addSerializer(LOCAL_DATETIME_SERIALIZER);
		module.addSerializer(LOCAL_DATE_SERIALIZER);
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(module);
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		return objectMapper;
	}

	// Use below configs if you want to define jobRepository and transactionManager
	// by yourself to get more control over them.

//	  @Bean
//	    public JobRepository jobRepository() throws Exception {
//	        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
//	        factory.setDataSource(dataSource());
//	        factory.setTransactionManager(transactionManager());
//	        return factory.getObject();
//	    }
//
//	    @Bean
//	    public DataSource dataSource() {
//	        DriverManagerDataSource dataSource = new DriverManagerDataSource();
//	        dataSource.setDriverClassName("org.sqlite.JDBC");
//	        dataSource.setUrl("jdbc:sqlite:repository.sqlite");
//	        return dataSource;
//	    }
//
//	    @Bean
//	    public PlatformTransactionManager transactionManager() {
//	        return new ResourcelessTransactionManager();
//	    }
//
//	    @Bean
//	    public JobLauncher jobLauncher() throws Exception {
//	        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
//	        jobLauncher.setJobRepository(jobRepository());
//	        return jobLauncher;
//	    }
}
