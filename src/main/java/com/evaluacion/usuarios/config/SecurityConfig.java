package com.evaluacion.usuarios.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.evaluacion.usuarios.security.JwtAuthenticationFilter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        http.csrf(csrf -> csrf.disable());
        
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/h2-console/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/webjars/**", "/error").permitAll()
            .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/usuarios").permitAll()
            .requestMatchers(org.springframework.http.HttpMethod.POST, "/auth/login").permitAll()
            .anyRequest().authenticated()
        );
        
        http.headers(headers -> headers.frameOptions(f -> f.sameOrigin()));
        
        http.sessionManagement(session -> 
            session.sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS)
        );
        http.exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(org.springframework.http.HttpStatus.UNAUTHORIZED.value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    var mapper = new ObjectMapper();
                    mapper.writeValue(response.getWriter(), new com.evaluacion.usuarios.dto.ApiError("No autorizado"));
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(org.springframework.http.HttpStatus.FORBIDDEN.value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    var mapper = new ObjectMapper();
                    mapper.writeValue(response.getWriter(), new com.evaluacion.usuarios.dto.ApiError("Acceso denegado"));
                })
        );
        
        http.addFilterBefore(jwtFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}