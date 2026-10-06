package com.marketx.marketx;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Store login/security information in the HTTP session
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityContextRepository securityContextRepository)
            throws Exception {

        http

            // CSRF disabled because this project uses JavaScript API requests
            .csrf(csrf -> csrf.disable())

            // Save authentication in session
            .securityContext(securityContext ->
                securityContext
                    .securityContextRepository(
                        securityContextRepository
                    )
            )

            .authorizeHttpRequests(auth -> auth

                // ==========================================
                // PUBLIC FRONTEND PAGES
                // ==========================================
                .requestMatchers(
                        "/",
                        "/index.html",
                        "/login.html",
                        "/register.html",
                        "/car-list.html",
                        "/car-details.html",
                        "/add-car.html",
                        "/my-cars.html",
                        "/edit-car.html",
                        "/profile.html",
                        "/edit-profile.html"
                ).permitAll()


                // ==========================================
                // ADMIN PAGE
                // ONLY ADMIN CAN OPEN admin.html
                // ==========================================
                .requestMatchers(
                        "/admin.html"
                ).hasRole("ADMIN")


                // ==========================================
                // PUBLIC STATIC FILES
                // ==========================================
                .requestMatchers(
                        "/style.css",
                        "/script.js",
                        "/images/**",
                        "/css/**",
                        "/js/**"
                ).permitAll()


                // ==========================================
                // CAR IMAGE API
                // ==========================================

                // Anyone can view uploaded images
                .requestMatchers(
                        HttpMethod.GET,
                        "/api/images/**"
                ).permitAll()

                // Only SELLER can upload images
                .requestMatchers(
                        HttpMethod.POST,
                        "/api/images/upload"
                ).hasRole("SELLER")


                // ==========================================
                // CAR API
                // ==========================================

                // Anyone can view cars
                .requestMatchers(
                        HttpMethod.GET,
                        "/api/cars",
                        "/api/cars/**"
                ).permitAll()

                // Only SELLER can add cars
                .requestMatchers(
                        HttpMethod.POST,
                        "/api/cars"
                ).hasRole("SELLER")

                // Only SELLER can update cars
                .requestMatchers(
                        HttpMethod.PUT,
                        "/api/cars/**"
                ).hasRole("SELLER")

                // Only SELLER can delete cars
                .requestMatchers(
                        HttpMethod.DELETE,
                        "/api/cars/**"
                ).hasRole("SELLER")


                // ==========================================
                // USER LOGIN / REGISTER
                // ==========================================

                // Anyone can register
                .requestMatchers(
                        "/api/users/register"
                ).permitAll()

                // Anyone can login
                .requestMatchers(
                        "/api/users/login"
                ).permitAll()


                // ==========================================
                // ADMIN API
                // ONLY ADMIN
                // ==========================================
                .requestMatchers(
                        "/api/admin/**"
                ).hasRole("ADMIN")


                // ==========================================
                // USER API
                // Login required
                // ==========================================
                .requestMatchers(
                        "/api/users/**"
                ).authenticated()


                // ==========================================
                // DEFAULT
                // ==========================================
                .anyRequest().permitAll()
            );

        return http.build();
    }


    // ==========================================
    // AUTHENTICATION MANAGER
    // ==========================================
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration)
            throws Exception {

        return authenticationConfiguration
                .getAuthenticationManager();
    }
}