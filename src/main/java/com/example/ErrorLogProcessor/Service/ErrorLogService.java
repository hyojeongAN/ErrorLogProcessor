package com.example.ErrorLogProcessor.Service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ErrorLogProcessor.Dto.error.DailyErrorCountDto;
import com.example.ErrorLogProcessor.Dto.error.ErrorLevelCountDto;
import com.example.ErrorLogProcessor.Entity.ErrorLog;
import com.example.ErrorLogProcessor.Repository.ErrorLogRepository;

import ch.qos.logback.classic.spi.ILoggingEvent;

@Service
public class ErrorLogService {

	private final ErrorLogRepository errorLogRepository;
	
	public ErrorLogService(ErrorLogRepository errorLogRepository) {
		this.errorLogRepository = errorLogRepository;
	}
	
	public void saveLog(ILoggingEvent event) {
        System.out.println("### SERVICE-FLOW: saveLog() 호출됨 (레벨: " + event.getLevel().toString() + ") ###");

        if (errorLogRepository == null) {
            System.err.println("### SERVICE-FLOW: errorLogRepository가 null입니다. DB 저장 불가능! ###");
            return;
        }
        
        System.out.println("### SERVICE-FLOW: DB 저장 시도 (레벨: " + event.getLevel().toString() + ") ###");
        try {
            String fullMessage = (event.getFormattedMessage() +
                                (event.getThrowableProxy() !=null ? " - " + event.getThrowableProxy().getMessage(): ""));
            
            ErrorLog errorLog = ErrorLog.builder()
                    .timestamp(LocalDateTime.now())
                    .level(event.getLevel().toString())
                    .source(event.getLoggerName())
                    .message(fullMessage)
                    .build();

            System.out.println("### SERVICE-FLOW: ErrorLog 객체 생성 완료 (메시지: " + errorLog.getMessage().substring(0, Math.min(errorLog.getMessage().length(), 50)) + "...) ###");
            
            errorLogRepository.save(errorLog);
            System.out.println("### SERVICE-FLOW: errorLogRepository.save() 호출 성공! DB 저장 완료! ###");
        } catch (Exception e) {
            System.err.println("### SERVICE-FLOW: errorLogRepository.save() 중 예외 발생! (테이블 없음, DB 연결 끊김 등) ###");
            e.printStackTrace(System.err);
        }
    }

	 
	 @Transactional(readOnly = true)
	 public List<DailyErrorCountDto> getDailyErrorCounts(LocalDateTime startDate, LocalDateTime endDate) {
		 
		 return errorLogRepository.findDailyErrorCounts(startDate, endDate);
	 }
	 
	 @Transactional(readOnly = true)
	 public List<ErrorLevelCountDto> getErrorCountsByLevel(LocalDateTime startDate, LocalDateTime endDate) {
		 
		 return errorLogRepository.findErrorCountsByLevel(startDate, endDate);
	 }
	 
}