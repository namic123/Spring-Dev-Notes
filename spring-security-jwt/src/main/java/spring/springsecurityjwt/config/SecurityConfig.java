package spring.springsecurityjwt.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import spring.springsecurityjwt.jwt.CustomLoginFilter;
import spring.springsecurityjwt.jwt.CustomLogoutFilter;
import spring.springsecurityjwt.jwt.JWTFilter;
import spring.springsecurityjwt.jwt.JWTUtil;
import spring.springsecurityjwt.repository.RefreshRepository;

import java.util.Collections;
/**
 * Spring Security 설정 클래스입니다.
 * 인증(Authentication) 및 인가(Authorization) 설정을 담당합니다.
 */
@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** AuthenticationManager 생성을 위한 AuthenticationConfiguration 객체 */
    private final AuthenticationConfiguration authenticationConfiguration;

    /** JWT 관련 유틸리티 클래스 */
    private final JWTUtil jwtUtil;

    /** Refresh Token 관리를 위한 Repository */
    private RefreshRepository refreshRepository;

    /**
     * AuthenticationManager 빈 등록
     * @param configuration AuthenticationConfiguration 객체
     * @return AuthenticationManager
     * @throws Exception 예외 발생 가능성
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * 비밀번호 암호화를 위한 BCryptPasswordEncoder 빈 등록
     * @return BCryptPasswordEncoder
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Spring Security FilterChain 설정
     * @param http HttpSecurity 객체
     * @return SecurityFilterChain
     * @throws Exception 예외 발생 가능성
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        /** CORS 설정 */
        http.cors(corsCustomizer -> corsCustomizer.configurationSource(new CorsConfigurationSource() {
            @Override
            public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000")); // 허용할 도메인
                configuration.setAllowedMethods(Collections.singletonList("*")); // 모든 HTTP 메서드 허용
                configuration.setAllowCredentials(true); // 쿠키 등 자격증명 허용
                configuration.setAllowedHeaders(Collections.singletonList("*")); // 모든 요청 헤더 허용
                configuration.setMaxAge(3600L); // preflight 캐시 시간 (초)
                configuration.setExposedHeaders(Collections.singletonList("Authorization")); // 응답 헤더 노출
                return configuration;
            }
        }));

        /** CSRF 비활성화 (JWT는 세션을 사용하지 않으므로) */
        http.csrf(csrf -> csrf.disable());

        /** 폼 로그인 비활성화 */
        http.formLogin(form -> form.disable());

        /** HTTP Basic 인증 비활성화 */
        http.httpBasic(httpBasic -> httpBasic.disable());

        /** 요청별 접근 제어 설정 */
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/", "/join").permitAll() // 로그인, 메인, 회원가입은 인증 없이 접근 허용
                .anyRequest().authenticated()); // 나머지 요청은 인증 필요

        /** JWT 인증 필터 추가 */
        http.addFilterBefore(new JWTFilter(jwtUtil), CustomLoginFilter.class);

        /** 커스텀 로그인 필터 추가 */
        http.addFilterAt(new CustomLoginFilter(authenticationManager(authenticationConfiguration), jwtUtil), UsernamePasswordAuthenticationFilter.class);

        /** 커스텀 로그아웃 필터 추가 */
        http.addFilterBefore(new CustomLogoutFilter(jwtUtil, refreshRepository), LogoutFilter.class);

        /** 세션을 STATELESS(비상태)로 설정: 서버에 세션 저장 안 함 */
        http.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}