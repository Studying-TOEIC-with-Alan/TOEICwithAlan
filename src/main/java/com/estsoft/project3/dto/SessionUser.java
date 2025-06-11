package com.estsoft.project3.dto;

import com.estsoft.project3.domain.Role;
import com.estsoft.project3.domain.User;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SessionUser implements Serializable {

    private String nickname;
    private Role role;
    private Long score;
    private final Long userId;
    private Long grade;
    private String isActive;

    public SessionUser(User user){
        this.nickname = user.getNickname();
        this.role = user.getRole();
        this.score = user.getScore();
        this.userId = user.getUserId();
        this.grade = user.getGrade();
        this.isActive = user.getIsActive();
    }


}
