package com.estsoft.project3.repository;

import com.estsoft.project3.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
