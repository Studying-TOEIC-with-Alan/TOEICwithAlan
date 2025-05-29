package com.estsoft.project3.configuration;

import com.estsoft.project3.handler.CustomOAuth2SuccessHandler;
import com.estsoft.project3.service.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;

    public WebSecurityConfig(CustomOAuth2UserService customOAuth2UserService) {
        this.customOAuth2UserService = customOAuth2UserService;
    }

    @Bean
    public WebSecurityCustomizer configure() {      //Disable Spring Security Features
        return web -> web.ignoring().requestMatchers("/static/**","/api/**","/js/**");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity,
        CustomOAuth2SuccessHandler customOAuth2SuccessHandler) throws Exception {
        httpSecurity
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/images/**", "/css/**", "/js/**", "/home", "/login").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                )
                .failureHandler((request, response, exception) -> {
                    exception.printStackTrace();
                })
                .successHandler(customOAuth2SuccessHandler)
                .loginPage("/login")
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/home")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            )
            .csrf(auth -> auth.disable());

        return httpSecurity.build();
    }

}