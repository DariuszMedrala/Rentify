package org.example.rentify.controller;

import org.example.rentify.security.UserDetailsServiceImpl;
import org.example.rentify.security.jwt.JwtUtil;
import org.example.rentify.service.RoleService;
import org.example.rentify.service.UserService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;


@TestConfiguration
@EnableMethodSecurity(prePostEnabled = true)
public class ControllerTestConfig {

    @Bean
    public UserService userService() {
        return Mockito.mock(UserService.class);
    }

    @Bean
    public JwtUtil jwtUtil() {
        return Mockito.mock(JwtUtil.class);
    }

    @Bean
    public UserDetailsServiceImpl userDetailsServiceImpl() {
        return Mockito.mock(UserDetailsServiceImpl.class);
    }

    @Bean
    public RoleService roleService() {
        return Mockito.mock(RoleService.class);
    }

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated()
                );
        return http.build();
    }
}