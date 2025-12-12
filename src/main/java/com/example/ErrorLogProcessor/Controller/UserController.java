package com.example.ErrorLogProcessor.Controller;

import com.example.ErrorLogProcessor.Dto.auth.UserResponseDto;
import com.example.ErrorLogProcessor.Dto.auth.UserUpdateRequestDto;
import com.example.ErrorLogProcessor.Entity.User;
import com.example.ErrorLogProcessor.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user") // 사용자 관련 요청은 이 경로로 모음
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 현재 로그인한 사용자의 정보 조회
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser() {
        String loginId = getCurrentUserLoginId();
        if (loginId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // 인증 안된 경우 401 반환
        }

        User user = userService.findUserByLoginId(loginId)
                  .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없음"));

        return ResponseEntity.ok(UserResponseDto.from(user)); // from() 메서드 사용
    }

    // 현재 로그인한 사용자 정보 수정
    @PutMapping("/settings")
    public ResponseEntity<UserResponseDto> updateUserSettings(@RequestBody UserUpdateRequestDto updateDto) {
        String loginId = getCurrentUserLoginId();
        if (loginId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            User updatedUser = userService.updateUserSettings(loginId, updateDto);
            return ResponseEntity.ok(UserResponseDto.from(updatedUser)); // from() 메서드 사용
        } catch (IllegalArgumentException e) {
        	
        	return ResponseEntity.badRequest().build(); // 요청이 잘못된 경우 400 반환
        }
    }

    // JWT 토큰에서 현재 로그인한 사용자 loginId 추출 (SecurityContext 사용)
    private String getCurrentUserLoginId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return null;
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            return (String) principal;
        }
        return null;
    }

    // (필요하면) 현재 로그인한 사용자 권한 정보 추출
    private String getCurrentUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return null;
        }

        return auth.getAuthorities().stream()
                   .map(g -> g.getAuthority())
                   .collect(Collectors.joining(","));
    }
}