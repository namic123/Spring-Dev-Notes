해당 CORS 설정 코드는 **Spring Security + Spring MVC 환경**에서 **프론트엔드(예: React - localhost:3000)와 백엔드(Spring Boot)가 분리되어 있는 경우** 발생할 수 있는 \*\*Cross-Origin 요청 문제(CORS 오류)\*\*를 해결하기 위한 설정입니다. 아래에서 각 설정이 어떤 역할을 하는지 자세히 설명드리겠습니다.

---

## 🔐 Spring Security에서의 CORS 설정

```
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

    return http.build();
}
```

### 📌 이 설정이 필요한 이유

-   Spring Security는 내부적으로 기본 CORS 처리를 비활성화 상태로 설정함
-   따라서 HttpSecurity의 cors()를 통해 별도로 허용할 도메인, 메서드, 헤더, 인증정보 등을 명시해야 함

---

## 🌐 Spring MVC에서의 CORS 설정 (추가 설정)

```
@Configuration
public class CorsMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry corsRegistry) {
        corsRegistry.addMapping("/**")
                .allowedOrigins("http://localhost:3000");
    }
}
```

### 📌 역할

-   @CrossOrigin을 컨트롤러마다 붙이지 않아도 전체 API(/\*\*)에 대해 CORS 허용
-   MVC 레벨에서의 허용이며, Spring Security의 CORS 설정과는 별개로 작동

---

## ✅ 두 설정이 필요한 이유



| 구성 | 목적 |
| --- | --- |
| SecurityFilterChain.cors() | Spring Security의 필터 체인 레벨에서 CORS 허용을 설정 |
| WebMvcConfigurer.addCorsMappings() | Spring MVC (컨트롤러 매핑 기준)의 응답에 대해 CORS 허용을 설정 |

> ❗ CORS 문제를 완전히 해결하려면 **Security + MVC 양쪽에서 설정**하는 것이 안정적입니다. 특히 JWT 사용 시 Authorization 헤더가 CORS 정책에 의해 차단되기 때문에 setExposedHeaders("Authorization") 설정이 중요합니다.

---

## 💡 요약



| 항목 | 설정 목적 |
| --- | --- |
| setAllowedOrigins() | 허용할 외부 도메인 지정 (ex. React 개발 서버) |
| setAllowedMethods() | 허용할 HTTP 메서드 (GET, POST 등) |
| setAllowedHeaders() | 클라이언트가 요청 시 사용할 수 있는 헤더 허용 |
| setExposedHeaders() | 클라이언트가 응답에서 접근할 수 있는 헤더 지정 (JWT는 "Authorization"이 필요) |
| setAllowCredentials() | 쿠키, 인증 헤더 등의 자격 정보 포함 허용 여부 |
| setMaxAge() | 브라우저가 preflight 요청 결과를 캐싱하는 시간 |