package com.example.ErrorLogProcessor.Repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.ErrorLogProcessor.Dto.error.DailyErrorCountDto;
import com.example.ErrorLogProcessor.Dto.error.ErrorLevelCountDto;
import com.example.ErrorLogProcessor.Dto.error.FrequentErrorDto;
import com.example.ErrorLogProcessor.Entity.ErrorLog;

@Repository
public interface ErrorLogRepository extends JpaRepository<ErrorLog, Long>, JpaSpecificationExecutor<ErrorLog> {

    @Query("SELECT el FROM ErrorLog el " +
               "WHERE (:level IS NULL OR el.level = :level) " +
               "AND (:keyword IS NULL OR el.message LIKE %:keyword% OR el.source LIKE %:keyword%) " +
               "AND (:startDate IS NULL OR el.timestamp >= :startDate) " +
               "AND (:endDate IS NULL OR el.timestamp <= :endDate) " +
               "ORDER BY el.timestamp DESC")
    Page<ErrorLog> searchErrorLogs(
            @Param("level") String level,
            @Param("keyword") String keyword,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
    
    @Query(value = "SELECT DATE_FORMAT(el.timestamp, '%Y-%m-%d') as date, COUNT(el.id) as count " + 
            "FROM error_logs el " +
            "WHERE el.timestamp BETWEEN :startDate AND :endDate AND (:loginId IS NULL OR el.login_id = :loginId) " +
            "GROUP BY DATE_FORMAT(el.timestamp, '%Y-%m-%d') " +
            "ORDER BY DATE_FORMAT(el.timestamp, '%Y-%m-%d')", nativeQuery = true)
    List<DailyErrorCountDto> findDailyErrorCounts(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("loginId") String loginId);
    
    @Query("SELECT new com.example.ErrorLogProcessor.Dto.error.ErrorLevelCountDto(el.level, COUNT(el)) " +
           "FROM ErrorLog el WHERE el.timestamp BETWEEN :startDate AND :endDate AND (:loginId IS NULL OR el.loginId = :loginId) " +
           "GROUP BY el.level")
    List<ErrorLevelCountDto> findErrorCountsByLevel(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("loginId") String loginId);

    @Query("SELECT new com.example.ErrorLogProcessor.Dto.error.FrequentErrorDto(el.message, COUNT(el), el.level) " +
            "FROM ErrorLog el " +
            "WHERE el.timestamp BETWEEN :startDate AND :endDate AND (:loginId IS NULL OR el.loginId = :loginId) " +
            "GROUP BY el.message, el.level " +
            "ORDER BY COUNT(el) DESC, el.level ASC")
    List<FrequentErrorDto> findFrequentErrors(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable, @Param("loginId") String loginId);
    
    @Query("SELECT COUNT(e) FROM ErrorLog e WHERE e.loginId = :loginId AND e.timestamp >= :since")
    int countRecentErrors(@Param("loginId") String loginId, @Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(e) FROM ErrorLog e WHERE e.loginId = :loginId AND e.timestamp >= :startOfToday")
    int countTodayErrors(@Param("loginId") String loginId, @Param("startOfToday") LocalDateTime StartOfToday);
}