package com.example.ErrorLogProcessor.Controller;

//import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.ErrorLogProcessor.Entity.ErrorLog;
import com.example.ErrorLogProcessor.Service.ErrorLogService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173") // 리액트 개발 서버 주소에 맞게 수정 가능
public class ErrorLogController {

    private final ErrorLogService errorLogService;

//    @GetMapping("/add-test")
//    public String addTestErrorLog() {
//        LocalDateTime now = LocalDateTime.now();
//
//        ErrorLog testLog = ErrorLog.builder()
//                .message("Test Error : DB connection failed.")
//                .level("ERROR")
//                .timestamp(now)
//                .status("NEW")
//                .stackTrace("java.lang.NullPointerException: Cannot invoke "
//                        + "\"java.lang.Object.toString()\" because \"myObject\" "
//                        + "is null\n\tat com.example.MyService."
//                        + "doSomething(MyService.java:45)\n\tat "
//                        + "com.example.MyController.handleRequest(MyController.java:23)")
//                .build();
//
//        errorLogService.saveErrorLog(testLog);
//        return "테스트 에러 로그 저장 성공: " + testLog.getMessage();
//    }

    @PostMapping("/parse-and-save")
    public ResponseEntity<String> receiveLog(@RequestBody Map<String, String> payload) {
        String logLine = payload.get("logLine");

        if (logLine == null || logLine.trim().isEmpty()) {
            return new ResponseEntity<>("로그 문자열이 비어있습니다.", HttpStatus.BAD_REQUEST);
        }

        try {
            errorLogService.parseAndSaveErrorLog(logLine);
            return new ResponseEntity<>("로그 파싱 및 저장 성공 (첫 줄): " + logLine.split("\n")[0], HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("에러: 로그 처리 중 예외 발생 -> " + e.getMessage());
            return new ResponseEntity<>("로그 처리 중 에러 발생: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/search")
    public List<ErrorLog> searchLogs(@RequestParam String keyword) {
        return errorLogService.searchLogsByMessage(keyword);
    }

    @GetMapping("/filter")
    public List<ErrorLog> filterLogs(@RequestParam String level) {
        return errorLogService.filterLogsByLevel(level);
    }

    @GetMapping
    public List<ErrorLog> getLogs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String status) {
        return errorLogService.searchAndfilterLogs(keyword, level, status);
    }
    
    @GetMapping("/logs")
    public List<ErrorLog> getAllLogs() {
    	return errorLogService.findAll();
    }
}