package com.estsoft.project3.repository;

import com.estsoft.project3.domain.User;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUserIdAndIsActive(Long userId, String isActive);
    User findByNickname(String nickname);
    Page<User> findByNicknameContaining(String keyword, Pageable pageable);
    Page<User> findAll(Pageable pageable);
}
