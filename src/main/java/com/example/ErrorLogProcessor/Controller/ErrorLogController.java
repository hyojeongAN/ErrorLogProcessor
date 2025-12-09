package com.example.ErrorLogProcessor.Controller;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ErrorLogProcessor.Entity.ErrorLog;
import com.example.ErrorLogProcessor.Repository.ErrorLogRepository;

import jakarta.persistence.criteria.Predicate;

@RestController
@RequestMapping("/api")
public class ErrorLogController {
	
	@Autowired
	private ErrorLogRepository errorLogrepository;
	
	private static final Logger log = LoggerFactory.getLogger(ErrorLogController.class);
	
//	@GetMapping("/logs")
//	public List<ErrorLog> getLogs() {
//		return errorLogrepository.findAll();
//	}
//	http://localhost:8082/api/logs/search?startDate=2025-12-01T14:21:04&endDate=2025-12-09T14:21:07
	
	
	@GetMapping("/logs/search")
	public List<ErrorLog> searchLogs (
			@RequestParam(required = false) String level, // 로그 레벨
			@RequestParam(required = false) String keyword, // 메시지 검색 키워드
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate, // 검색 시작 일시
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate ) {	// 검색 종료 일시
	
//		System.out.println("startDate : --------------------------------------" + startDate);
//		System.out.println("endDate : --------------------------------------" + endDate);
		
		Specification<ErrorLog> spec = (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();
			
//			LocalDateTime searchStartDate = startDate;
//		    LocalDateTime searchEndDate = endDate;
//		    
//		    ZoneId kstZone = ZoneId.of("Asia/Seoul");
//		    
//		    if (startDate != null) {
//		        // 백엔드가 현재 해석한 시간(LocalDateTime)을 UTC 기준으로 해석한 후,
//		        // KST로 변환하여 9시간을 되돌립니다.
//		        ZonedDateTime zdt = startDate.atZone(ZoneId.of("UTC")).withZoneSameInstant(kstZone);
//		        searchStartDate = zdt.toLocalDateTime();
//		    }
//		    
//		    if (endDate != null) {
//		        ZonedDateTime zdt = endDate.atZone(ZoneId.of("UTC")).withZoneSameInstant(kstZone);
//		        searchEndDate = zdt.toLocalDateTime();
//		    }
		    
		    
		
			// 레벨 필터링
			if (level != null && !level.isEmpty()) {
                predicates.add(cb.equal(root.get("level"), level.toUpperCase()));
            }
			
			// 키워드 검색
			if (keyword != null && !keyword.isEmpty()) {
                String likeKeyword = "%" + keyword.toLowerCase() + "%";
                Predicate messageLike = cb.like(cb.lower(root.get("message")), likeKeyword);
                Predicate sourceLike = cb.like(cb.lower(root.get("source")), likeKeyword);
                predicates.add(cb.or(messageLike, sourceLike)); // 메시지 또는 소스 중 하나라도 일치하면
            }
			
			// 기간 필터링
			if (startDate != null) {
				predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), startDate));
			}
			if (endDate != null) {
				predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), endDate));
			}
			
			// 최신 로그부터 보여주는 정렬
			query.orderBy(cb.desc(root.get("timestamp")));
			
			return cb.and(predicates.toArray(new Predicate[0]));
		
		};

		return errorLogrepository.findAll(spec);
	}
	
	
	
	
//	@RestController
//	@RequestMapping("/api")
//	public class SimpleApiController {
//
//	    @GetMapping("/hello")
//	    public String hello() {
//	        return "Hello, MingMyangMyeong!";
//	    }
//	    
//	}

	
	
//    @GetMapping("/test-log")
//    public String testLog() {
//        System.out.println("----- /test-log 요청 수신! -----");
//
//        try {
//            log.info("인포 레벨 로그입니다. (DB에는 안 들어갈 가능성 높음)");
//            log.warn("워닝 레벨 로그입니다. DB에 저장될 예정!");
//            log.error("에러 레벨 로그입니다. DB 저장 테스트 예외!", new RuntimeException("강제 예외 발생!"));
//            System.out.println("----- log.error()까지 실행됨 -----");
//
//        } catch (Exception e) {
//        	log.error("강제 예외 발생! DB 로깅 시도", e);
//            System.err.println("!!! testLog 실행 중 캐치된 예외 (이건 의도된 동작): " + e.getMessage());
//            // e.printStackTrace(); // <- 콘솔이 너무 길면 주석 처리
//        }
//        System.out.println("----- /test-log 응답 직전! -----");
//
//        return "로그가 발생했습니다! 이제 DB를 확인하세요!";
//    }
}