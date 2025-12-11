package com.example.ErrorLogProcessor.Service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ErrorLogProcessor.Config.jwt.JwtTokenProvider;
import com.example.ErrorLogProcessor.Dto.auth.LoginRequestDto;
import com.example.ErrorLogProcessor.Dto.auth.TokenDto;
import com.example.ErrorLogProcessor.Dto.auth.UserJoinRequestDto;
import com.example.ErrorLogProcessor.Entity.User;
import com.example.ErrorLogProcessor.Repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
	
	private final UserRepository userRepository;
	private final UserService userService;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final AuthenticationManager authenticationManager;
	
	@Transactional
	public User UserJoin(UserJoinRequestDto userJoinRequestDto) {
		return userService.UserJoin(userJoinRequestDto);
	}
	
	@Transactional
	public TokenDto login(LoginRequestDto loginRequestDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getLoginId(),
                        loginRequestDto.getPassword()
                )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
		
        String loginId = authentication.getName(); 
        String jwt = jwtTokenProvider.generateToken(loginId);
		
		return TokenDto.builder().token(jwt).build();
	}
}