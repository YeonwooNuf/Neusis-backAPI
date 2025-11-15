package com.neusis.backapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF는 일단 비활성화 (API + 세션 개발 단계)
                .csrf(AbstractHttpConfigurer::disable)
                // CORS는 우리가 만든 CorsConfig 사용
                .cors(Customizer.withDefaults())
                // 일단 모든 요청 허용 (인증은 우리가 직접 HttpSession으로 처리)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                // 기본 폼 로그인, HTTP Basic 비활성화 (팝업/로그인페이지 막기)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }
}