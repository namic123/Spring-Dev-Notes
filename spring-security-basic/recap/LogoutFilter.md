## Spring Security - LogoutFilter (로그아웃), 구조, 동작 원리, 커스터마이징 방법 (SecurityFilterChain 7번째 필터)

블로그 : https://pjs-world.tistory.com/entry/Spring-Security-%EB%A1%9C%EA%B7%B8%EC%95%84%EC%9B%83-LogoutFilter-%EA%B5%AC%EC%A1%B0-%EB%8F%99%EC%9E%91-%EC%9B%90%EB%A6%AC-%EC%BB%A4%EC%8A%A4%ED%84%B0%EB%A7%88%EC%9D%B4%EC%A7%95-%EB%B0%A9%EB%B2%95-SecurityFilterChain-7%EB%B2%88%EC%A7%B8-%ED%95%84%ED%84%B0

## 1\. LogoutFilter란 무엇인가?

LogoutFilter는 Spring Security의 기본 필터 체인 중 하나로, 클라이언트로부터 로그아웃 요청이 들어왔을 때 이를 처리하는 역할을 담당하는 필터이다.

내부적으로는 등록된 여러 LogoutHandler들을 차례대로 호출하며, 마지막으로 LogoutSuccessHandler를 통해 로그아웃 이후의 후처리를 수행한다. 이 필터는 기본적으로 **Security Filter Chain의 일곱 번째 위치**에 있으며, CsrfFilter 다음에 실행된다.

