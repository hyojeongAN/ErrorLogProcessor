package com.example.ErrorLogProcessor.Dto.error;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyErrorCountDto {
    private String date;
    private Long count;
}