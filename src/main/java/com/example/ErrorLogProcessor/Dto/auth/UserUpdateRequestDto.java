package com.example.ErrorLogProcessor.Dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequestDto {
	private String name;
	private String email;
	private String currentPassword;
	private String newPassword;
}
