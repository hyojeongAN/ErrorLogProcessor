package com.example.ErrorLogProcessor.Config.jwt;

import com.example.ErrorLogProcessor.Entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections; // 빈 컬렉션 반환용

public class CustomUserDetails implements UserDetails {
	
	private static final long serialVersionUID = 1L;

    private final User user; // User 엔티티를 주입받음

    public CustomUserDetails(User user) {
        this.user = user;
    }

    // 사용자에게 부여된 권한 목록을 반환
    // 지금은 단순화를 위해 권한을 따로 관리하지 않고, 빈 컬렉션 반환
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 나중에 권한 관리가 필요하면 여기에 구현
        return Collections.emptyList();
    }

    // 사용자의 비밀번호 반환
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    // 사용자의 고유한 아이디(여기서는 loginId) 반환
    @Override
    public String getUsername() { // Spring Security에서는 'username'을 사용
        return user.getLoginId();
    }

    // 계정 만료 여부 (true: 만료되지 않음)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 계정 잠금 여부 (true: 잠금되지 않음)
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // 비밀번호 만료 여부 (true: 만료되지 않음)
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 계정 활성화 여부 (true: 활성화됨)
    @Override
    public boolean isEnabled() {
        return true;
    }

    // CustomUserDetails를 통해 실제 User 엔티티에 접근할 수 있는 getter
    public User getUser() {
        return user;
    }
}