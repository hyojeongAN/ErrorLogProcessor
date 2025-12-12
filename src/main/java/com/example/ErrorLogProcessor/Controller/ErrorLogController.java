package com.example.ErrorLogProcessor.Controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors; 

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ErrorLogProcessor.Dto.error.DailyErrorCountDto;
import com.example.ErrorLogProcessor.Dto.error.ErrorLevelCountDto;
import com.example.ErrorLogProcessor.Dto.error.ErrorLogDto;
import com.example.ErrorLogProcessor.Dto.error.ErrorStatsDto;
import com.example.ErrorLogProcessor.Dto.error.FrequentErrorDto;
import com.example.ErrorLogProcessor.Entity.ErrorLog;
import com.example.ErrorLogProcessor.Repository.ErrorLogRepository;
import com.example.ErrorLogProcessor.Service.ErrorLogService;

import jakarta.persistence.criteria.CriteriaBuilder.In;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ErrorLogController {
	
	private final ErrorLogRepository errorLogrepository; 
	private final ErrorLogService errorLogService;
	
	private static final Logger log = LoggerFactory.getLogger(ErrorLogController.class);
	
	
	@PostMapping("/logs")
    public ResponseEntity<ErrorLog> createErrorLog(@RequestBody ErrorLogDto errorLogDto) {
        // 현재 인증된 사용자의 loginId를 JWT 토큰에서 추출하여 errorLogDto에 설정
        String loginId = getCurrentUserLoginId(); 
        errorLogDto.setLoginId(loginId); // DTO에 loginId 설정

        // ErrorLogService를 통해 로그 저장
        ErrorLog savedErrorLog = errorLogService.saveErrorLog(errorLogDto); 
        return new ResponseEntity<>(savedErrorLog, HttpStatus.CREATED);
    }
	
	private String getCurrentUserLoginId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return null; // 익명 사용자
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) { // JWT 토큰에서 직접 추출한 경우 String일 수 있음
            return (String) principal;
        }
        return null;
    }

    private String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return null; // 익명 사용자
        }
        return authentication.getAuthorities().stream()
                .map(g -> g.getAuthority())
                .collect(Collectors.joining(","));
    }
	
	@GetMapping("/logs/search")
	public Page<ErrorLog> searchLogs (
			@RequestParam(required = false) String level, 
			@RequestParam(required = false) String keyword, 
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate, 
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
			@PageableDefault(size = 15, sort = "timestamp", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
	
		UserPrincipal currentUser = getCurrentUserPrincipal(); 
		String currentLoginId = currentUser != null ? currentUser.getLoginId() : null;
		String currentUserRole = currentUser != null ? currentUser.getRole() : null;
		
		Specification<ErrorLog> spec = (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();
			
			if (currentLoginId != null && !"ROLE_ADMIN".equals(currentUserRole)) { 
				predicates.add(cb.equal(root.get("loginId"), currentLoginId));
			}
			
			List<String> levelsToFilter = new ArrayList<>();
            if (level != null && !level.isEmpty()) {
                levelsToFilter.add(level.toUpperCase()); // 특정 레벨 요청 시 해당 레벨만 필터링
            } else {
                // 요청 파라미터가 없으면 기본값 (ERROR, WARN, FATAL)
                levelsToFilter.add("ERROR");
                levelsToFilter.add("WARN");
                levelsToFilter.add("FATAL");
            }
            // `IN` 절을 사용하여 필터링
            In<String> inClause = cb.in(root.get("level"));
            for (String l : levelsToFilter) {
                inClause.value(l);
            }
            predicates.add(inClause);

			
			if (level != null && !level.isEmpty()) {
                predicates.add(cb.equal(root.get("level"), level.toUpperCase()));
            }
			
			if (keyword != null && !keyword.isEmpty()) {
                String likeKeyword = "%" + keyword.toLowerCase() + "%";
                Predicate messageLike = cb.like(cb.lower(root.get("message")), likeKeyword);
                Predicate sourceLike = cb.like(cb.lower(root.get("source")), likeKeyword);
                predicates.add(cb.or(messageLike, sourceLike)); 
            }
			
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
	
    private UserPrincipal getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                String loginId = ((UserDetails) principal).getUsername();
                String role = ((UserDetails) principal).getAuthorities().stream()
                                                      .map(g -> g.getAuthority())
                                                      .collect(Collectors.joining(",")); 
                return new UserPrincipal(loginId, role);
            }
        }
        return null;
    }

	private static class UserPrincipal {
		private String loginId;
		private String role;

		public UserPrincipal(String loginId, String role) {
			this.loginId = loginId;
			this.role = role;
		}

		public String getLoginId() {
			return loginId;
		}

		public String getRole() {
			return role;
		}
	}
	
	@GetMapping("/dashboard/daily-error-counts")
	public List<DailyErrorCountDto> getDailyErrorCounts(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)LocalDateTime startDate, 
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)LocalDateTime endDate) {
		
		return errorLogService.getDailyErrorCounts(startDate, endDate);
	}
	
	@GetMapping("/dashboard/error-level-counts")
	public ResponseEntity<List<ErrorLevelCountDto>> getErrorCountsByLevel(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)LocalDateTime startDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)LocalDateTime endDate) {
		
		List<ErrorLevelCountDto> errorLevelCounts = errorLogService.getErrorCountsByLevel(startDate, endDate);
		return ResponseEntity.ok(errorLevelCounts);
	}
	
	@GetMapping("/dashboard/frequent-errors")
    public ResponseEntity<List<FrequentErrorDto>> getFrequentErrors(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "5") int limit) {

        List<FrequentErrorDto> frequentErrors = errorLogService.getFrequentErrors(startDate, endDate, limit);
        return ResponseEntity.ok(frequentErrors);
    }
	
	@GetMapping("/logs/statistics")
	public ResponseEntity<ErrorStatsDto> getErrorStats() {
		String loginId = getCurrentUserLoginId();
		if (loginId == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		ErrorStatsDto stats = errorLogService.getUserErrorStatistics(loginId);
		
		return ResponseEntity.ok(stats);
	}
}