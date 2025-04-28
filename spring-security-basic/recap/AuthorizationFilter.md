## Spring Security 최종 인가 필터, AuthorizationFilter(SecurityFilterChain 마지막 필터)

블로그 : https://pjs-world.tistory.com/entry/Spring-Security-%EC%B5%9C%EC%A2%85-%EC%9D%B8%EA%B0%80-%ED%95%84%ED%84%B0-AuthorizationFilterSecurityFilterChain-%EB%A7%88%EC%A7%80%EB%A7%89-%ED%95%84%ED%84%B0

## 1\. AuthorizationFilter란 무엇인가?

AuthorizationFilter는 **Spring Security** 내부에서 동작하는 핵심 필터 중 하나로, 인증(Authentication)이 끝난 사용자에 대하여 **최종적으로 인가(Authorization)** 를 수행하는 역할을 담당한다.

이 필터는 SecurityFilterChain 안에 기본적으로 등록되며, 필터 체인의 **마지막 부근에 위치**하여 모든 인증이 완료된 이후, "이 사용자가 현재 요청을 수행할 권한이 있는가?"를 체크하게 된다.

즉, 인증만으로 끝나는 것이 아니라, **각 요청마다 세밀하게 접근 권한을 추가로 검증**하는 마지막 관문 역할을 한다고 볼 수 있다.

[##_Image|kage@lbP1Y/btsNAULYkVs/EAQ0QNvvauBr1wfDUz2aVk/img.png|CDM|1.3|{"originWidth":1214,"originHeight":568,"style":"alignCenter"}_##]

## 2\. AuthorizationFilter가 필요한 이유

많은 사람들이 오해하는 것 중 하나는, "로그인만 성공하면 모든 리소스에 접근할 수 있다"고 생각하는 것이다. 그러나 현실적인 보안 환경에서는 로그인만으로 모든 리소스 접근을 허용할 수 없다. **요청 경로마다 세밀한 권한 제어가 반드시 필요**하다.

예를 들어,

-   /admin/\*\* 경로는 오직 관리자만 접근해야 하며,
-   /user/\*\* 경로는 인증된 일반 사용자도 접근할 수 있어야 한다.

이를 위해서는 사용자가 보유한 권한(Role)과 요청 URL을 비교하여, **접근 허용 여부를 최종적으로 판단하는 과정**이 필요하다. 바로 이 역할을 AuthorizationFilter가 맡고 있다.

## 3\. 등록 및 설정 방법

AuthorizationFilter는 별도로 직접 등록할 필요가 없다. **SecurityFilterChain** 설정을 구성하는 순간 자동으로 필터 체인에 포함된다.

보통 다음과 같이 설정한다.

```
http
    .authorizeHttpRequests(auth -> auth
        .requestMatchers("/admin/**").hasRole("ADMIN")
        .requestMatchers("/user/**").authenticated()
        .anyRequest().permitAll()
    );
```

이 설정들은 결국 AuthorizationFilter가 실제 요청 시점에 적용하여, 각 요청에 대해 접근 허용 여부를 최종적으로 판단하게 된다.

## 4\. AuthorizationFilter 내부 동작 흐름

AuthorizationFilter의 핵심 동작은 doFilter() 메서드 안에서 진행되며, 전체적인 흐름은 다음과 같다.

| **순서** | **동작 내용** | **설명** |
| --- | --- | --- |
| **1** | **isApplied(request)** | 이미 필터가 적용된 요청인지 확인하여 중복 적용 방지 |
| **2** | **skipDispatch(request)** | 비동기(Async)나 에러(Dispatch) 요청 여부 확인 후 스킵 결정 |
| **3** | **authorizationManager.check() 호출** | 현재 Authentication과 요청 정보를 기반으로 인가 여부 판단 |
| **4** | **결과 이벤트 발행** | 인가 성공/실패 여부를 이벤트로 발행 가능 (모니터링 용도) |
| **5** | **인가 실패 처리** | 권한 없을 경우 AccessDeniedException 발생 → 403 응답 반환 |
| **6** | **필터 통과** | 인가 성공 시 다음 필터로 요청 넘김 |

**주요 코드 예시**

```
@Override
public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
    throws IOException, ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) request;

    if (isApplied(httpRequest) || skipDispatch(httpRequest)) {
        chain.doFilter(request, response);
        return;
    }

    markApplied(httpRequest);

    AuthorizationDecision decision = authorizationManager.check(this::getAuthentication, httpRequest);

    if (decision != null && !decision.isGranted()) {
        throw new AccessDeniedException("접근이 거부되었습니다.");
    }

    chain.doFilter(request, response);
}
```

-   authorizationManager.check()는 현재 사용자 정보와 요청 정보를 기반으로 인가를 수행한다.
-   인가 실패 시 AccessDeniedException을 던져 **403 Forbidden** 응답을 발생시킨다.

## 5\. 요약

| **항목** | **설명** |
| --- | --- |
| **필터명** | AuthorizationFilter |
| **핵심 기능** | 인증 완료된 사용자에 대해 최종 인가(Authorization) 수행 |
| **위치** | SecurityFilterChain의 마지막 부근 |
| **등록 방법** | SecurityFilterChain 구성 시 자동 등록 |
| **주요 역할** | authorizeHttpRequests() 설정을 기반으로 최종 접근 허용 여부 판단 |
| **실패 시 결과** | AccessDeniedException 발생 → 403 Forbidden 응답 |