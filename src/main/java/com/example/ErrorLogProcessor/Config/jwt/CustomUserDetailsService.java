package com.example.ErrorLogProcessor.Config.jwt;

import com.example.ErrorLogProcessor.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // 사용자 정보를 로드할 때 호출하는 메서드
    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        // userRepository를 이용해 DB에서 loginId에 해당하는 User를 찾음
        // 찾지 못하면 UsernameNotFoundException 발생
        return userRepository.findByLoginId(loginId)
                .map(CustomUserDetails::new) // User 엔티티를 CustomUserDetails 객체로 변환
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + loginId));
    }
}