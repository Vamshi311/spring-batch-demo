package com.example.springbatchdemo.model;

import java.io.Serializable;
import java.time.LocalDate;

import com.example.springbatchdemo.serializer.CustomLocalDateSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Line implements Serializable {

	private String name;
	@JsonSerialize(using = CustomLocalDateSerializer.class)
	private LocalDate dob;
	private Long age;

}
