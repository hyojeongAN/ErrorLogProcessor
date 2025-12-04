package com.example.ErrorLogProcessor.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ErrorLogProcessor.Dto.auth.LoginRequestDto;
import com.example.ErrorLogProcessor.Dto.auth.TokenDto;
import com.example.ErrorLogProcessor.Dto.auth.UserJoinRequestDto;
import com.example.ErrorLogProcessor.Dto.auth.UserResponseDto;
import com.example.ErrorLogProcessor.Service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	
	/**
     * 회원가입 API
     * POST /api/auth/userjoin
     * @param userJoinRequestDto 회원가입 요청 DTO
     * @return 성공 시 UserResponseDto와 HTTP 201 Created 반환
     * @throws IllegalArgumentException 아이디/이메일 중복 시
     */
	@PostMapping("/userjoin")
	public ResponseEntity<UserResponseDto> UserJoin(@RequestBody UserJoinRequestDto userJoinRequestDto) {
		
		// AuthService를 통해 회원가입 처리 후 UserResponseDto로 변환하여 반환
        UserResponseDto responseDto = UserResponseDto.from(authService.UserJoin(userJoinRequestDto));
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto); // 201 Created 상태 코드와 함께 응답
	}
	
	/**
     * 로그인 API
     * POST /api/auth/login
     * @param loginRequestDto 로그인 요청 DTO
     * @return 성공 시 TokenDto (JWT)와 HTTP 200 OK 반환
     * @throws IllegalArgumentException 로그인 실패 시 (아이디/비밀번호 불일치)
     */
	@PostMapping("/login")
	public ResponseEntity<TokenDto> login(@RequestBody LoginRequestDto loginRequestDto) {
		
		 // AuthService를 통해 로그인 처리 후 JWT 토큰 반환
		TokenDto tokenDto = authService.login(loginRequestDto);
		return ResponseEntity.ok(tokenDto);  // 200 OK 상태 코드와 함께 응답
	}
}
