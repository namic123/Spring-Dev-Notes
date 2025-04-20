# SecurityConfig 클래스 설명

## 1\. 개요

SecurityConfig 클래스는 Spring Security의 핵심 구성 요소 중 하나로, 애플리케이션의 인증(Authentication) 및 인가(Authorization) 설정을 담당하는 클래스입니다.  
Spring Security는 버전별로 설정 방식이 크게 다르며, 특히 5.x 버전 이후에는 WebSecurityConfigurerAdapter 클래스가 deprecated 되었고, 6.x 버전부터는 SecurityFilterChain을 직접 Bean으로 등록하는 방식이 표준화되었습니다.

> 본 설정 예시는 Spring Security **6.2.1 버전**을 기준으로 설명되며, JWT 방식의 인증 흐름을 구성하기 위한 **기초 설정**을 포함합니다.

2\. 기본 구조

```
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 구성 예정
        return http.build();
    }
}
```

-   @Configuration: 해당 클래스가 설정 클래스임을 명시합니다.
-   @EnableWebSecurity: Spring Security 기능을 활성화하며, 내부적으로 SecurityFilterChain을 초기화합니다.

## 3\. 필터 체인 구성 요소 설명

HttpSecurity 객체를 통해 설정되는 각 기능은 다음과 같은 보안 목적을 가지고 있습니다.

### (1) CSRF 설정 비활성화

```
http.csrf((auth) -> auth.disable());
```

-   CSRF(Cross Site Request Forgery)는 기본적으로 Spring Security가 활성화합니다.
-   REST API 및 JWT 기반 인증 방식에서는 세션을 사용하지 않으므로 비활성화(disable()) 합니다.

(2) Form 로그인 방식 비활성화

```
http.formLogin((auth) -> auth.disable());
```

-   기본적으로 Spring Security는 /login 요청을 가로채는 Form Login 기능을 제공합니다.
-   JWT 방식에서는 직접 로그인 API를 구성하고 토큰을 발급하므로 Form 기반 로그인은 제거합니다.

---

### (3) HTTP Basic 인증 비활성화

```
http.httpBasic((auth) -> auth.disable());
```

-   HTTP Basic 인증은 브라우저에서 ID/PW를 Base64로 인코딩하여 헤더에 포함하는 방식입니다.
-   JWT 방식에서는 사용자 인증을 위해 별도의 토큰 인증 체계를 사용하므로 비활성화합니다.

---

### (4) 경로별 인가 설정

```
http.authorizeHttpRequests((auth) -> auth
        .requestMatchers("/login", "/", "/join").permitAll()
        .requestMatchers("/admin").hasRole("ADMIN")
        .anyRequest().authenticated());
```

-   /login, /, /join 경로는 인증 없이 접근 가능하도록 설정합니다.
-   /admin 경로는 ROLE\_ADMIN 권한이 있어야 접근 가능합니다.
-   나머지 모든 요청(anyRequest)은 인증이 필요하도록 설정합니다.

---

### (5) 세션 설정

```
http.sessionManagement((session) -> session
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
```

-   SessionCreationPolicy.STATELESS 설정은 **세션을 생성하지 않고**, **서버가 상태 정보를 저장하지 않는 구조**입니다.
-   JWT 방식에서는 서버가 클라이언트의 상태를 기억할 필요가 없으므로 반드시 설정해야 하는 옵션입니다.

## 4\. 비밀번호 암호화: BCryptPasswordEncoder

JWT 인증 환경에서도 사용자의 비밀번호는 안전하게 암호화되어야 합니다. Spring Security는 이를 위해 BCryptPasswordEncoder 클래스를 제공합니다.

### BCryptPasswordEncoder 등록

```
@Bean
public BCryptPasswordEncoder bCryptPasswordEncoder() {
    return new BCryptPasswordEncoder();
}
```

-   비밀번호를 BCrypt 알고리즘으로 해시 처리합니다.
-   UserEntity 저장 시 또는 로그인 비교 시 자동으로 암호화를 처리하거나 비교할 수 있습니다.

---

## 5\. 정리

| 설정 항목 | 목적 및 설명 |
| --- | --- |
| .csrf().disable() | REST API에서는 CSRF 방어가 불필요하므로 비활성화 |
| .formLogin().disable() | 기본 Form 로그인 제거, 커스텀 로그인 API 구현을 위함 |
| .httpBasic().disable() | HTTP Basic 인증 제거 |
| .authorizeHttpRequests() | 엔드포인트별 접근 권한 설정 |
| .sessionManagement().sessionCreationPolicy(STATELESS) | JWT 방식에서는 세션 미사용, 상태 없는 방식 사용 |
| BCryptPasswordEncoder | 사용자 비밀번호 안전 암호화를 위한 필수 설정 |