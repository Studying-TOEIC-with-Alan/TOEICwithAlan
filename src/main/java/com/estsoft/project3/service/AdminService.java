package com.estsoft.project3.service;

import com.estsoft.project3.domain.User;
import com.estsoft.project3.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;

    public void updateUserGradeAndScore(Long userId, Long grade, Long score) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        user.setGrade(grade);
        user.setScore(score);
        userRepository.save(user);
    }

    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Page<User> findByNicknameContaining(String nickname, Pageable pageable) {
        return userRepository.findByNicknameContaining(nickname, pageable);
    }


}
