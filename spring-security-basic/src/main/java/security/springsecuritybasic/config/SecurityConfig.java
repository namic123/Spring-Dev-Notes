package security.springsecuritybasic.config;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import security.springsecuritybasic.filter.CustomGenericFilter;
import security.springsecuritybasic.filter.CustomOnceFilter;

/**
 * Spring Security 필터 체인 설정 클래스.
 * <p>
 * 커스텀 필터(CustomGeneriFilter, CustomOnceFilter)를 필터 체인에 등록한다.
 * </p>
 *
 * @author 박재성
 * @version 1.0
 * @since 2025-04-10
 */
@Configuration
@EnableWebSecurity(debug = true)
public class SecurityConfig {

    /**
     * SecurityFilterChain을 구성하여 커스텀 필터를 등록한다.
     *
     * @param http HttpSecurity 객체
     * @return 구성된 SecurityFilterChain
     * @throws Exception 설정 중 예외 발생 시
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests((auth) -> auth.anyRequest().permitAll());

        http
                .addFilterAfter(new CustomGenericFilter(), LogoutFilter.class);
        http
                .addFilterAfter(new CustomOnceFilter(), LogoutFilter.class);

        return http.build();
    }

}