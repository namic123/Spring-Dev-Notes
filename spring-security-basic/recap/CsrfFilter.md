## Spring security - CsrfFilter, CSRF 보호 메커니즘 (SecurityFilterChain 6번째 필터)

블로그: https://pjs-world.tistory.com/entry/Spring-security-CsrfFilter-CSRF-%EB%B3%B4%ED%98%B8-%EB%A9%94%EC%BB%A4%EB%8B%88%EC%A6%98-SecurityFilterChain-6%EB%B2%88%EC%A7%B8-%ED%95%84%ED%84%B0

## 1\. CSRF란 무엇인가?

CSRF(Cross-Site Request Forgery)는 인증된 사용자의 브라우저를 이용하여 사용자의 의도와 **무관한 요청을 보내는 공격 기법**이다.  
브라우저는 동일 출처 정책(SOP)에 따라 쿠키를 자동으로 첨부하므로, 악의적인 사이트가 사용자의 세션을 도용하여 의도치 않은 요청을 서버에 보낼 수 있게 된다.

**예시 시나리오**는 다음과 같다

1.  사용자가 서비스 A에 로그인하여 세션이 유지되고 있는 상태
2.  공격자가 자신의 악성 사이트 B에 A 사이트로의 요청을 숨겨놓은 이미지나 링크를 배치
3.  사용자가 사이트 B를 방문하거나 해당 링크를 클릭
4.  브라우저는 쿠키를 자동으로 첨부하여 A로 요청 전송
5.  서버는 정당한 사용자 요청으로 오인하여 민감한 작업(예: 계정 탈퇴, 송금 등)을 수행

## 2\. CsrfFilter의 구조와 위치

Spring Security는 이러한 공격을 방지하기 위해 CsrfFilter라는 전용 보안 필터를 제공한다.  
이 필터는 기본 Security Filter Chain 상에서 **여섯 번째 필터**로 구성되며, CorsFilter 다음에 실행된다.

