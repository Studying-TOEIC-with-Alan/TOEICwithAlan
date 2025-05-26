package com.estsoft.project3.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public WebSecurityCustomizer configure() {      //Disable Spring Security Features
        return web -> web.ignoring().requestMatchers("/static/**","/api/**");
    }

    public WebSecurityConfig() {
    }

    //Configure web-based security for specific HTTP requests
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/images/**","/css/**","/","/home").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN") // /admin/** 경로는 ROLE_ADMIN 권한 필요
                        .anyRequest().authenticated())
                /*.oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .failureHandler((request, response, exception) -> {
                            exception.printStackTrace();
                        })
                        .loginPage("/login") // Custom login page Url
                        .defaultSuccessUrl("/checkNickname", true)
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/home")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                )*/
                .csrf(auth -> auth.disable());
        return httpSecurity.build();
    }
}