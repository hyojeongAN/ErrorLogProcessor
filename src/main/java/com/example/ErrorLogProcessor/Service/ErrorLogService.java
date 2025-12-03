package com.example.ErrorLogProcessor.Service;

import com.example.ErrorLogProcessor.Entity.ErrorLog;
import com.example.ErrorLogProcessor.Repository.ErrorLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ErrorLogService {

    private final ErrorLogRepository errorLogRepository;

    public List<ErrorLog> getAllErrorLogs() {
        return errorLogRepository.findAll();
    }

    public ErrorLog saveErrorLog(ErrorLog errorLog) {
        Optional<ErrorLog> existingLog = errorLogRepository.findByTimestampAndLevelAndMessage(
                errorLog.getTimestamp(),
                errorLog.getLevel(),
                errorLog.getMessage()
        );

        if (existingLog.isEmpty()) {
            return errorLogRepository.save(errorLog);
        } else {
            System.out.println("로그 스킵 (중복): " + errorLog.getMessage());
            return existingLog.get();
        }
    }

    public void parseAndSaveErrorLog(String logLine) {
        String regex = "^\\[(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})\\] \\[([A-Z]+)\\]\\s*(.*)";
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(logLine);

        if (matcher.matches()) {
            String timestampStr = matcher.group(1);
            String level = matcher.group(2);
            String remainder = matcher.group(3).trim();

            LocalDateTime timestamp;
            try {
                timestamp = LocalDateTime.parse(timestampStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } catch (DateTimeParseException e) {
                System.err.println("🚨 에러: 타임스탬프 파싱 실패 -> " + timestampStr + " in log line: " + logLine);
                return;
            }

            String message;
            String stackTrace;

            String[] lines = remainder.split("\n", 2);

            boolean hasStackTrace = false;
            if (lines.length > 1) {
                String firstLineOfStackTrace = lines[1].trim();
                if (firstLineOfStackTrace.startsWith("at ") ||
                    firstLineOfStackTrace.startsWith("Caused by:") ||
                    firstLineOfStackTrace.startsWith("... ") ||
                    firstLineOfStackTrace.matches("^[a-zA-Z0-9_]+\\.[a-zA-Z0-9_]+\\.[a-zA-Z0-9_]+\\(.*\\.java:\\d+\\).*")
                ) {
                    hasStackTrace = true;
                }
            }

            if (hasStackTrace) {
                message = lines[0].trim();
                stackTrace = remainder.substring(lines[0].length() + 1).trim();
            } else {
                message = remainder.trim();
                stackTrace = "N/A";
            }

            String status = "NEW";

            if ("ERROR".equals(level) || "FATAL".equals(level) || "WARN".equals(level)) {
                ErrorLog errorLog = ErrorLog.builder()
                        .timestamp(timestamp)
                        .level(level)
                        .message(message)
                        .status(status)
                        .stackTrace(stackTrace)
                        .build();

                saveErrorLog(errorLog);
                System.out.println("로그 저장 성공: " + logLine.split("\n")[0]);
            } else {
                System.out.println("로그 스킵 (" + level + " 레벨): " + logLine.split("\n")[0]);
            }
        } else {
            System.err.println("🚨 에러: 로그 라인 파싱 실패 (정규식 불일치) -> " + logLine.split("\n")[0]);
        }
    }

    public List<ErrorLog> searchLogsByMessage(String keyword) {
        return errorLogRepository.findByMessageContaining(keyword);
    }

    public List<ErrorLog> filterLogsByLevel(String level) {
        return errorLogRepository.findByLevel(level);
    }

    public List<ErrorLog> filterLogsByStatus(String status) {
        return errorLogRepository.findByStatus(status);
    }

    public List<ErrorLog> searchAndfilterLogs(String keyword, String level, String status) {
        boolean hasKeyword = (keyword != null && !keyword.isEmpty());
        boolean hasLevel = (level != null && !level.isEmpty());
        boolean hasStatus = (status != null && !status.isEmpty());

        if (hasKeyword && hasLevel && hasStatus) {
            return errorLogRepository.findByMessageContainingAndLevelAndStatus(keyword, level, status);
            
        } else if (hasKeyword && hasLevel) {
            return errorLogRepository.findByMessageContainingAndLevel(keyword, level);
            
        } else if (hasKeyword && hasStatus) {
            return errorLogRepository.findByMessageContainingAndStatus(keyword, status);
            
        } else if (hasLevel && hasStatus) {
            return errorLogRepository.findByLevelAndStatus(level, status);
            
        } else if (hasKeyword) {
            return errorLogRepository.findByMessageContaining(keyword);
            
        } else if (hasLevel) {
            return errorLogRepository.findByLevel(level);
            
        } else if (hasStatus) {
            return errorLogRepository.findByStatus(status);
            
        } else {
            return errorLogRepository.findAll();
        }
    }

    public List<ErrorLog> searchLogsByTimestampBetween(LocalDateTime start, LocalDateTime end) {
        return errorLogRepository.findByTimestampBetween(start, end);
    }

	public List<ErrorLog> findAll() {
		return errorLogRepository.findAll();
	}
}