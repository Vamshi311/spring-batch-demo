package com.example.springbatchdemo;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.springbatchdemo.model.Line;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@SpringBootTest
class SpringBatchDemoApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	public void testObjectMapper() throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		Line line = Line.builder().name("John").dob(LocalDate.now().minusYears(25)).build();
		String value = objectMapper.writeValueAsString(line);
		System.out.println("value is " + value);
	}

}
