package com.example.ErrorLogProcessor.Dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserJoinRequestDto {
	private String name;
	private String loginId;
	private String password;
	private String email;
}
