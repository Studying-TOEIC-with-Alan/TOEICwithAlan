package com.estsoft.project3.dto;

import com.estsoft.project3.domain.Role;
import com.estsoft.project3.domain.User;
import java.io.Serializable;
import lombok.Getter;

@Getter
public class SessionUser implements Serializable {

    private final String nickname;
    private final Role role;
    private final Long score;
    private final Long userId;

    public SessionUser(User user){
        this.nickname = user.getNickname();
        this.role = user.getRole();
        this.score = user.getScore();
        this.userId = user.getUserId();
    }


}
