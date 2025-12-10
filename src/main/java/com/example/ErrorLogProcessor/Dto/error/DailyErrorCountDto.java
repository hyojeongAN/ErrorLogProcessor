package com.example.ErrorLogProcessor.Dto.error;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class DailyErrorCountDto {
	
	private LocalDate date; // 에러가 발생한 날짜 (YYYY-MM-DD)
	private Long count; // 해당 날짜에 발생한 에러 총 개수
	
	public DailyErrorCountDto(LocalDateTime timestamp, Long count) {
		this.date = timestamp != null ? timestamp.toLocalDate() : null;
		
		this.count = count;
	}
	
	public DailyErrorCountDto() {
		
	}
}
