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
import org.springframework.stereotype.Component;

import com.example.springbatchdemo.model.Line;
import com.example.springbatchdemo.util.DataUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LinesReader implements Tasklet, StepExecutionListener {

	private List<Line> lines = new ArrayList<Line>();

	@Override
	public void beforeStep(StepExecution stepExecution) {
		log.info("Lines Reader initialized");
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		stepExecution.getJobExecution().getExecutionContext().put("lines", lines);
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
