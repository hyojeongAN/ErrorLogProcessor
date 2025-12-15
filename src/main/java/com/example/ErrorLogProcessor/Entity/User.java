package com.example.ErrorLogProcessor.Entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name= "users")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "loginId", nullable = false, unique = true, length = 50)
	private String loginId;
	
	@Column(nullable = false, length = 255)
	private String password;
	
	@Column(nullable = false, length = 100)
	private String userName;
	
	@Column(nullable = false, length = 255, unique = true)
	private String email;
	
	@Column(name = "createdAt", nullable = false, updatable = false)
	private LocalDateTime createdAt;
	
	@Column(name = "updatedAt", nullable = false)
	private LocalDateTime updatedAt;
	
	@Column(nullable = false, length = 20) 
	private String role; 
	
	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}
	
	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}
	
	public String getRole() {
		return role;
	}
}