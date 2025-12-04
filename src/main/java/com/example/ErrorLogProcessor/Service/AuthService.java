package com.example.ErrorLogProcessor.Service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ErrorLogProcessor.Config.jwt.CustomUserDetails;
import com.example.ErrorLogProcessor.Config.jwt.JwtTokenProvider;
import com.example.ErrorLogProcessor.Dto.auth.LoginRequestDto;
import com.example.ErrorLogProcessor.Dto.auth.TokenDto;
import com.example.ErrorLogProcessor.Dto.auth.UserJoinRequestDto;
import com.example.ErrorLogProcessor.Entity.User;
import com.example.ErrorLogProcessor.Repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor // final 필드를 이용한 생성자 자동 주입
public class AuthService {
	
	private final UserRepository userRepository; // 사용자 중복 확인 등에 사용
	private final UserService userService; // 실제 사용자 저장 로직 호출
	private final PasswordEncoder passwordEncoder; // 비밀번호 암호화 및 비교
	private final JwtTokenProvider jwtTokenProvider; // JWT 토큰 생성 및 검증
	private final AuthenticationManager authenticationManager;
	
	 /**
     * 회원가입 로직
     * UserService의 UserJoin 메서드를 호출하여 사용자 생성
     * @param userJoinRequestDto 회원가입 요청 DTO
     * @return 생성된 User 엔티티
     * @throws IllegalArgumentException 아이디/이메일 중복 등 UserJoin에서 발생하는 예외
     */
	@Transactional
	public User UserJoin(UserJoinRequestDto userJoinRequestDto) {
		// 실제 회원가입 로직은 UserService에게 위임 (중복 확인, 비밀번호 암호화 등)
		return userService.UserJoin(userJoinRequestDto);
	}
	
	 /**
     * 로그인 로직
     * 사용자 인증 후 JWT 토큰 발급
     * @param loginRequestDto 로그인 요청 DTO (로그인 아이디, 비밀번호)
     * @return 발급된 JWT 토큰 정보 (TokenDto)
     * @throws IllegalArgumentException 인증 실패 시 발생
     */
	@Transactional
	public TokenDto login(LoginRequestDto loginRequestDto) {
		// 1. AuthenticationManager를 통해 인증 시도
        // ID와 PW로 Authentication 객체 생성
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getLoginId(),
                        loginRequestDto.getPassword()
                )
        );
        
        // 2. 인증 성공 후 SecurityContextHolder에 인증 객체 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);
		
		 // 3. 사용자 인증 정보 기반으로 JWT 토큰 생성
        String loginId = ((CustomUserDetails) authentication.getPrincipal()).getUsername(); // CustomUserDetails를 이용해 loginId 가져오기
        String jwt = jwtTokenProvider.generateToken(loginId);
		
		// 4. 생성된 토큰 반환
		return TokenDto.builder().token(jwt).build();
	}
}
