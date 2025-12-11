package com.example.ErrorLogProcessor.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ErrorLogProcessor.Dto.error.DailyErrorCountDto;
import com.example.ErrorLogProcessor.Dto.error.ErrorLevelCountDto;
import com.example.ErrorLogProcessor.Dto.error.FrequentErrorDto;
import com.example.ErrorLogProcessor.Entity.ErrorLog;
import com.example.ErrorLogProcessor.Repository.ErrorLogRepository;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ErrorLogService {

	private final ErrorLogRepository errorLogRepository;
	
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
	    if (isAdmin()) {
	        return errorLogRepository.findDailyErrorCounts(startDate, endDate, null); // 관리자는 전체 조회
	    }
	    String loginId = getCurrentUserLoginId();
	    return errorLogRepository.findDailyErrorCounts(startDate, endDate, loginId); // 일반 사용자는 본인 조회
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
		 if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
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
}