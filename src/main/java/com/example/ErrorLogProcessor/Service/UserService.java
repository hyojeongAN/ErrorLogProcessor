package com.example.ErrorLogProcessor.Service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ErrorLogProcessor.Dto.auth.UserJoinRequestDto;
import com.example.ErrorLogProcessor.Dto.auth.UserUpdateRequestDto;
import com.example.ErrorLogProcessor.Entity.User;
import com.example.ErrorLogProcessor.Repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	
	@Transactional
	public User UserJoin(UserJoinRequestDto userJoinRequestDto) {
		if (userRepository.findByLoginId(userJoinRequestDto.getLoginId()).isPresent()) {
			throw new IllegalArgumentException("이미 사용 중인 로그인 아이디입니다.");
		}
		
		if (userRepository.findByEmail(userJoinRequestDto.getEmail()).isPresent()) {
			throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
		}
		
		String encodedPassword = passwordEncoder.encode(userJoinRequestDto.getPassword());
		
		User newUser = User.builder()
				.loginId(userJoinRequestDto.getLoginId())
				.password(encodedPassword)
				.userName(userJoinRequestDto.getUserName())
				.email(userJoinRequestDto.getEmail())       
				.role("ROLE_USER") 
				.build();
		
		return userRepository.save(newUser);
	}
	
	public Optional<User> findUserByLoginId(String loginId) {
		return userRepository.findByLoginId(loginId);
		
	}
	
	@Transactional
	public User updateUserSettings(String loginId, UserUpdateRequestDto updateRequestDto) {
		 User user = userRepository.findByLoginId(loginId)
	                .orElseThrow(() -> new IllegalArgumentException("해당하는 유저를 찾을 수 없습니다."));
		
	        Optional.ofNullable(updateRequestDto.getName()).ifPresent(user::setUserName);
	        Optional.ofNullable(updateRequestDto.getEmail()).ifPresent(user::setEmail);

	        if (updateRequestDto.getNewPassword() != null && !updateRequestDto.getNewPassword().isEmpty()) {
	            if (updateRequestDto.getCurrentPassword() == null || !passwordEncoder.matches(updateRequestDto.getCurrentPassword(), user.getPassword())) {
	                throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
	            }
	            user.setPassword(passwordEncoder.encode(updateRequestDto.getNewPassword()));
	        }

	        return userRepository.save(user);
	    }
}