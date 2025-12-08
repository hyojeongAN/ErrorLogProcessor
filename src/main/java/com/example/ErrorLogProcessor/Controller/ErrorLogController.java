package com.example.ErrorLogProcessor.Controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ErrorLogProcessor.Entity.ErrorLog;
import com.example.ErrorLogProcessor.Repository.ErrorLogRepository;

@RestController
@RequestMapping("/api")
public class ErrorLogController {
	
	@Autowired
	private ErrorLogRepository errorLogrepository;
	
	private static final Logger log = LoggerFactory.getLogger(ErrorLogController.class);
	
	@GetMapping("/logs")
	public List<ErrorLog> getAllErrorLogs() {
		System.out.println("testtesttesttesttesttesttesttesttesttesttesttesttesttesttesttest");
		return errorLogrepository.findAll();
	}

    @GetMapping("/test-log")
    public String testLog() {
        System.out.println("----- /test-log 요청 수신! -----");

        try {
            log.info("인포 레벨 로그입니다. (DB에는 안 들어갈 가능성 높음)");
            log.warn("워닝 레벨 로그입니다. DB에 저장될 예정!");
            log.error("에러 레벨 로그입니다. DB 저장 테스트 예외!", new RuntimeException("강제 예외 발생!"));
            System.out.println("----- log.error()까지 실행됨 -----");

        } catch (Exception e) {
        	log.error("강제 예외 발생! DB 로깅 시도", e);
            System.err.println("!!! testLog 실행 중 캐치된 예외 (이건 의도된 동작): " + e.getMessage());
            // e.printStackTrace(); // <- 콘솔이 너무 길면 주석 처리
        }
        System.out.println("----- /test-log 응답 직전! -----");

        return "로그가 발생했습니다! 이제 DB를 확인하세요!";
    }
}