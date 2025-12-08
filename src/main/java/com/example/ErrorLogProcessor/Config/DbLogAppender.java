package com.example.ErrorLogProcessor.Config;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.example.ErrorLogProcessor.Service.ErrorLogService;


public class DbLogAppender extends  AppenderBase<ILoggingEvent> {

    private volatile ErrorLogService errorLogServiceInstance;
    private volatile boolean serviceResolved = false;

    @Override
    public void start() {
        super.start();
        System.out.println("### APPENDER: DbLogAppender 시작됨 ###"); // 확인 3
    }

	@Override
	protected void append(ILoggingEvent event) {
		System.out.println("### APPENDER: append() 메서드 호출됨 ### Level: " + event.getLevel().toString()); // 확인 4
		
		// 서비스 빈을 아직 못 가져왔다면 시도함
        if (!serviceResolved) {
            System.out.println("### APPENDER-FLOW: 서비스 가져오기 시도 (serviceResolved=false) ###"); // 확인 5
            
            // **ApplicationContextProvider를 통해 Context 준비 여부 확인**
            if (ApplicationContextProvider.getApplicationContext() == null) {
                System.out.println("### APPENDER-FLOW: ApplicationContextProvider Context가 아직 null임. 스킵 ###"); 
                return; // 컨텍스트 없으면 로깅 불가.
            }
            
            synchronized (this) { // 멀티쓰레딩에서 안전하게 한 번만 초기화
                if (!serviceResolved) {
                    System.out.println("### APPENDER-FLOW: 동기화 블록 진입. 서비스 빈 getBean 시도 ###"); // 확인 8
                    try {
                        // **ApplicationContextProvider를 사용하여 Bean 로드**
                        errorLogServiceInstance = ApplicationContextProvider.getBean(ErrorLogService.class); 
                        
                        if (errorLogServiceInstance != null) {
                            serviceResolved = true; // 서비스 주입 성공
                            System.out.println("### APPENDER-FLOW: ErrorLogService 빈 주입 성공! ###"); // 확인 9
                        } else {
                            System.err.println("### APPENDER-FLOW: ErrorLogService 빈을 찾았지만 null임. (Bean 정의 문제?) ###");
                            return;
                        }
                    } catch (Exception e) { // Exception으로 광범위하게 캐치
                        System.err.println("### APPENDER-FLOW: ErrorLogService 빈을 가져오는 중 예외 발생! ###"); 
                        e.printStackTrace(System.err); 
                        return;
                    }
                }
            }
        }
        
        // serviceResolved가 true이고 serviceInstance가 있다면 DB 저장 시도
        if (errorLogServiceInstance != null) {
            System.out.println("### APPENDER-FLOW: errorLogServiceInstance 유효함. saveLog 호출 직전 ###"); // 확인 11
            try {
                errorLogServiceInstance.saveLog(event);
                System.out.println("### APPENDER-FLOW: errorLogServiceInstance.saveLog() 호출 성공! ###"); // 확인 12
            } catch (Exception e) {
                System.err.println("### APPENDER-FLOW: errorLogServiceInstance.saveLog() 중 예외 발생! (DB 관련 문제일 수 있음) ###"); // 확인 13
                e.printStackTrace(System.err); 
            }
        } else {
            System.out.println("### APPENDER-FLOW: errorLogServiceInstance가 아직 null. DB 로깅 건너뜀 ###"); // 확인 14
        }
    }
	@Override
    public void stop() {
        super.stop();
        System.out.println("### APPENDER: DbLogAppender 종료됨 ###");
    }
}