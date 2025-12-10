package com.example.ErrorLogProcessor.Config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextProvider implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static <T> T getBean(Class<T> beanClass) {
        ApplicationContext context = getApplicationContext();
        
        if (context == null) { 
            return null;
        }

        if (!(context instanceof ConfigurableApplicationContext) || !((ConfigurableApplicationContext) context).isActive()) {
            return null;
        }

        int maxRetries = 5;
        long initialSleepMillis = 50;
        
        for (int i = 0; i < maxRetries; i++) {
            try {
                T bean = context.getBean(beanClass);
                if (bean != null) {
                    System.err.println("### CONTEXT-PROVIDER: getBean("+beanClass.getName()+") 성공 후 반환. ###");
                    return bean; // 빈을 성공적으로 찾으면 바로 반환
                } else {
                    System.err.println("### CONTEXT-PROVIDER-WARN: context.getBean("+beanClass.getName()+")이 null을 반환! (이런 경우는 없어야 함) 재시도 " + (i+1) + "/" + maxRetries);
                }
            } catch (BeansException e) {
                System.err.println("### CONTEXT-PROVIDER-ERROR: getBean("+beanClass.getName()+") 실패 (재시도 "+(i+1)+"/"+maxRetries+"). 상세 에러:");
                e.printStackTrace(System.err); 
            }

            try {
                Thread.sleep(initialSleepMillis * (long) Math.pow(2, i));
            } catch (InterruptedException ie) {
                System.err.println("### CONTEXT-PROVIDER-ERROR: getBean("+beanClass.getName()+") 재시도 중 Interrupted! (재시도 " + (i+1) + "/" + maxRetries + ")");
                Thread.currentThread().interrupt(); // 인터럽트 상태 복구
                // InterruptedException이 발생해도 다음 재시도까지 시도 (바로 return null 하지 않음)
            }
        }
        System.err.println("### CONTEXT-PROVIDER-ERROR-FINAL: getBean("+beanClass.getName()+") 모든 재시도 실패! 빈을 가져올 수 없음! ###");
        return null;
    }
}