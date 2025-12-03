package com.example.ErrorLogProcessor.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ErrorLogProcessor.Entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByLoginId(String loginId);
}
