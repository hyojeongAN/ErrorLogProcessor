package com.example.ErrorLogProcessor.Controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ErrorLogProcessor.Dto.error.DailyErrorCountDto;
import com.example.ErrorLogProcessor.Dto.error.ErrorLevelCountDto;
import com.example.ErrorLogProcessor.Entity.ErrorLog;
import com.example.ErrorLogProcessor.Repository.ErrorLogRepository;
import com.example.ErrorLogProcessor.Service.ErrorLogService;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ErrorLogController {
	
	private final ErrorLogRepository errorLogrepository; // 주입 (사용 여부는 상황에 따라 달라짐)
	private final ErrorLogService errorLogService;
	
	private static final Logger log = LoggerFactory.getLogger(ErrorLogController.class);
	
//	@GetMapping("/test-log")
//	public String testLog() {
//		log.info("### API 호출 테스트 INFO 로그입니다! ###"); 
//		log.debug("### API 호출 테스트 DEBUG 로그입니다! ###"); 
//		log.warn("### API 호출 테스트 WARN 로그입니다! ###");
//		log.error("### API 호출 테스트 ERROR 로그입니다! ###");
//		return "OK! Check your backend console logs and MariaDB error_log table.";
//	}
	
	@GetMapping("/logs/search")
	public Page<ErrorLog> searchLogs (
			@RequestParam(required = false) String level, // 로그 레벨
			@RequestParam(required = false) String keyword, // 메시지 검색 키워드
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate, // 검색 시작 일시
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
			@PageableDefault(size = 15, sort = "timestamp", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {	// 검색 종료 일시
	
		Specification<ErrorLog> spec = (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();
			
			predicates.add(root.get("level").in("INFO", "DEBUG").not());
			
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
			
			return cb.and(predicates.toArray(new Predicate[0]));
		
		};

		return errorLogrepository.findAll(spec, pageable);
	}
	
	@GetMapping("/dashboard/daily-error-counts")
	public List<DailyErrorCountDto> getDailyErrorCounts(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)LocalDateTime startDate, // ✨ ISO.DATE_TIME으로 변경됨
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)LocalDateTime endDate) { // ✨ ISO.DATE_TIME으로 변경됨
		
		return errorLogService.getDailyErrorCounts(startDate, endDate);
	}
	
	@GetMapping("/dashboard/error-level-counts")
	public ResponseEntity<List<ErrorLevelCountDto>> getErrorCountsByLevel(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)LocalDateTime startDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)LocalDateTime endDate) {
		
		List<ErrorLevelCountDto> errorLevelCounts = errorLogService.getErrorCountsByLevel(startDate, endDate);
		
		return ResponseEntity.ok(errorLevelCounts);
	}
}