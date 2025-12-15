package com.example.ErrorLogProcessor.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ErrorLogProcessor.Dto.error.DailyErrorCountDto;
import com.example.ErrorLogProcessor.Dto.error.ErrorLevelCountDto;
import com.example.ErrorLogProcessor.Dto.error.ErrorLogDto;
import com.example.ErrorLogProcessor.Dto.error.ErrorStatsDto;
import com.example.ErrorLogProcessor.Dto.error.FrequentErrorDto;
import com.example.ErrorLogProcessor.Entity.ErrorLog;
import com.example.ErrorLogProcessor.Entity.User;
import com.example.ErrorLogProcessor.Repository.ErrorLogRepository;
import com.example.ErrorLogProcessor.Repository.UserRepository;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ErrorLogService {

	private final ErrorLogRepository errorLogRepository;
	private final UserRepository userRepository;
	
	//외부 클라이언트로부터 HTTP 요청으로 받는 에러를 저장하는 메서드 (loginId 자동 포함)
	@Transactional
    public ErrorLog saveErrorLog(ErrorLogDto errorLogDto) {
        ErrorLog errorLog = ErrorLog.builder()
                .timestamp(errorLogDto.getTimestamp() != null ? errorLogDto.getTimestamp() : LocalDateTime.now())
                .level(errorLogDto.getLevel())
                .source(errorLogDto.getSource())
                .message(errorLogDto.getMessage())
                .stackTrace(errorLogDto.getStackTrace())
                .loginId(errorLogDto.getLoginId()) // Controller에서 설정된 loginId 사용
                .build();
        return errorLogRepository.save(errorLog);
    }
	
	// 백엔드 자체의 로깅 이벤트를 저장
	@Transactional
	public void saveLog(ILoggingEvent event) {
		String stackTrace = null;
		if(event.getThrowableProxy() != null && event.getThrowableProxy() instanceof ThrowableProxy) {
			ThrowableProxy tp = (ThrowableProxy) event.getThrowableProxy();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			tp.getThrowable().printStackTrace(pw);
			stackTrace = sw.toString();
		}
		
		String loginId = null;
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication != null && authentication.isAuthenticated() && !"anonymosUser".equals(authentication.getPrincipal())) {
				Object principal = authentication.getPrincipal();
				if (principal instanceof UserDetails) {
					loginId = ((UserDetails) principal).getUsername();
				} else if (principal instanceof String) {
					loginId = (String) principal;
				}
			}
		} catch (Exception e) {
			System.err.println("Failed to get authentication for logging: " + e.getMessage());
		}
		
		 ErrorLog errorLog = ErrorLog.builder()
	                .level(event.getLevel().toString())
	                .message(event.getFormattedMessage())
	                .source(event.getLoggerName())
	                .timestamp(LocalDateTime.now())
	                .stackTrace(stackTrace)
	                .loginId(loginId)
	                .build();
	        errorLogRepository.save(errorLog);
	}
	 
	@Transactional(readOnly = true)
	public List<DailyErrorCountDto> getDailyErrorCounts(LocalDateTime startDate, LocalDateTime endDate) {
	    List<DailyErrorCountDto> projections;
	    if (isAdmin()) {
	        projections = errorLogRepository.findDailyErrorCounts(startDate, endDate, null); // 관리자는 전체 조회
	    } else {
	        String loginId = getCurrentUserLoginId();
	        projections = errorLogRepository.findDailyErrorCounts(startDate, endDate, loginId); // 일반 사용자는 본인 조회
	    }
        // Projection 결과를 DTO로 변환
        return projections.stream()
                .map(p -> new DailyErrorCountDto(p.getDate(), p.getCount()))
                .collect(Collectors.toList());
	}
	 
	@Transactional(readOnly = true)
	public List<ErrorLevelCountDto> getErrorCountsByLevel(LocalDateTime startDate, LocalDateTime endDate) {
	    if (isAdmin()) {
	        return errorLogRepository.findErrorCountsByLevel(startDate, endDate, null);
	    }
	    String loginId = getCurrentUserLoginId();
	    return errorLogRepository.findErrorCountsByLevel(startDate, endDate, loginId);
	}
	 
	 @Transactional
	 public List<FrequentErrorDto> getFrequentErrors(LocalDateTime startDate, LocalDateTime endDate, int limit) {
	     Pageable pageable = PageRequest.of(0, limit);

	     if (isAdmin()) {
	         return errorLogRepository.findFrequentErrors(startDate, endDate, pageable, null); 
	     }
	     
	     String loginId = getCurrentUserLoginId();
	     return errorLogRepository.findFrequentErrors(startDate, endDate, pageable, loginId);
	 }
	 
	 private String getCurrentUserLoginId() {
		 Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		 if (authentication != null && authentication.isAuthenticated() && !"anonymosUser".equals(authentication.getPrincipal())) {
			 Object principal = authentication.getPrincipal();
			 if (principal instanceof UserDetails) {
				 return ((UserDetails) principal).getUsername();
			 } else if (principal instanceof String) {
				 return (String) principal;
			 }
		 }
		 return null; // 로그인하지 않은 상태이거나 인증 정보가 불완전하면 null 반환
	 }
	 
	 private boolean isAdmin() {
		 Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		 if (authentication != null && authentication.isAuthenticated()) {
			 return authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
		 }
		 return false;
	 }
	 
	 public ErrorStatsDto getUserErrorStatistics(String loginId) {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime since = now.minusDays(30);
		LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
		
		int recentErrorCount = errorLogRepository.countRecentErrors(loginId, since);
		int todayErrorCount = errorLogRepository.countTodayErrors(loginId, startOfToday);
		 
		 return new ErrorStatsDto(recentErrorCount, todayErrorCount);
		 
	 }
	 
		public long countRecentOneHourErrorsForCurrentUser(String currentUserLoginId) {

			System.out.println("서비스 : 파라미터로 받은 loginId -> " + currentUserLoginId);

			LocalDateTime now = LocalDateTime.now();
			LocalDateTime oneHourAgo = now.minusHours(1);
			System.out.println("서비스:현재 시간 -> " + now + ", 1시간 전 시간 -> " + oneHourAgo);
			String userRole = userRepository.findByLoginId(currentUserLoginId).map(User::getRole).orElse("USER");

			System.out.println("서비스: 사용자 " + currentUserLoginId + "의 역할 -> " + userRole);

			long count;
// 2. 역할에 따라 다른 레파지토리 메서드 호출!
			if ("ROLE_ADMIN".equals(userRole)) { // 역할이 "ROLE_ADMIN"이라면
	            System.out.println("서비스: 관리자이므로 전체 에러 개수를 가져옴.");
	            count = errorLogRepository.countByTimestampBetween(oneHourAgo, now);
	        } else {
	            System.out.println("서비스: 일반 사용자이므로 해당 loginId의 에러 개수를 가져옴.");
	            count = errorLogRepository.countByTimestampBetweenAndLoginId(oneHourAgo, now, currentUserLoginId);
	        }

	        System.out.println("서비스: 최종 반환된 에러 개수 -> " + count);
	        return count;
	    }
}