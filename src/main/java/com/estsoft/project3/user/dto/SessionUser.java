package com.estsoft.project3.user.dto;

import com.estsoft.project3.user.domain.Role;
import com.estsoft.project3.user.domain.User;
import java.io.Serializable;
import lombok.Getter;

@Getter
public class SessionUser implements Serializable {

    private final String nickname;
    private final Role role;
    private final Long score;

    public SessionUser(User user){
        this.nickname = user.getNickname();
        this.role = user.getRole();
        this.score = user.getScore();
    }

}
