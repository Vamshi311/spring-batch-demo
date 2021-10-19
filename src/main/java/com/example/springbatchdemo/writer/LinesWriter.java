package com.example.springbatchdemo.writer;

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

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LinesWriter implements Tasklet, StepExecutionListener {

	private List<Line> lines;

	@Override
	public void beforeStep(StepExecution stepExecution) {
		lines = (List<Line>) stepExecution.getJobExecution().getExecutionContext().get("lines");
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
