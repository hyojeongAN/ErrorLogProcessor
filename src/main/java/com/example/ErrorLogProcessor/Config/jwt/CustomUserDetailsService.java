package com.example.ErrorLogProcessor.Config.jwt;

import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.ErrorLogProcessor.Entity.User;
import com.example.ErrorLogProcessor.Repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService {

	private final UserRepository userRepository;
	
	public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
		User user = userRepository.findByLoginId(loginId)
				.orElseThrow(() -> new UsernameNotFoundException("해당하는 유저를 찾을 수 없습니다:" + loginId));
		
		GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");
		return new org.springframework.security.core.userdetails.User(
				user.getLoginId(), user.getPassword(), Collections.singleton(authority));
	}
}
