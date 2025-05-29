package org.example.rentify.controller;

import org.example.rentify.security.UserDetailsServiceImpl;
import org.example.rentify.security.jwt.JwtUtil;
import org.example.rentify.service.*;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
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
    public JwtUtil jwtUtil() {return Mockito.mock(JwtUtil.class);}

    @Bean
    public UserDetailsServiceImpl userDetailsServiceImpl() {return Mockito.mock(UserDetailsServiceImpl.class);}

    @Bean
    public RoleService roleService() {return Mockito.mock(RoleService.class);}

    @Bean
    public ImageService imageService() {return Mockito.mock(ImageService.class);}

    @Bean
    public PropertyService propertyService() {return Mockito.mock(PropertyService.class);}

    @Bean
    public ReviewService reviewService() {return Mockito.mock(ReviewService.class);}

    @Bean
    public BookingService bookingService() {return Mockito.mock(BookingService.class);}

    @Bean
    public PaymentService paymentService() {return Mockito.mock(PaymentService.class);}

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/roles/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/properties/{propertyId}/image/all").permitAll()
                        .requestMatchers("/api/properties/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/bookings/reviews/property/{propertyId}").permitAll()
                        .requestMatchers("/api/bookings/**").permitAll()
                        .requestMatchers("/api/properties/**").permitAll()
                        .requestMatchers("/api/bookings/reviews/**").authenticated()
                        .anyRequest().authenticated()
                );
        return http.build();
    }
}