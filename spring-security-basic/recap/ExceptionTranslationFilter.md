## Spring Security 예외 처리 필터, ExceptionTranslationFilter(SecurityFilterChain 15번째 필터)

블로그 : https://pjs-world.tistory.com/entry/Spring-Security-%EC%98%88%EC%99%B8-%EC%B2%98%EB%A6%AC-%ED%95%84%ED%84%B0-ExceptionTranslationFilter

## 1\. ExceptionTranslationFilter란 무엇인가?

Spring Security에서는 인증(Authentication) 혹은 인가(Authorization) 과정에서 문제가 발생했을 때, 이를 사용자에게 적절한 방식으로 안내할 수 있어야 한다.  
ExceptionTranslationFilter는 바로 이러한 인증/인가 실패 상황을 **가로채어 처리하는 역할**을 수행하는 필터이다.

예를 들어 인증되지 않은 사용자가 보호된 리소스를 요청하거나, 인증은 되었지만 권한이 부족한 경우 해당 예외를 포착하여 로그인 페이지로 리다이렉트하거나 403 응답을 반환한다.

ExceptionTranslationFilter는 기본적으로 Spring Security의 **Security Filter Chain 내 15번째** 필터로 등록된다.  
중요한 점은 이 필터 **이후에 발생하는 보안 예외만을 감지하고 처리할 수 있다**는 것이다.  
만약 UsernamePasswordAuthenticationFilter에서 예외가 발생한 경우, 그 위치가 ExceptionTranslationFilter보다 앞서기 때문에 이 필터는 해당 예외를 처리하지 않는다.

[##_Image|kage@71SpV/btsNuX8wq6T/3lKtTspkFc0xBFqKyRv1J1/img.png|CDM|1.3|{"originWidth":1556,"originHeight":609,"style":"alignCenter"}_##]

## 2\. doFilter() 내부 흐름과 예외 유형별 처리 흐름

핵심 동작은 try-catch 블록을 통해 이루어지며, 구체적인 흐름은 다음과 같다.

```
try {
    chain.doFilter(request, response);
} catch (Exception ex) {
    // 예외에서 AuthenticationException 또는 AccessDeniedException을 추출
    // 예외가 감지되면 해당 유형에 따라 처리
    handleSpringSecurityException(...);
}
```

즉, 후속 필터에서 발생한 보안 관련 예외를 캐치하고, 적절한 핸들러에 위임하여 응답을 생성하는 것이 이 필터의 역할이다.

예외는 크게 두 가지 유형으로 나뉘어 처리된다.

**AuthenticationException 발생 시**

인증되지 않은 사용자가 보호된 자원에 접근하려 한 경우이다.  
이 때는 AuthenticationEntryPoint가 호출되어 로그인 페이지로 리디렉션하거나, API 응답으로는 401 Unauthorized를 반환한다.

```
handleAuthenticationException(...) {
    sendStartAuthentication(...);
}
```

**AccessDeniedException 발생 시**

사용자는 인증은 되어 있으나 해당 리소스에 접근할 권한이 없는 경우이다.  
이 경우에는 AccessDeniedHandler가 호출되며, 403 Forbidden 응답을 반환하거나 커스텀 응답을 제공한다.

```
handleAccessDeniedException(...) {
    accessDeniedHandler.handle(...);
}
```

## 3\. 예외 처리 핸들러 구성과 한계

| **예외 종류** | **처리 핸들러** | **기본 동작** |
| --- | --- | --- |
| **AuthenticationException** | AuthenticationEntryPoint | 로그인 페이지 이동 또는 JSON 응답 반환 |
| **AccessDeniedException** | AccessDeniedHandler | 403 응답 또는 커스텀 처리 |

이 두 핸들러는 필요에 따라 SecurityConfig에서 직접 설정하여 응답 형식을 변경할 수 있다.

ExceptionTranslationFilter는 위치상 뒤에 있기 때문에, **해당 필터보다 앞단에서 발생한 예외는 포착하지 못한다**는 제한이 존재한다.  
예컨대 로그인 요청을 처리하는 UsernamePasswordAuthenticationFilter 내부에서 발생한 예외는 이 필터에서 감지되지 않는다.

따라서 인증 필터 내부에서는 자체적인 try-catch나 커스텀 예외 처리 로직이 별도로 필요하다.

## 4\. 설정 예시 (SecurityConfig)

보안 예외 응답 방식을 커스터마이징하려면 다음과 같이 설정할 수 있다

```
http
    .exceptionHandling(exception -> exception
        .authenticationEntryPoint(customEntryPoint)
        .accessDeniedHandler(customAccessDeniedHandler)
    );
```

예를 들어 JSON 형태의 응답을 제공하거나, 특정 메시지를 포함한 페이지로 리디렉션하고 싶을 때 위 설정을 활용한다.

## 5\. 요약

| **항목** | **설명** |
| --- | --- |
| **필터명** | ExceptionTranslationFilter |
| **위치** | Security Filter Chain의 15번째 |
| **핵심 역할** | 인증 및 인가 예외를 포착하여 적절한 응답 제공 |
| **처리 핸들러** | AuthenticationEntryPoint, AccessDeniedHandler |
| **처리 가능한 예외** | AuthenticationException, AccessDeniedException |
| **한계점** | 앞단 필터에서 발생한 예외는 감지하지 못함 |
| **설정 방법** | http.exceptionHandling() 구문 사용 |