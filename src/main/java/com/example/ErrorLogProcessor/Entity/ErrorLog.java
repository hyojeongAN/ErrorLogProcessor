package com.example.ErrorLogProcessor.Entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "error_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false, columnDefinition = "VARCHAR(1000)")
    private String message;

    @Column(nullable = false, length = 10)
    private String level;
    
    @Column(columnDefinition = "TEXT")
    private String stackTrace;

    @Column(nullable = false, updatable = false) // timestamp는 생성 후 수정되지 않으므로 updatable=false
    private LocalDateTime timestamp;

}