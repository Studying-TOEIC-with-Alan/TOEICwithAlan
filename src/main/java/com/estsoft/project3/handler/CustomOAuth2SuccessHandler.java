package com.estsoft.project3.handler;


import com.estsoft.project3.domain.User;
import com.estsoft.project3.dto.SessionUser;
import com.estsoft.project3.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = (String) oAuth2User.getAttributes().get("email");

        User user = userRepository.findByEmailAndIsActive(email, "Y").orElseThrow();

        request.getSession().setAttribute("user", new SessionUser(user));
        System.out.println("로그인 성공 후 세션 저장됨: " + user.getNickname());

        if (user.getNickname() == null || user.getNickname().isBlank()) {
            response.sendRedirect("/set-nickname");
        } else {
            response.sendRedirect("/");
        }

    }

}
