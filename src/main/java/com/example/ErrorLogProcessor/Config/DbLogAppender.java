package com.example.ErrorLogProcessor.Config;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.example.ErrorLogProcessor.Service.ErrorLogService;
import org.springframework.beans.factory.BeanCreationNotAllowedException;
import org.springframework.context.ApplicationContext;

public class DbLogAppender extends AppenderBase<ILoggingEvent> {

    @Override
    protected void append(ILoggingEvent event) {
        // 컨텍스트가 활성화되지 않았다면 DB 로깅 시도조차 하지 않음
        if (!ApplicationContextProvider.isActive()) {
            return;
        }
        
        // Appender가 시작되지 않았거나, 특정 JPA 관련 로그는 DB에 저장하지 않음 (순환 참조 방지)
        if (!isStarted() || event.getLoggerName().startsWith("org.springframework.orm.jpa")) {
            return;
        }
        
        ErrorLogService errorLogService = null;
        try {
            // 컨텍스트가 활성 상태임을 전제로 서비스를 요청
            ApplicationContext context = ApplicationContextProvider.getApplicationContext();
            if (context != null) { // null 체크 추가
                errorLogService = context.getBean(ErrorLogService.class);
            }
        } catch (BeanCreationNotAllowedException e) {
            // 서버 종료 단계 또는 빈 초기화 문제로 서비스 가져오기 실패 시 로깅 건너뜀
            // addError("Failed to get ErrorLogService during app lifetime: " + e.getMessage()); 
            return;
        } catch (Exception e) {
            addError("Unexpected error when getting ErrorLogService: " + e.getMessage());
            return;
        }
        
        if (errorLogService == null) {
            addWarn("ErrorLogService could not be retrieved. Skipping DB logging for event: " + event.getMessage());
            return;
        }

        try {
            errorLogService.saveLog(event);
        } catch (BeanCreationNotAllowedException e) {
            // saveLog 호출 중 다시 빈 생성 예외가 발생할 경우 (종료 중이거나 컨텍스트 불안정)
            addError("Bean creation not allowed during DB logging: " + event.getMessage());
        } catch (Exception e) {
            addError("Failed to save log to DB for event: " + event.getMessage(), e);
        }
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
    }
}