package com.estsoft.project3.dto;

import com.estsoft.project3.domain.Role;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UserRequest {
    private Long userId;
    private String nickname;
    private Role role;
    private String isActive;
    private Long grade;
    private Long score;
    private LocalDate terminationDate;
}
