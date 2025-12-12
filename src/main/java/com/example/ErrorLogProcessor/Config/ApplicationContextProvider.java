package com.example.ErrorLogProcessor.Config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextProvider implements ApplicationContextAware {

    private static ApplicationContext context; // 애플리케이션 컨텍스트
    private static boolean contextInitialized = false; // 컨텍스트 초기화 여부

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
        contextInitialized = true;
    }

    public static ApplicationContext getApplicationContext() {
        return context;
    }

    public static boolean isActive() {
        // 컨텍스트 자체가 null이거나 아직 초기화되지 않았다면 false 반환
        if (context == null || !contextInitialized) {
            return false;
        }

        // ConfigurableApplicationContext로 안전하게 캐스팅하여 isActive() 및 isRunning() 호출
        if (context instanceof ConfigurableApplicationContext) {
            ConfigurableApplicationContext configurableContext = (ConfigurableApplicationContext) context;
            // 컨텍스트가 현재 활성 상태이고 (isRunning()이 true이거나 해당 상태인 경우)
            // 아직 종료 과정에 들어가지 않았는지 확인 (configurableContext.isActive()는 종료 시 false가 될 수 있음)
            // isRunning()은 컨텍스트가 완전히 시작되어 외부 요청을 처리할 수 있는 상태인지 확인 (종료 요청이 오지 않은 상태)
            return configurableContext.isActive() && configurableContext.isRunning();
        }
        
        // 일반 ApplicationContext의 경우 (isActive() 메서드가 없으므로, 기본적으로 true로 간주하거나 더 보수적으로 false로 간주)
        // 여기서는 해당 컨텍스트가 완전한 lifecycle 관리 대상이 아니므로, isActive()를 호출할 수 없음
        // 따라서, 안전하게 true로 간주하여, 다음 로직에서 빈 요청을 시도하게 함. (만약 isActive()를 명시적으로 지원하지 않는 컨텍스트라면)
        // 하지만 Spring Boot의 일반적인 사용 사례에서는 context가 대부분 ConfigurableApplicationContext의 인스턴스임
        // 더 보수적으로, isActive()를 지원하지 않는 경우 False 반환하도록 수정 (실제로 발생하기 어려움)
        return false; // 매우 보수적인 접근
    }
}