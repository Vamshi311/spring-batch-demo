package com.example.springbatchdemo.reader;

import java.util.ArrayList;
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
import com.example.springbatchdemo.util.DataUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LinesReader implements Tasklet, StepExecutionListener {

	@Autowired
	private ObjectMapper objectMapper;

	private List<Line> lines = new ArrayList<Line>();

	@Override
	public void beforeStep(StepExecution stepExecution) {
		log.info("Lines Reader initialized");
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		try {
			stepExecution.getJobExecution().getExecutionContext().put("lines", objectMapper.writeValueAsString(lines));
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.info("Lines Reader ended");
		return ExitStatus.COMPLETED;
	}

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		lines = DataUtils.getLines();
		log.info("Line Reader retrieved lines");
		return RepeatStatus.FINISHED;
	}

}
