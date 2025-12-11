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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ErrorLogProcessor.Dto.error.DailyErrorCountDto;
import com.example.ErrorLogProcessor.Dto.error.ErrorLevelCountDto;
import com.example.ErrorLogProcessor.Dto.error.FrequentErrorDto;
import com.example.ErrorLogProcessor.Entity.ErrorLog;
import com.example.ErrorLogProcessor.Repository.ErrorLogRepository;
import com.example.ErrorLogProcessor.Service.ErrorLogService;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ErrorLogController {
	
	private final ErrorLogRepository errorLogrepository; 
	private final ErrorLogService errorLogService;
	
	private static final Logger log = LoggerFactory.getLogger(ErrorLogController.class);
	
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
			
			predicates.add(root.get("level").in(
                "INFO", "DEBUG", "TRACE", "WARN", "FATAL", "CONFIG"
                ).not()
            );
			
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
}