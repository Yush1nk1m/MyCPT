package com.mycpt.backend.domain.user.repository;

import com.mycpt.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// JpaRepository 상속 시 @Repository 애너테이션 없이도 Spring이 Bean으로 등록
public interface UserRepository extends JpaRepository<User, Long> {

    // SELECT * FROM users WHERE kakao_id = ?
    Optional<User> findByKakaoId(String kakaoId);
}
