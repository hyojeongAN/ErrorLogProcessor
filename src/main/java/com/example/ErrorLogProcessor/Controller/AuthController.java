package com.example.ErrorLogProcessor.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ErrorLogProcessor.Config.jwt.JwtTokenProvider;
import com.example.ErrorLogProcessor.Dto.LoginRequestDto;
import com.example.ErrorLogProcessor.Dto.TokenDto;
import com.example.ErrorLogProcessor.Entity.User;
import com.example.ErrorLogProcessor.Service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final UserService userService;
	private final JwtTokenProvider jwtTokenProvider;
	private final AuthenticationManagerBuilder authenticationManagerBuilder;
	
	@PostMapping("/userjoin")
	public ResponseEntity<User> UserJoin(@RequestBody User user) {
		try {
			User created = userService.UserJoin(user);
			// 비밀번호는 절대 클라이언트에게 직접 노출하면 안 되므로 null 처리 또는 DTO 사용
			created.setPassword(null); // 비밀번호 필드를 비워서 반환
			return ResponseEntity.status(HttpStatus.CREATED).body(created);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(null); // 실제로는 ErrorResponseDto 사용
		}
	}
	
	@PostMapping("/login")
	public ResponseEntity<TokenDto> login(@RequestBody LoginRequestDto loginRequestDto) {
		UsernamePasswordAuthenticationToken authenticationToken = 
				new UsernamePasswordAuthenticationToken(loginRequestDto.getLoginId(), loginRequestDto.getPassword());
		
		Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
		
		String jwtToken = jwtTokenProvider.generateToken(authentication);
		
		return ResponseEntity.ok(new TokenDto(jwtToken));
	}
}
