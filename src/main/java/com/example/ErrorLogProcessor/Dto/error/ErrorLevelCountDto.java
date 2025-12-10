package com.example.ErrorLogProcessor.Dto.error;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorLevelCountDto {

	private String level;
	private Long count;
}