[##_Image|kage@BRMkJ/btsNjhUp2C7/K6OMgkjaUQZiIELxPL39N0/img.png|CDM|1.3|{"originWidth":1102,"originHeight":433,"style":"alignCenter","caption":"Security Filter Chain 여섯 번째 필터"}_##]

CsrfFilter는 주로 HTTP 메서드 중 상태 변경을 수반하는 POST, PUT, DELETE 요청에 대해 CSRF 토큰 검증을 수행한다.  
반면 GET, HEAD, OPTIONS, TRACE와 같은 단순 조회 요청은 검증 대상에서 제외된다.

## 3\. CsrfFilter의 동작 방식

CsrfFilter는 doFilterInternal() 메서드에서 다음과 같은 순서로 동작한다

1.  DeferredCsrfToken을 요청으로부터 로드한다.
2.  요청 속성(HtttpServletRequest.setAttribute())에 해당 토큰을 저장하여 이후 컨트롤러에서도 접근할 수 있도록 한다.
3.  요청 메서드가 검증 대상인지 판단한다. 
4.  검증이 필요한 요청이라면, 클라이언트로부터 전달받은 토큰과 서버 측 저장 토큰을 비교한다.
5.  토큰이 일치하지 않으면 403 Access Denied 예외가 발생한다.
6.  검증이 완료되면 다음 필터로 요청을 전달한다.

위 흐름을 도식화하면 아래 플로우 차트로 표현할 수 있다.

[##_Image|kage@b53GQo/btsNkiZlzLJ/eBakQ4EUt3n6ZLNShcWmq0/img.png|CDM|1.3|{"originWidth":450,"originHeight":589,"style":"alignLeft","width":413}_##]

이러한 흐름은 서버가 발급한 토큰과 클라이언트가 제출한 토큰이 일치하는지를 통해 요청의 진위 여부를 판단하는 구조로 설계되어 있다.

## 4\. CSRF 토큰 저장소와 설정

**CsrfTokenRepository는 CSRF 토큰을 저장하고 불러오는 역할을 수행하는 인터페이스**이며, Spring Security는 다음과 같은 구현체를 기본 제공한다

| **구현체** | **설명** |
| --- | --- |
| **HttpSessionCsrfTokenRepository** | 서버 세션에 토큰을 저장 (기본값) |
| **CookieCsrfTokenRepository** | 클라이언트 측 쿠키에 토큰을 저장, SPA 구조에 적합 |
| **직접 구현** | 필요에 따라 커스터마이징 가능 |

설정 예시는 다음과 같다

```
http.csrf(csrf -> csrf.csrfTokenRepository(new HttpSessionCsrfTokenRepository()));
```

## 5\. SSR 기반의 CSRF 보호 방식

서버 사이드 렌더링(SSR) 기반의 웹 애플리케이션에서는 HTML 폼(form) 기반 요청에 대해 CSRF 토큰을 <input type="hidden"> 요소로 삽입한다.

```
<input type="hidden" name="_csrf" value="${_csrf.token}" />
```

이러한 방식은 사용자의 입력 없이도 보안 처리가 가능하며, Spring MVC에서는 별도의 설정 없이도 템플릿 엔진을 통해 자동 삽입된다.

## 6\. Stateless 환경에서의 처리

REST API와 같은 Stateless한 시스템에서는 세션을 유지하지 않기 때문에 기본적으로 CSRF 공격의 위험이 상대적으로 적다.  
따라서 대부분의 경우 다음과 같이 CSRF 보호를 비활성화하는 설정이 사용된다

```
http.csrf(csrf -> csrf.disable());
```

그러나 예외적으로 **JWT를 쿠키에 저장하는 방식**에서는 다시 CSRF 공격의 위협이 발생할 수 있다.  
이유는 브라우저가 쿠키를 자동으로 첨부하기 때문에, 공격자가 사용자 대신 요청을 보낼 수 있기 때문이다.

이러한 구조에서는 CSRF 보호를 활성화해야 하며, Referer 검증 또는 토큰 기반 검증 방식을 사용해야 한다.

## 7\. 토큰 방식 vs Referer 방식

Spring Security에서는 두 가지 방식으로 CSRF 공격을 방어할 수 있다

| **방식** | **설명** |
| --- | --- |
| **토큰 기반 방식** | 서버가 발급한 토큰을 클라이언트가 요청 시 함께 제출 |
| **Referer 기반 방식** | HTTP 요청의 Referer 헤더를 분석하여 요청 출처의 정당성을 검증 |

Referer 방식은 다음과 같은 상황에서 유용하다.

-   Stateless API 구조에서 쿠키 기반 인증(JWT 등)을 사용할 경우
-   프론트엔드 SPA가 별도의 토큰 발급 뷰를 가지지 않을 경우

## 8\. 실무 적용 정리

| **환경** | **권장 CSRF 설정** |
| --- | --- |
| **SSR + 세션 로그인** | ✅ 기본 활성화 (토큰 기반 방식 사용) |
| **REST API + JWT (LocalStorage)** | ❌ csrf().disable() 가능 |
| **REST API + JWT (쿠키 저장)** | ✅ Referer 방식 또는 토큰 방식 활성화 필요 |
| **OAuth2 인증 연동** | 상황에 따라 별도 검토 필요 |

## 9\. 요약

| **항목** | **설명** |
| --- | --- |
| **필터명** | CsrfFilter |
| **위치** | Security Filter Chain 내 여섯 번째 |
| **보호 대상** | POST, PUT, DELETE 등 상태 변경 요청 |
| **기본 방식** | 서버가 토큰을 생성 → 클라이언트 제출 → 비교 검증 |
| **저장소 유형** | HttpSession, Cookie, 직접 구현 가능 |
| **REST API** | 대부분 비활성화 가능하나, 쿠키 인증 시 주의 |