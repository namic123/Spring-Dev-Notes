## ✅ 로그인 방식 비교: formLogin() vs httpBasic()



| 항목 | formLogin() | httpBasic |
| --- | --- | --- |
| **인증 방식** | HTML <form>으로 ID/PW 전송 | HTTP Authorization 헤더에 ID/PW 포함 |
| **UI 지원** | 커스텀 가능한 로그인 페이지 제공 가능 | 브라우저 기본 팝업 로그인 창 사용 |
| **보안** | HTTPS로 전송 시 안전 | 반드시 HTTPS 필요 (평문 ID/PW 노출 위험) |
| **사용 사례** | 웹 서비스의 일반적인 로그인 처리 | REST API, 테스트용 인증 등 |
| **기본 필터** | UsernamePasswordAuthenticationFilter 사용 | BasicAuthenticationFilter 사용 |

---

## ✅ Http Basic 인증 방식이란?

-   사용자의 **아이디:비밀번호를 Base64로 인코딩한 문자열**을 HTTP 요청의 Authorization 헤더에 담아 서버에 전달합니다.

### 예시 헤더

```
Authorization: Basic YWRtaW46MTIzNA==
```

-   위 인코딩 문자열은 "admin:1234"를 Base64로 인코딩한 것입니다.

### 특징

-   서버는 이 값을 디코딩해서 인증 정보를 확인합니다.
-   Spring Security는 세션을 생성하여 사용자를 기억합니다.  
    (원래 HTTP Basic은 stateless지만, Spring은 기본적으로 세션 관리함)

---

## ✅ Spring Security 설정 예시: httpBasic()

```
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .httpBasic(Customizer.withDefaults());  // HTTP Basic 인증 활성화

        return http.build();
    }
}
```

### 🔸 주의할 점

-   formLogin()을 **제거**하고 httpBasic()만 설정해야 HTTP Basic 방식이 작동합니다.
-   브라우저 접근 시 기본 팝업이 뜨며 로그인 창을 제공하게 됩니다.
-   보안 강화를 위해 **HTTPS 환경에서만** 사용해야 안전합니다.

---

## ✅ 언제 사용하면 좋을까?

| 상황 | 추천 방식 |
| --- | --- |
| 웹사이트의 일반적인 로그인 화면 | formLogin() |
| Postman, REST Client 테스트 | httpBasic() |
| API 서버(내부 시스템 인증 등) | httpBasic() or OAuth |
| 브라우저를 통한 인증 불필요한 API | Token 방식 (JWT 등) |

---

## ✅ 결론

-   httpBasic()은 간단하고 빠르게 인증 테스트할 수 있어 **개발 초기나 REST API용 인증 방식으로 유용**합니다.
-   하지만 보안이 약하므로 **운영 환경에서는 HTTPS와 함께 사용하거나 토큰 기반 인증으로 대체**하는 것이 좋습니다.