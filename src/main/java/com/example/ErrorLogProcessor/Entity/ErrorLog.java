package com.example.ErrorLogProcessor.Entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "error_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private LocalDateTime timestamp;
	
	@Column(length = 20)
	private String level;
	
	@Column(length = 255)
	private String source;
	
	@Column(columnDefinition = "TEXT")
	private String message;
	
}