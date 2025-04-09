package security.springsecuritybasic.config;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 단일 SecurityFilterChain을 등록하는 기본 예제
 * 모든 요청에 대해 기본 보안 설정을 적용
 */
@Configuration
@EnableWebSecurity(debug = true)
public class SecurityConfig {

    /**
     * 기본 필터 체인 등록 (@Bean)
     *
     * @param http HttpSecurity 객체
     * @return SecurityFilterChain
     * @throws Exception 보안 설정 실패 시
     */
    @Bean
    public SecurityFilterChain filterChain1(HttpSecurity http) throws Exception {
        return http.build(); // 기본 설정만 적용됨
    }

}