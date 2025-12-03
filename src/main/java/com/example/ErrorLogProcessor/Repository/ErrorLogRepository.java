package com.example.ErrorLogProcessor.Repository;

import com.example.ErrorLogProcessor.Entity.ErrorLog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
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
	
	Optional<ErrorLog> findByTimestampAndLevelAndMessage(LocalDateTime timestamp, String level, String message);

	List<ErrorLog> findByStatus(String status);
}
