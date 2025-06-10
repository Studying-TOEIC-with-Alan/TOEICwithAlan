package com.estsoft.project3.controller;

import com.estsoft.project3.domain.Role;
import com.estsoft.project3.domain.User;
import com.estsoft.project3.dto.SessionUser;
import com.estsoft.project3.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testUpdateUser() throws Exception {
        //given:
        User user = new User();
        user.setProvider("google");
        user.setEmail("email@test.com");
        user.setNickname("OldName");
        user.setRole(Role.ROLE_USER);
        user.setIsActive("Y");
        user = userRepository.save(user);

        SessionUser sessionUser = new SessionUser(user);

        String jsonContent = """
                {
                    "nickname": "NewNickname"
                }
                """;

        //when:
        ResultActions resultActions = mockMvc.perform(put("/api/user/{id}",user.getUserId())
                .sessionAttr("user", sessionUser)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent));

        //then:
        resultActions.andExpect(status().isOk());

        User updatedUser = userRepository.findById(user.getUserId()).orElseThrow();
        assertEquals("NewNickname", updatedUser.getNickname());
    }
}