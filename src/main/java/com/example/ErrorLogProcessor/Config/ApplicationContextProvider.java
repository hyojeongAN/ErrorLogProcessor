package com.example.ErrorLogProcessor.Config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component // ⬅️ 이 어노테이션을 반드시 추가해야 Spring Bean으로 등록됨
public class ApplicationContextProvider implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    // Spring이 이 Bean을 초기화할 때 Context를 정적으로 저장
    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        System.out.println("### CONTEXT-PROVIDER: setApplicationContext 호출됨. Context 설정 완료. ###");
        applicationContext = context;
    }

    // 외부(DbLogAppender)에서 Context를 가져올 수 있는 정적 메서드
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    // Appender가 Service Bean을 가져오는 헬퍼 메서드
    public static <T> T getBean(Class<T> beanClass) {
        ApplicationContext context = getApplicationContext();
        if (context == null) {
            return null;
        }
        try {
            return context.getBean(beanClass);
        } catch (BeansException e) {
            // Bean을 찾을 수 없는 경우
            return null;
        }
    }
}