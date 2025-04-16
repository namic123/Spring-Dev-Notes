## Spring Security - HTTP 기본 인증을 처리하는 필터, BasicAuthenticationFilter(SecurityFilterChain 11번째 필터)

블로그 : https://pjs-world.tistory.com/entry/Spring-Security-HTTP-%EA%B8%B0%EB%B3%B8-%EC%9D%B8%EC%A6%9D%EC%9D%84-%EC%B2%98%EB%A6%AC%ED%95%98%EB%8A%94-%ED%95%84%ED%84%B0-BasicAuthenticationFilterSecurityFilterChain-11%EB%B2%88%EC%A7%B8-%ED%95%84%ED%84%B0

## 1\. BasicAuthenticationFilter란?

Spring Security에서 제공하는 BasicAuthenticationFilter는 **HTTP 요청의 Authorization 헤더**에 담긴 Basic {credentials} 값을 해석하고 인증을 수행하는 역할을 담당하는 필터이다.

이 필터는 기본적으로 **SecurityFilterChain의 11번째**에 배치되며, Authorization: Basic ~ 형태로 들어오는 인증 요청을 자동으로 감지하고 처리한다.

[##_Image|kage@bybGdv/btsNl92qFLl/UwYkflpxccQdKOtPxXBtS1/img.png|CDM|1.3|{"originWidth":1540,"originHeight":580,"style":"alignCenter","caption":"SecurityFilterChain의 11번째 필터"}_##]

## 2\. HTTP Basic 인증이란?

HTTP Basic 인증은 웹 표준에 정의된 가장 단순한 형태의 인증 방식으로, 사용자 이름과 비밀번호를 username:password 형태로 **Base64 인코딩**한 값을 Authorization 헤더에 담아 서버로 전송한다.

| **항목** | **Form 로그인** | **Basic 인증** |
| --- | --- | --- |
| **사용자 입력 방식** | HTML form | 브라우저 팝업 or 직접 헤더 입력 |
| **인증 데이터 전달** | POST body | Authorization 헤더 |
| **인증 유지 방식** | 세션 or 토큰 | 매 요청마다 인증 정보 포함 |
| **UI 제어** | 커스터마이징 가능 | 브라우저 기본 팝업 UI |

**예시**

```
Authorization: Basic YWRtaW46cGFzc3dvcmQ=
# 위 값은 "admin:password"를 Base64로 인코딩한 문자열
```

## 3\. 기본 동작 흐름

이 필터의 핵심 로직은 doFilterInternal() 메서드 내부에서 다음과 같은 단계로 진행된다.

1.  **Authorization 헤더 추출**
2.  Basic 스킴인지 확인
3.  username:password 디코딩 후 UsernamePasswordAuthenticationToken 생성
4.  AuthenticationManager에 위임하여 인증 수행
5.  인증 성공 시 SecurityContextHolder에 결과 저장
6.  인증 실패 시 AuthenticationEntryPoint를 통해 401 응답

**핵심 코드**

```
Authentication authRequest = authenticationConverter.convert(request);
Authentication authResult = authenticationManager.authenticate(authRequest);
SecurityContextHolder.getContext().setAuthentication(authResult);
```

## 4\. 클래스 및 구성요소 요약

| **구성 요소** | **설명** |
| --- | --- |
| **BasicAuthenticationFilter** | 필터 본체. OncePerRequestFilter를 상속 |
| **authenticationConverter** | Authorization 헤더에서 사용자 정보를 추출 |
| **authenticationManager** | 인증 처리 담당. DaoAuthenticationProvider 사용 가능 |
| **SecurityContextHolderStrategy** | 인증 정보를 저장. 기본은 ThreadLocal |
| **AuthenticationEntryPoint** | 인증 실패 시 401 Unauthorized 응답 처리 |
| **RememberMeServices** | remember-me 기능과 연계됨 (선택 사항) |

## 5\. 실제 사용 예시

Spring Security에서는 다음과 같은 코드 한 줄로 Basic 인증을 활성화할 수 있다.

```
http.httpBasic(Customizer.withDefaults());
```

주의할 점은, **커스터마이징된 SecurityFilterChain을 구성하는 경우에는 위 설정이 누락되면 BasicAuthenticationFilter가 등록되지 않는다는 점**이다.

## 6\. 브라우저 및 Postman 테스트 방법

-   **브라우저 접근:**  
    주소창에 보호된 리소스를 입력하면 **브라우저 팝업창이 자동 생성**되어 사용자에게 인증 정보를 요구하게 된다.
-   **Postman 테스트:**  
    Headers 탭에 다음과 같은 값을 추가한다.

```
Authorization: Basic YWRtaW46cGFzc3dvcmQ=
```

이때 admin:password와 같은 값은 Base64로 인코딩한 문자열이어야 한다.

## 7\. 요약

| **항목** | **설명** |
| --- | --- |
| **필터명** | BasicAuthenticationFilter |
| **위치** | SecurityFilterChain의 11번째 |
| **목적** | HTTP Basic 인증 처리 |
| **인증 정보** | username:password → Base64 인코딩 후 헤더로 전달 |
| **활성화 조건** | http.httpBasic() 명시 필요 |
| **인증 유지 방식** | 세션 or SecurityContext |
| **실패 시 처리** | AuthenticationEntryPoint를 통해 401 응답 |