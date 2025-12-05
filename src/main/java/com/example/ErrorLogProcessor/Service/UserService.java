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
	
	// 아이디, 이메일 중복 확인
	@Transactional
	public User UserJoin(UserJoinRequestDto userJoinRequestDto) {
		if (userRepository.findByLoginId(userJoinRequestDto.getLoginId()).isPresent()) {
			throw new IllegalArgumentException("이미 사용 중인 로그인 아이입니다.");
		}
		
		if (userRepository.findByEmail(userJoinRequestDto.getEmail()).isPresent()) {
			throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
		}
		
		String encodedPassword = passwordEncoder.encode(userJoinRequestDto.getPassword());
		
		User newUser = User.builder()
				.loginId(userJoinRequestDto.getLoginId())
				.password(encodedPassword) // 암호화된 비밀번호 저장
				.userName(userJoinRequestDto.getUserName())
				.email(userJoinRequestDto.getEmail())
				.build();
		
		return userRepository.save(newUser);
	}
	
	public Optional<User> findUserByLoginId(String loginId) {
		return userRepository.findByLoginId(loginId);
		
	}
	
	/**
     * 사용자 정보 수정 로직 (이름, 이메일, 비밀번호 변경 포함)
     * @param loginId       수정할 사용자 로그인 아이디
     * @param updateRequestDto 수정 요청 DTO
     * @return 수정된 User 엔티티
     * @throws IllegalArgumentException 사용자를 찾을 수 없거나 현재 비밀번호 불일치 시
     */
	@Transactional
	public User updateUserSettings(String loginId, UserUpdateRequestDto updateRequestDto) {
		 User user = userRepository.findByLoginId(loginId)
	                .orElseThrow(() -> new IllegalArgumentException("해당하는 유저를 찾을 수 없습니다."));
		
		// 1. 이름 및 이메일 업데이트 (null이 아닌 경우만 업데이트)
	        Optional.ofNullable(updateRequestDto.getName()).ifPresent(user::setUserName);
	        Optional.ofNullable(updateRequestDto.getEmail()).ifPresent(user::setEmail);

	        // 2. 비밀번호 변경 요청이 있다면 처리
	        if (updateRequestDto.getNewPassword() != null && !updateRequestDto.getNewPassword().isEmpty()) {
	            // 현재 비밀번호 확인
	            if (updateRequestDto.getCurrentPassword() == null || !passwordEncoder.matches(updateRequestDto.getCurrentPassword(), user.getPassword())) {
	                throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
	            }
	            // 새 비밀번호 암호화 후 저장
	            user.setPassword(passwordEncoder.encode(updateRequestDto.getNewPassword()));
	        }

	        // 3. 변경 사항 DB 저장 (자동으로 updatedAt 갱신)
	        return userRepository.save(user);
	    }

}
