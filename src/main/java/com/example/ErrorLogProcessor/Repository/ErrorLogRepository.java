package com.example.ErrorLogProcessor.Repository;

import com.example.ErrorLogProcessor.Entity.ErrorLog; // ErrorLog 엔티티 임포트

import java.time.LocalDateTime; // LocalDateTime 임포트 (timestamp Between 용)
import java.util.List; // List 임포트
import java.util.Optional; // Optional 임포트 (단일 결과 반환 시)

import org.springframework.data.jpa.repository.JpaRepository; // JpaRepository 임포트
import org.springframework.stereotype.Repository; // @Repository 어노테이션 임포트

@Repository // 스프링 빈으로 등록되도록 어노테이션 붙이기!
public interface ErrorLogRepository extends JpaRepository<ErrorLog, Long> {

	// 특정 키워드가 포함된 에러 로그 검색
	List<ErrorLog> findByMessageContaining(String keyword);
	
	// 특정 레벨의 에러 로그 검색
	List<ErrorLog> findByLevel(String level);
	
	// 특정 키워드와 레벨을 동시에 만족하는 에러 로그 검색
	List<ErrorLog> findByMessageContainingAndLevel(String keyword, String level);

    // 특정 키워드와 상태를 동시에 만족하는 에러 로그 검색
    List<ErrorLog> findByMessageContainingAndStatus(String keyword, String status);

    // 특정 레벨과 상태를 동시에 만족하는 에러 로그 검색
    List<ErrorLog> findByLevelAndStatus(String level, String status);

    // 메시지 키워드, 레벨, 상태를 모두 만족하는 에러 로그 검색
    List<ErrorLog> findByMessageContainingAndLevelAndStatus(String keyword, String level, String status);
	
	// 특정 기간 내의 에러 로그 검색 (타임스탬프 기준으로 시작/끝 시간 사이)
	List<ErrorLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
	
	// 세 가지 필드(timestamp, level, message)의 값이 모두 정확히 일치하는 errorlog 한 개만 찾아서 반환
    // 이전에는 findByTimestampAndMessage였는데, 여기에 level까지 포함시켜서 더 명확하게 검색!
	Optional<ErrorLog> findByTimestampAndLevelAndMessage(LocalDateTime timestamp, String level, String message);

	List<ErrorLog> findByStatus(String status);
}
