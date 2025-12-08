package com.example.ErrorLogProcessor.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ErrorLogProcessor.Entity.ErrorLog;

public interface ErrorLogRepository extends JpaRepository<ErrorLog, Long> {

}
