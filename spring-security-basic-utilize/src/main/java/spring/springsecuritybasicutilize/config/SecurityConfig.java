package spring.springsecuritybasicutilize.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration // 해당 클래스를 Spring 설정 클래스로 등록
@EnableWebSecurity // Spring Security를 활성화하며, 내부적으로 FilterChain을 자동 구성함
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // 🔐 URL 별 인가(Authorization) 규칙 설정
        http
                .authorizeHttpRequests((auth) -> auth
                        // "/" 또는 "/login", "/loginProc" 경로는 인증 없이 접근 허용
                        .requestMatchers("/", "/login", "/loginProc").permitAll()
                        // "/admin" 경로는 "ADMIN" 역할을 가진 사용자만 접근 가능
                        .requestMatchers("/admin").hasRole("ADMIN")
                        // "/my/**" 경로는 "ADMIN" 또는 "USER" 역할을 가진 사용자만 접근 가능
                        .requestMatchers("/my/**").hasAnyRole("ADMIN", "USER")
                        // 위에서 명시되지 않은 나머지 요청은 인증된 사용자만 접근 가능
                        .anyRequest().authenticated()
                );

        // 🧾 Form 로그인 설정
        http
                .formLogin((auth) -> auth
                        // 사용자 정의 로그인 페이지 경로 지정 (GET 요청)
                        .loginPage("/login")
                        // 로그인 form 데이터를 처리할 URL (POST 요청)
                        .loginProcessingUrl("/loginProc")
                        // 로그인 페이지 및 처리 URL은 모두 인증 없이 접근 가능
                        .permitAll()
                );

        // ❌ CSRF 보호 기능 비활성화 (개발 또는 API 서버 환경에서 주로 사용)
        http
                .csrf((auth) -> auth.disable());

        // 구성된 SecurityFilterChain 반환
        return http.build();
    }
}
