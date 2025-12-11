package com.example.ErrorLogProcessor.Repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.ErrorLogProcessor.Dto.error.DailyErrorCountDto;
import com.example.ErrorLogProcessor.Dto.error.ErrorLevelCountDto;
import com.example.ErrorLogProcessor.Dto.error.FrequentErrorDto;
import com.example.ErrorLogProcessor.Entity.ErrorLog;

public interface ErrorLogRepository extends JpaRepository<ErrorLog, Long>, JpaSpecificationExecutor<ErrorLog> {

	@Query("SELECT el FROM ErrorLog el " +
	           "WHERE (:level IS NULL OR el.level = :level) " +
	           "AND (:keyword IS NULL OR el.message LIKE %:keyword% OR el.source LIKE %:keyword%) " +
	           "AND (:startDate IS NULL OR el.timestamp >= :startDate) " +
	           "AND (:endDate IS NULL OR el.timestamp <= :endDate) " +
	           "AND el.level NOT IN ('INFO', 'DEBUG') " + 
	           "ORDER BY el.timestamp DESC")
	    Page<ErrorLog> searchErrorLogs(
	            @Param("level") String level,
	            @Param("keyword") String keyword,
	            @Param("startDate") LocalDateTime startDate,
	            @Param("endDate") LocalDateTime endDate,
	            Pageable pageable);
	
	@Query("SELECT new com.example.ErrorLogProcessor.Dto.error.DailyErrorCountDto(el.timestamp, COUNT(el.id)) " + // el.timestamp 그대로 넘김
	           "FROM ErrorLog el " +
	           "WHERE el.timestamp BETWEEN :startDate AND :endDate " +
	           "AND el.level NOT IN ('INFO', 'DEBUG', 'TRACE') " +
	           "AND (:loginId IS NULL OR el.loginId = :loginId) " +
	           "GROUP BY FUNCTION('DATE_FORMAT', el.timestamp, '%Y-%m-%d') " +
	           "ORDER BY FUNCTION('DATE_FORMAT', el.timestamp, '%Y-%m-%d') ASC")
	List<DailyErrorCountDto> findDailyErrorCounts(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("loginId") String loginId);
	
	@Query("SELECT new com.example.ErrorLogProcessor.Dto.error.ErrorLevelCountDto(el.level, COUNT(el.id)) " +
			"FROM ErrorLog el " +
			"WHERE el.timestamp BETWEEN :startDate AND :endDate " +
			"AND el.level NOT IN ('INFO', 'DEBUG', 'TRACE') " +
			"AND (:loginId IS NULL OR el.loginId = :loginId) " +
			"GROUP BY el.level ORDER BY COUNT(el.id) DESC")
	List<ErrorLevelCountDto> findErrorCountsByLevel(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("loginId") String loginId);

	@Query("SELECT new com.example.ErrorLogProcessor.Dto.error.FrequentErrorDto(el.message, COUNT(el.id)) " +
			"FROM ErrorLog el " +
			"WHERE el.timestamp BETWEEN :startDate AND :endDate " +
			"AND el.level NOT IN ('INFO', 'DEBUG', 'TRACE') " + // INFO, DEBUG, TRACE 레벨은 제외하고 센트럴 로그레벨로 설정
			"AND (:loginId IS NULL OR el.loginId = :loginId) " +
			"GROUP BY el.message " +
			"ORDER BY COUNT(el.id) DESC, el.message ASC")
	List<FrequentErrorDto> findFrequentErrors(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable, @Param("loginId") String loginId);

}
