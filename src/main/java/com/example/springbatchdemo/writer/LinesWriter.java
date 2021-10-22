package com.example.springbatchdemo.writer;

import java.util.List;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.springbatchdemo.model.Line;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LinesWriter implements Tasklet, StepExecutionListener {

	@Autowired
	private ObjectMapper objectMapper;
	private List<Line> lines;

	@Override
	public void beforeStep(StepExecution stepExecution) {
		String linesValue = (String) stepExecution.getJobExecution().getExecutionContext().get("lines");
		try {
			lines = objectMapper.readValue(linesValue, new TypeReference<List<Line>>() {
			});
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.info("Lines writer initialized");

	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		log.info("Line writer ended");
		return ExitStatus.COMPLETED;
	}

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		lines.stream().forEach(line -> log.info("Line {}", line));
		log.info("Finished Line writer execute phase");
		return RepeatStatus.FINISHED;
	}

}
