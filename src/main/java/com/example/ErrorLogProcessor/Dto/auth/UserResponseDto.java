package com.example.ErrorLogProcessor.Dto.auth;

import java.time.LocalDateTime;

import com.example.ErrorLogProcessor.Entity.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
	private Long id;
	private String loginId;
	private String userName;
	private String email;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	
	public static UserResponseDto from(User user) {
		return UserResponseDto.builder()
				.id(user.getId()).loginId(user.getLoginId()).userName(user.getUserName())
				.email(user.getEmail()).createdAt(user.getCreatedAt()).updatedAt(user.getUpdatedAt()).build();
			
	}
}
