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
@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    //AuthenticationManager가 인자로 받을 AuthenticationConfiguraion 객체 생성자 주입
    private final AuthenticationConfiguration authenticationConfiguration;

    private final JWTUtil jwtUtil;

    private RefreshRepository refreshRepository;


    //AuthenticationManager Bean 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .cors((corsCustomizer -> corsCustomizer.configurationSource(new CorsConfigurationSource() {

                    @Override
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {

                        CorsConfiguration configuration = new CorsConfiguration();

                        configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000")); // 허용 도메인
                        configuration.setAllowedMethods(Collections.singletonList("*")); // 모든 HTTP 메서드 허용 (GET, POST, PUT 등)
                        configuration.setAllowCredentials(true); // 자격 증명 허용 (쿠키, 인증 헤더 포함 허용)
                        configuration.setAllowedHeaders(Collections.singletonList("*")); // 모든 헤더 허용
                        configuration.setMaxAge(3600L); // CORS preflight 요청 결과 캐시 시간(초)
                        configuration.setExposedHeaders(Collections.singletonList("Authorization")); // 응답에 노출시킬 헤더 지정

                        return configuration;
                    }
                })));

        http
                .csrf((auth) -> auth.disable());

        http
                .formLogin((auth) -> auth.disable());

        http
                .httpBasic((auth) -> auth.disable());

        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/login", "/", "/join").permitAll()
                        .anyRequest().authenticated());

        http
                .addFilterBefore(new JWTFilter(jwtUtil), CustomLoginFilter.class);

//필터 추가 LoginFilter()는 인자를 받음 (AuthenticationManager() 메소드에 authenticationConfiguration 객체를 넣어야 함) 따라서 등록 필요
                // jwtUtil 인수 전달
        http
                .addFilterAt(new CustomLoginFilter(authenticationManager(authenticationConfiguration), jwtUtil), UsernamePasswordAuthenticationFilter.class);
        http
                .addFilterBefore(new CustomLogoutFilter(jwtUtil, refreshRepository), LogoutFilter.class);
        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}