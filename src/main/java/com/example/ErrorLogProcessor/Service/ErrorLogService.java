package com.example.ErrorLogProcessor.Service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ErrorLogProcessor.Entity.ErrorLog;
import com.example.ErrorLogProcessor.Repository.ErrorLogRepository;

import ch.qos.logback.classic.spi.ILoggingEvent;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ErrorLogService {

	private final ErrorLogRepository errorLogRepository;
	
	 public void saveLog(ILoggingEvent event) {
	        System.out.println("### SERVICE-FLOW: saveLog() 호출됨 (레벨: " + event.getLevel().toString() + ") ###"); // 확인 16

	        if (errorLogRepository == null) {
	            System.err.println("### SERVICE-FLOW: errorLogRepository가 null입니다. DB 저장 불가능! ###"); // 확인 17
	            // 이게 null이라면 ErrorLogService 자체 초기화 문제일 수 있음.
	            return;
	        }
	        
	        if (event.getLevel().isGreaterOrEqual(ch.qos.logback.classic.Level.WARN)) {
	            System.out.println("### SERVICE-FLOW: WARN 이상 레벨. DB 저장 시도 ###"); // 확인 18
	            try {
	            	String fullMessage = (event.getFormattedMessage() +
	            			(event.getThrowableProxy() !=null ? " - " + event.getThrowableProxy().getMessage(): ""));
	            	
	                ErrorLog errorLog = ErrorLog.builder()
	                        .timestamp(LocalDateTime.now())
	                        .level(event.getLevel().toString())
	                        .source(event.getLoggerName())
	                        .message(event.getFormattedMessage() +
	                                (event.getThrowableProxy() != null ? " - " + event.getThrowableProxy().getMessage() : ""))
	                        .build();

	                System.out.println("### SERVICE-FLOW: ErrorLog 객체 생성 완료 (메시지: " + errorLog.getMessage().substring(0, Math.min(errorLog.getMessage().length(), 50)) + "...) ###"); // 확인 19
	                
	                errorLogRepository.save(errorLog);
	                System.out.println("### SERVICE-FLOW: errorLogRepository.save() 호출 성공! DB 저장 완료! ###"); // 확인 20
	            } catch (Exception e) {
	                System.err.println("### SERVICE-FLOW: errorLogRepository.save() 중 예외 발생! (테이블 없음, DB 연결 끊김 등) ###"); // 확인 21
	                e.printStackTrace(System.err);
	            }
	        } else {
	            System.out.println("### SERVICE-FLOW: INFO 레벨. DB 저장 건너뜀 ###"); // 확인 22
	        }
	    }
	}