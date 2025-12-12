package com.example.ErrorLogProcessor.Dto.error;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorLogDto {
    private LocalDateTime timestamp;
    private String level;
    private String source;
    private String message;
    private String stackTrace;
    private String loginId; // ErrorLogController에서 자동으로 설정될 필드
}