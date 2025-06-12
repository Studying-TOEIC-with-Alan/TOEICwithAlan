package com.estsoft.project3.service;

import com.estsoft.project3.domain.Role;
import com.estsoft.project3.domain.User;
import com.estsoft.project3.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // Google 사용자 정보
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String provider = userRequest.getClientRegistration().getRegistrationId();

        // 기존 회원인지 확인
        User user = userRepository.findByEmailAndIsActive(email, "Y")
                .orElseGet(() -> getOrRestoreTerminatedUser(email, provider)
                        .orElseGet(() -> createNewUser(email, provider))
                );

        // OAuth2User 반환 (세션용)
        return new DefaultOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority(user.getRole().name())),
            attributes,
            "email" // principal 이름으로 쓸 key
        );

    }

    private Optional<User> getOrRestoreTerminatedUser(String email, String provider) {
        return userRepository.findByEmailAndIsActiveAndTerminationDateAfter(
                email,
                "N",
                LocalDate.now().minusDays(7)
        ).map(user -> {
            user.setIsActive("Y");
            user.setTerminationDate(null);
            return userRepository.save(user);
        });
    }

    private User createNewUser(String email, String provider) {
        return userRepository.save(User.builder()
                .provider(provider)
                .email(email)
                .nickname(null)
                .role(Role.ROLE_USER)
                .isActive("Y")
                .build());
    }
}
