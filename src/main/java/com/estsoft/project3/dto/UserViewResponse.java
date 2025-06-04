package com.estsoft.project3.dto;


import com.estsoft.project3.domain.Role;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserViewResponse {

    private Long userId;
    private String provider;
    private String email;
    private String nickname;
    private Role role;
    private String isActive;
    private Long grade;
    private Long score;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

}
