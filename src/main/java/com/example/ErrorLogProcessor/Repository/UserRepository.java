package com.example.ErrorLogProcessor.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.ErrorLogProcessor.Entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByLoginId(String loginId);
	
	Optional<User> findByEmail(String email);
	
	boolean existsByLoginId(String loginId);
	boolean existsByEmail(String email);
	
}
