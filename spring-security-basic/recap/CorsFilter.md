## Spring Security - Cross-Origin 요청을 제어하는 CorsFilter (SecurityFilterChain 5번째 필터)

블로그 : https://pjs-world.tistory.com/entry/Spring-Security-Cross-Origin-%EC%9A%94%EC%B2%AD%EC%9D%84-%EC%A0%9C%EC%96%B4%ED%95%98%EB%8A%94-CorsFilter-SecurityFilterChain-5%EB%B2%88%EC%A7%B8-%ED%95%84%ED%84%B0

## 1\. CORS란 무엇인가?

브라우저에서 **다른 Origin**(도메인, 포트, 프로토콜이 다른 경우)의 리소스를 요청할 때 발생하는 보안 정책을 의미한다.  
즉, 프론트와 백이 다른 도메인/포트를 사용할 때, 브라우저가 보안상의 이유로 요청을 **차단**할 수 있다.

```
Frontend: http://localhost:3000
Backend: http://localhost:8080
```

위처럼 **Origin이 다르면** 기본 요청도 막히고, 특히 Credential 포함 요청(Set-Cookie, Authorization 등)은 더 까다로워진다. 

## 2\. CorsFilter의 구조 및 위치

Spring Security는 이러한 CORS 정책을 처리하기 위해 CorsFilter라는 전용 필터를 제공하고 있으며, 해당 필터는 기본 Security Filter Chain에서 **다섯 번째 필터**로 등록된다.

[##_Image|kage@ol2wR/btsNjCKAyed/CeD3fEcHhlFAiqfhkFERAk/img.png|CDM|1.3|{"originWidth":1362,"originHeight":536,"style":"alignCenter","caption":"Security Filter Chain 5번째 필터"}_##]

CorsFilter는 Spring 기반이 아닌 일반 서블릿 환경에서도 사용 가능하도록 설계되어 있어 GenericFilterBean이 아닌 **GenericFilter를 상속**받는다.

이로 인해 해당 필터는 Spring Bean으로 관리되지 않으며, 순수 서블릿 컨테이너 기반에서 동작한다는 특징이 있다.

## 3\. 내부 동작 방식

CorsFilter는 요청이 들어올 때, 다음과 같은 방식으로 동작한다:

1.  요청에 **Origin 헤더가 존재할 경우, 이를 CORS 요청으로 인식**한다.
2.  요청 방식이 OPTIONS일 경우, Preflight 요청으로 간주하고 허용된 메서드와 헤더 목록을 응답에 포함한다.
3.  그 외 일반적인 GET, POST 요청인 경우에도 필요한 CORS 응답 헤더를 추가하여 브라우저가 이를 신뢰할 수 있도록 구성한다.

이러한 처리는 서블릿 필터 체인 내에서 doFilter() 메서드를 통해 수행되며, 내부적으로는 CorsConfigurationSource로부터 CORS 정책을 불러와 응답에 반영하게 된다.

## 4\. CorsFilter 설정 방법

Spring Security에서는 다음과 같이 HttpSecurity 설정을 통해 CORS 정책을 구성할 수 있다.

```
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.cors(cors -> cors.configurationSource(new CorsConfigurationSource() {
        @Override
        public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowedOrigins(List.of("http://localhost:3000"));
            config.setAllowedMethods(List.of("*"));
            config.setAllowedHeaders(List.of("*"));
            config.setAllowCredentials(true);
            config.setExposedHeaders(List.of("Authorization", "Set-Cookie"));
            config.setMaxAge(3600L);
            return config;
        }
    }));
    return http.build();
}
```

**CORS 처리를 외부 Nginx 또는 API Gateway 등에서 위임하고자 할 경우에는 다음과 같이 비활성화하는 것도 가능하다**

```
http.cors(cors -> cors.disable());
```

## 5\. 실무 적용 시 주의사항

| **상황** | **주의할 점** |
| --- | --- |
| **프론트와 백엔드가 다른 Origin일 경우** | allowedOrigins와 allowCredentials(true)를 동시에 설정해야 함 |
| **쿠키 기반 인증(JWT 포함)을 사용할 경우** | 클라이언트 측 withCredentials = true 설정 필요 |
| **응답이 200인데도 요청이 실패할 경우** | Preflight 요청(OPTIONS)에 대한 응답 구성이 누락되었을 가능성 존재 |
| **Set-Cookie 값을 클라이언트 JS에서 읽지 못하는 경우** | exposedHeaders에 "Set-Cookie" 명시 여부 확인 필요 (브라우저 정책에 따라 제한될 수 있음) |

## 6\. 요약 정리

| 항목 | 설명 |
| --- | --- |
| **필터명** | CorsFilter |
| **위치** | Security Filter Chain 내 다섯 번째 |
| **상속 클래스** | GenericFilter |
| **역할** | 등록된 CORS 정책에 따라 응답 헤더 자동 삽입 |
| **설정 위치** | http.cors().configurationSource(...) |
| **실무 핵심** | allowedOrigins, allowCredentials(true) 조합 정확히 설정할 것 |