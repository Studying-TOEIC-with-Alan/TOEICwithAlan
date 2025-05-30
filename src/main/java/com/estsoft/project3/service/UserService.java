package com.estsoft.project3.service;

import com.estsoft.project3.domain.User;
import com.estsoft.project3.dto.UserRequest;
import com.estsoft.project3.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserById(Long userId) {
        User user = userRepository.findByUserIdAndIsActive(userId, "Y")
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        return user;
    }

    public void updateUserById(Long userId, UserRequest userRequest) {
        User user = userRepository.findByUserIdAndIsActive(userId, "Y")
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        if (!user.getNickname().equals(userRequest.getNickname())) {
            User userByNickname = userRepository.findByNickname(userRequest.getNickname());
            if (userByNickname != null) {
                throw new RuntimeException("닉네임 중복");
            }
        }

        if (userRequest.getNickname() != null) {
            user.setNickname(userRequest.getNickname());
        }

        if(userRequest.getGrade() != null) {
            user.setGrade(userRequest.getGrade());
        }

        if(userRequest.getScore() != null) {
            user.setScore(userRequest.getScore());
        }

        if(userRequest.getIsActive() != null) {
            user.setIsActive(userRequest.getIsActive());
        }

        userRepository.save(user);
    }

}
