package com.example.ErrorLogProcessor.Service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.ErrorLogProcessor.Dto.UserSettingUpdateDto;
import com.example.ErrorLogProcessor.Entity.User;
import com.example.ErrorLogProcessor.Repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	
	public User UserJoin(User user) {
		// 회원가입시 loginId 중복 확인
		if (userRepository.findByLoginId(user.getLoginId()).isPresent()) {
			throw new IllegalArgumentException("이미 사용 중인 로그인 아이디입니다.");
		}
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		return userRepository.save(user);
	}
	
	// id로 사용자 조회
	public Optional<User> findByLoginId(String loginId) {
		return userRepository.findByLoginId(loginId);
	}
	
	public User getUserSettings(String loginId) {
		return userRepository.findByLoginId(loginId)
					.orElseThrow(() -> new IllegalArgumentException("해당 로그인 아이디의 사용자를 찾을 수 없습니다."));
	}

	public User updateUserSettings(String loginId, UserSettingUpdateDto settingUpdateDto) {
		return userRepository.findByLoginId(loginId).map(user -> {
				user.setEnableEmailNotifications(settingUpdateDto.isEnableEmailNotifications());
				user.setEnableWebAppNotifications(settingUpdateDto.isEnableWebAppNotifications());
				user.setPreferredTheme(settingUpdateDto.getPreferredTheme());
				return userRepository.save(user);
		})
		.orElseThrow(() -> new IllegalArgumentException("해당 로그인 아이디의 사용자를 찾을 수 없습니다."));
	}

}
