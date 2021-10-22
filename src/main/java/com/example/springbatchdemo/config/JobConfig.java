package com.example.springbatchdemo.config;

import java.net.MalformedURLException;
import java.time.format.DateTimeFormatter;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.ExecutionContextSerializer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.dao.Jackson2ExecutionContextStringSerializer;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.support.DatabaseType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import com.example.springbatchdemo.model.Line;
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
@EnableBatchProcessing
@Slf4j
public class JobConfig extends DefaultBatchConfigurer {

	public static final String DATETIME_FORMAT = "dd-MM-yyyy HH:mm";
	public static LocalDateTimeSerializer LOCAL_DATETIME_SERIALIZER = new LocalDateTimeSerializer(
			DateTimeFormatter.ofPattern(DATETIME_FORMAT));

	public static final String DATE_FORMAT = "dd-MM-yyyy";
	public static LocalDateSerializer LOCAL_DATE_SERIALIZER = new LocalDateSerializer(
			DateTimeFormatter.ofPattern(DATE_FORMAT));

	ExecutionContext executionContext = new ExecutionContext();

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
	private DataSource dataSource;

	@Autowired
	private JobRegistry jobRegistry;

//	@Autowired
//	private JobLauncher jobLauncher;

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
				.next(lineProcessorTask())
				.next(lineWriterTask()).build();
	}

	// We need to set this objectMapper to Jackson2ExecutionContextStringSerializer
	// to safely serialize LocalDate etc to execution context in job Repository.
	@Bean
	@Primary
	public ObjectMapper objectMapper() {
		JavaTimeModule module = new JavaTimeModule();
		module.addSerializer(LOCAL_DATETIME_SERIALIZER);
		module.addSerializer(LOCAL_DATE_SERIALIZER);
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(module);
		objectMapper.findAndRegisterModules();
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

	@Bean
	public ExecutionContextSerializer customSerializer() {
		Jackson2ExecutionContextStringSerializer jackson2ExecutionContextStringSerializer = new Jackson2ExecutionContextStringSerializer(
				Line.class.getName());
		jackson2ExecutionContextStringSerializer.setObjectMapper(objectMapper());
		return jackson2ExecutionContextStringSerializer;
	}

	@Override
	public JobRepository createJobRepository() throws Exception {

		JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
		factory.setDataSource(dataSource);
		factory.setDatabaseType(DatabaseType.MYSQL.getProductName());
		factory.setTransactionManager(getTransactionManager());
		factory.setSerializer(customSerializer());
		factory.setTablePrefix("BATCH_");
		factory.setJdbcOperations(new JdbcTemplate(dataSource));
		return (JobRepository) factory.getObject();
	}

	@Bean
	public DataSourceInitializer dataSourceInitializer() throws MalformedURLException {
		ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();

		databasePopulator.setIgnoreFailedDrops(true);

		DataSourceInitializer initializer = new DataSourceInitializer();
		initializer.setDataSource(dataSource);
		initializer.setDatabasePopulator(databasePopulator);
		return initializer;
	}

	@Override
	public JobExplorer createJobExplorer() throws Exception {
		JobExplorerFactoryBean factoryBean = new JobExplorerFactoryBean();
		factoryBean.setDataSource(dataSource);
		factoryBean.setJdbcOperations(new JdbcTemplate(dataSource));
		factoryBean.setSerializer(customSerializer());
		factoryBean.setTablePrefix("BATCH_");
		return factoryBean.getObject();
	}

	// This is need to register all jobs as they are created. Once registered, we
	// can start jobs using JobOperator
	@Bean
	public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor() {
		JobRegistryBeanPostProcessor postProcessor = new JobRegistryBeanPostProcessor();
		postProcessor.setJobRegistry(jobRegistry);
		return postProcessor;
	}
}
