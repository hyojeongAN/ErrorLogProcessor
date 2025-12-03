package com.example.ErrorLogProcessor.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ErrorLogProcessor.Dto.UserSettingUpdateDto;
import com.example.ErrorLogProcessor.Entity.User;
import com.example.ErrorLogProcessor.Service.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;


@RestController
@RequestMapping("/api/user") // 사용자 관련 API
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;
	
	@GetMapping("/settings/{loginId}")
	public ResponseEntity<User> getUserSettings(@PathVariable String loginId) {
		
		try {
			User user = userService.getUserSettings(loginId); // 유지 정보용 DTO 만들기
			return ResponseEntity.ok(user);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.notFound().build(); // 사용자를 찾을 수 없을 때 404
		}
	}

	@PutMapping("/settings/{loginId}")
	public ResponseEntity<User> updateUserSettings(
			@PathVariable String loginId, @RequestBody UserSettingUpdateDto settingUpdateDto) {

		try {
			User updatedUser = userService.updateUserSettings(loginId, settingUpdateDto);
			return ResponseEntity.ok(updatedUser);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.notFound().build();
		}
	}
}
