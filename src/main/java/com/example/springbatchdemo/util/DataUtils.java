package com.example.springbatchdemo.util;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.example.springbatchdemo.model.Line;

public class DataUtils {

	public static List<Line> getLines() {

		List<Line> lines = new ArrayList<Line>();
		
		Line line = Line.builder().name("John").dob(LocalDate.now().minusYears(25)).build();
		lines.add(line);

		line = Line.builder().name("Kerry").dob(LocalDate.now().minusYears(28)).build();
		lines.add(line);

		line = Line.builder().name("Bell").dob(LocalDate.now().minusYears(35)).build();
		lines.add(line);

		line = Line.builder().name("Catyln").dob(LocalDate.now().minusYears(45)).build();
		lines.add(line);

		line = Line.builder().name("Denver").dob(LocalDate.now().minusYears(55)).build();
		lines.add(line);

		return lines;
	}
}
