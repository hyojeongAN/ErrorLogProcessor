package com.example.ErrorLogProcessor.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/error")
    public String createTestError() {
        throw new RuntimeException("테스트용 강제 에러: 이 에러는 의도적으로 발생시킨 것입니다!");
    }
}