[##_Image|kage@d9aRJg/btsNkvYAThg/x6nakBfl230O45NDhtW7P0/img.png|CDM|1.3|{"originWidth":1124,"originHeight":437,"style":"alignCenter","caption":"Security Filter Chain 7번째 필터"}_##]

## 2\. LogoutFilter의 기본 동작 흐름

LogoutFilter는 요청이 특정 URL(/logout)에 해당하는 경우에만 동작한다. 요청이 들어오면, 다음과 같은 단계로 처리를 수행한다.

1.  현재 요청이 로그아웃 요청인지 판단한다.
2.  인증(Authentication) 객체를 SecurityContextHolder로부터 가져온다.
3.  등록된 LogoutHandler들을 차례로 실행하여 로그아웃 관련 처리를 수행한다.
4.  로그아웃이 완료되면, LogoutSuccessHandler를 호출하여 리디렉션 또는 응답 메시지 출력을 진행한다.

요청이 로그아웃과 관련 없는 경우에는 다음 필터로 요청을 전달하게 된다.

아래는 이해를 돕기 위한 LogoutFilter 내부 로직 흐름을 표현하는 이미지다

[##_Image|kage@cjB6vJ/btsNkHkdJbe/kdte3DPkns8Lt2CorXBYM0/img.png|CDM|1.3|{"originWidth":1115,"originHeight":587,"style":"alignCenter","caption":"LogoutFilter 내부 로직"}_##]

## 3\. 구성 요소 분석: LogoutHandler와 LogoutSuccessHandler

**LogoutHandler**

LogoutHandler는 로그아웃 시에 실행되어야 할 작업을 정의하는 인터페이스이다. 대표적인 구현체로는 다음과 같은 클래스들이 존재한다.

-   SecurityContextLogoutHandler: 세션을 무효화하고 SecurityContext를 초기화하는 역할을 수행한다.
-   CookieClearingLogoutHandler: 지정된 쿠키를 클라이언트 브라우저로부터 삭제한다.
-   HeaderWriterLogoutHandler: 로그아웃 시 응답 헤더를 추가하는 데 사용된다.
-   LogoutSuccessEventPublishingLogoutHandler: 로그아웃 완료 시 Spring 이벤트를 발행한다.

**LogoutSuccessHandler**

로그아웃이 정상적으로 처리된 이후 수행되는 후처리를 담당한다. 예를 들어, 사용자를 로그인 페이지로 리디렉션하거나, JSON 형태의 메시지를 반환하도록 커스터마이징할 수 있다.

## 4\. LogoutFilter 커스터마이징 전략

Spring Security에서는 로그아웃 동작을 다양하게 커스터마이징할 수 있는 방법을 제공한다. 몇 가지 주요 전략은 다음과 같다.

-   **로그아웃 필터 비활성화**: 로그아웃 기능이 불필요한 API 서버 등에서는 logout().disable() 설정을 통해 해당 필터를 비활성화할 수 있다.
-   **쿠키 삭제 기능 추가**: 로그아웃 시 인증 정보를 담은 쿠키를 명시적으로 제거하고자 할 때는 CookieClearingLogoutHandler를 추가할 수 있다.
-   **리디렉션 경로 변경**: 로그아웃 후 사용자를 특정 페이지로 이동시키고자 할 경우 LogoutSuccessHandler를 커스터마이징하여 처리한다.

## 5\. JWT 인증 구조에서의 로그아웃 처리

Spring Security의 기본 로그아웃 처리는 세션 기반 인증에 초점이 맞추어져 있다. 그러나 JWT를 사용하는 Stateless 구조에서는 로그아웃 방식이 다르게 설계되어야 한다.

1.  **JWT 토큰 삭제**: 보통 클라이언트 측에서 JWT를 저장하는 쿠키를 삭제하는 방식으로 처리하며, 이를 위해 CookieClearingLogoutHandler를 활용할 수 있다.
2.  **블랙리스트 처리**: 보안이 중요한 경우, 로그아웃된 JWT 토큰을 서버에서 블랙리스트로 관리할 수도 있다. 이 경우 커스텀 LogoutHandler를 구현하여 저장소에 토큰을 기록한다.
3.  **로그아웃 응답 제어**: JWT 기반의 API 서버에서는 페이지 리디렉션보다는 JSON 메시지 응답이 필요할 수 있으므로, LogoutSuccessHandler를 통해 JSON 응답을 반환하도록 구성할 수 있다.

#### **예시 코드**

**1\. JWT 쿠키 삭제: CookieClearingLogoutHandler**

```
CookieClearingLogoutHandler cookieClearingHandler = 
    new CookieClearingLogoutHandler("JWT-TOKEN");
```

**2\. 커스텀 블랙리스트 LogoutHandler 예제**

```
@Component
public class JwtBlacklistLogoutHandler implements LogoutHandler {

    private final BlacklistRepository blacklistRepository; // 예: Redis 기반 저장소

    public JwtBlacklistLogoutHandler(BlacklistRepository repository) {
        this.blacklistRepository = repository;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String jwt = extractJwtFromRequest(request);
        if (jwt != null) {
            blacklistRepository.addToBlacklist(jwt); // 만료 시점까지 저장
        }
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
```

**3\. 커스텀 LogoutSuccessHandler – JSON 응답 반환**

```
@Component
public class JsonLogoutSuccessHandler implements LogoutSuccessHandler {

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                Authentication authentication) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.getWriter().write("{\"message\": \"로그아웃 성공\", \"status\": 200}");
    }
}
```

**4\. Security 설정 적용**

```
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtBlacklistLogoutHandler blacklistHandler,
                                           JsonLogoutSuccessHandler logoutSuccessHandler) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .logout(logout -> logout
                .logoutUrl("/api/logout")
                .addLogoutHandler(new CookieClearingLogoutHandler("JWT-TOKEN"))
                .addLogoutHandler(blacklistHandler)
                .logoutSuccessHandler(logoutSuccessHandler)
            );

        return http.build();
    }
}
```

## 6\. 전체 요약

| **항목** | **설명** |
| --- | --- |
| **필터명** | LogoutFilter |
| **위치** | Security Filter Chain의 7번째 필터 |
| **주요 기능** | 로그아웃 요청 감지, LogoutHandler 실행, 후처리 수행 |
| **구성 요소** | LogoutHandler, LogoutSuccessHandler |
| **세션 기반 환경** | SecurityContext 초기화 및 세션 무효화 |
| **JWT 기반 환경** | 쿠키 삭제, 토큰 무효화, 응답 JSON 처리 등 커스터마이징 필요 |