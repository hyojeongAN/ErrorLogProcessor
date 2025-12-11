package com.example.ErrorLogProcessor.Config.jwt; 

import com.example.ErrorLogProcessor.Entity.User;
import com.example.ErrorLogProcessor.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // 관리자 계정이 이미 존재하는지 확인 후 생성
        if (userRepository.findByLoginId("admin").isEmpty()) {
            User admin = new User();
            admin.setLoginId("admin");
            admin.setPassword(passwordEncoder.encode("adminPassword")); 
            admin.setRole("ROLE_ADMIN"); 
            admin.setEmail("admin@example.com"); 
            admin.setUserName("관리자"); 
            userRepository.save(admin);
            System.out.println("관리자 계정 'admin'이 생성되었습니다. ID: admin / PW: adminPassword / Email: admin@example.com");
        }
        // 일반 사용자 계정이 이미 존재하는지 확인 후 생성
        if (userRepository.findByLoginId("user").isEmpty()) {
            User normalUser = new User();
            normalUser.setLoginId("user");
            normalUser.setPassword(passwordEncoder.encode("userPassword")); 
            normalUser.setRole("ROLE_USER"); 
            normalUser.setEmail("user@example.com"); 
            normalUser.setUserName("일반유저"); 
            userRepository.save(normalUser);
            System.out.println("일반 사용자 계정 'user'가 생성되었습니다. ID: user / PW: userPassword / Email: user@example.com");
        }
    }
}