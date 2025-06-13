package com.estsoft.project3.configuration;

import com.estsoft.project3.handler.CustomOAuth2SuccessHandler;
import com.estsoft.project3.service.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;

    public WebSecurityConfig(CustomOAuth2UserService customOAuth2UserService) {
        this.customOAuth2UserService = customOAuth2UserService;
    }

    @Bean
    public WebSecurityCustomizer configure() {      //Disable Spring Security Features
        return web -> web.ignoring().requestMatchers("/static/**", "/api/**");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity,
        CustomOAuth2SuccessHandler customOAuth2SuccessHandler,
        ClientRegistrationRepository clientRegistrationRepository) throws Exception {
        httpSecurity
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/images/**", "/css/**", "/js/**", "/home", "/login", "/set-nickname").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(endpoint -> endpoint
                    .authorizationRequestResolver(
                        customAuthorizationRequestResolver(clientRegistrationRepository)
                    )
                )
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                )
                .failureHandler((request, response, exception) -> {
                    exception.printStackTrace();
                })
                .successHandler(customOAuth2SuccessHandler)
                .loginPage("/login").permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            )
            .csrf(auth -> auth.disable());

        return httpSecurity.build();
    }

    private OAuth2AuthorizationRequestResolver customAuthorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository) {

        DefaultOAuth2AuthorizationRequestResolver defaultResolver =
                new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization");

        return new OAuth2AuthorizationRequestResolver() {
            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
                OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request);
                return customizeAuthorizationRequest(authorizationRequest);
            }

            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
                OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request, clientRegistrationId);
                return customizeAuthorizationRequest(authorizationRequest);
            }
        };
    }

    private OAuth2AuthorizationRequest customizeAuthorizationRequest(OAuth2AuthorizationRequest request) {
        if (request == null) return null;

        Map<String, Object> additionalParameters = new HashMap<>(request.getAdditionalParameters());
        additionalParameters.put("prompt", "select_account");

        return OAuth2AuthorizationRequest.from(request)
                .additionalParameters(additionalParameters)
                .build();
    }
}