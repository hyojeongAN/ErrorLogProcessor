package com.example.ErrorLogProcessor.Config;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.example.ErrorLogProcessor.Service.ErrorLogService;

import org.springframework.context.ApplicationContext; // ApplicationContext 임포트

public class DbLogAppender extends AppenderBase<ILoggingEvent> {

    private volatile ErrorLogService errorLogServiceInstance;
    private volatile boolean serviceResolved = false;

    @Override
    public void start() {
        super.start();
        // System.out.println("### APPENDER: DbLogAppender 시작됨 ###");
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        // System.out.println("### APPENDER: append() 메서드 호출됨 ### Level: " + eventObject.getLevel().toString());
        
        if (!serviceResolved) {
            // System.out.println("### APPENDER-FLOW: 서비스 가져오기 시도 (serviceResolved=false) ###");
            
            // ApplicationContext를 먼저 안전하게 가져옴
            ApplicationContext springContext = ApplicationContextProvider.getApplicationContext();
            
            // isActive() 호출하지 않고, 컨텍스트가 null인지만 확인
            if (springContext == null) {
                // System.out.println("### APPENDER-FLOW: ApplicationContext가 아직 null임. DB 로깅 스킵 ###");
                return; // 컨텍스트 없으면 로깅 불가.
            }
            // 컨텍스트가 null이 아니면, getBean() 호출 시 예외 처리에 맡김.
            // isActive()가 또 에러를 내면 이 단계를 건너뛰는 게 최선!
            
            synchronized (this) {
                if (!serviceResolved) {
                    // System.out.println("### APPENDER-FLOW: 동기화 블록 진입. 서비스 빈 getBean 시도 ###");
                    try {
                        errorLogServiceInstance = ApplicationContextProvider.getBean(ErrorLogService.class); 
                        
                        if (errorLogServiceInstance != null) {
                            serviceResolved = true;
                            // System.out.println("### APPENDER-FLOW: ErrorLogService 빈 주입 성공! ###");
                        } else {
                            System.err.println("### APPENDER-FLOW: ErrorLogService 빈을 찾았지만 null임. (Bean 정의 문제?) ###");
                            return;
                        }
                    } catch (Exception e) { 
                        System.err.println("### APPENDER-FLOW: ErrorLogService 빈을 가져오는 중 예외 발생! ###"); 
                        e.printStackTrace(System.err); 
                        return;
                    }
                }
            }
        }
        
        if (errorLogServiceInstance != null) {
            // System.out.println("### APPENDER-FLOW: errorLogServiceInstance 유효함. DB 저장 시도 ###");
            try {
                // ILoggingEvent를 ErrorLogService의 saveLog() 메소드에 직접 전달
                errorLogServiceInstance.saveLog(eventObject); 
                // System.out.println("### APPENDER-FLOW: ErrorLogService.saveLog() 호출 성공! ###");
            } catch (Exception e) {
                System.err.println("### APPENDER-FLOW: errorLogServiceInstance.saveLog() 중 예외 발생! (DB 관련 문제일 수 있음) ###");
                e.printStackTrace(System.err); 
            }
        } else {
            // System.out.println("### APPENDER-FLOW: errorLogServiceInstance가 아직 null. DB 로깅 건너뜀 ###");
        }
    }

    @Override
    public void stop() {
        super.stop();
        // System.out.println("### APPENDER: DbLogAppender 종료됨 ###");
    }
}