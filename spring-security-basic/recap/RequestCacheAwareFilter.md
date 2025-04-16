## Spring Security - 인증 후 원래 요청으로 복원해주는 보안 필터, RequestCacheAwareFilter(SecurityFilterChain 12번째 필터)

블로그 : https://pjs-world.tistory.com/entry/Spring-Security-%EC%9D%B8%EC%A6%9D-%ED%9B%84-%EC%9B%90%EB%9E%98-%EC%9A%94%EC%B2%AD%EC%9C%BC%EB%A1%9C-%EB%B3%B5%EC%9B%90%ED%95%B4%EC%A3%BC%EB%8A%94-%EB%B3%B4%EC%95%88-%ED%95%84%ED%84%B0-RequestCacheAwareFilterSecurityFilterChain-12%EB%B2%88%EC%A7%B8-%ED%95%84%ED%84%B0#google_vignette

## 1\. RequestCacheAwareFilter란?

Spring Security에서 제공하는 RequestCacheAwareFilter는 **사용자가 인증되지 않은 상태에서 특정 요청을 시도했을 때, 해당 요청을 기억해두었다가 인증 이후 다시 요청을 복원해주는 역할**을 수행하는 필터이다.  
기본적으로 **SecurityFilterChain의 12번째 필터**로 등록되어 있다.

이 필터의 존재 덕분에 우리는 로그인 이후 곧바로 원래 접근하려 했던 페이지로 자연스럽게 이동할 수 있다. 다시 말해, 로그인 전 URL 기억 기능의 중심축이라고 볼 수 있다.

[##_Image|kage@b9bvi9/btsNm1Y5Kg6/DCkoAuKBPeuELq4g5WgT11/img.png|CDM|1.3|{"originWidth":1544,"originHeight":568,"style":"alignCenter","caption":"SecurityFilterChain 12번째 필터"}_##]

---

## 2\. 동작 예시 흐름

아래는 실제 동작 시나리오를 단순화한 흐름이다.

```
1. 사용자가 "/my" 요청 → 로그인 안된 상태
2. 인증 예외 발생 → AccessDeniedException
3. ExceptionTranslationFilter가 현재 요청을 RequestCache에 저장
4. 로그인 페이지로 이동 → 인증 진행
5. 인증 성공 시 → 저장된 요청을 복원하여 자동 리디렉트
```

이처럼 RequestCacheAwareFilter는 로그인 과정과 직접 연결되지는 않지만, 로그인 이후 **어디로 갈지를 결정하는 중요한 역할**을 맡고 있다.

---

## 3\. 동작 흐름 상세 설명

이 필터는 단독으로 동작하지 않고, **ExceptionTranslationFilter와 협업 구조**를 이루고 있다.

| 필터 | 역할 |
| --- | --- |
| ExceptionTranslationFilter | 인증 예외 발생 시 RequestCache.saveRequest() 호출 |
| RequestCacheAwareFilter | 인증 후 getMatchingRequest()로 요청 복원 수행 |

즉, 예외 발생 시 저장, 인증 후 복원이라는 두 단계를 통해 사용자가 다시 원래 요청한 페이지로 되돌아가도록 처리한다.

---

## 4\. 내부 코드 흐름 분석

구체적으로 어떤 코드가 동작하는지 분석해보자.

**요청 저장: 예외 발생 시**

```
requestCache.saveRequest(request, response);
authenticationEntryPoint.commence(request, response, exception);
```

ExceptionTranslationFilter 내부에서 발생하며, 인증되지 않은 상태로 보호된 리소스에 접근했을 때 현재 요청이 저장된다.

**요청 복원: 인증 성공 이후**

```
HttpServletRequest wrappedRequest = requestCache.getMatchingRequest(request, response);
chain.doFilter(wrappedRequest != null ? wrappedRequest : request, response);
```

RequestCacheAwareFilter가 인증 성공 이후 필터 체인 흐름을 이어가기 전에, 이전에 저장된 요청이 있다면 이를 꺼내어 **현재 요청 객체로 감싼 뒤** 전달한다.

---

## 5\. 주요 클래스 구성 요약

| **클래스** | **설명** |
| --- | --- |
| **RequestCache** | 요청 저장/복원을 위한 인터페이스 |
| **HttpSessionRequestCache** | 기본 구현체. 요청을 세션에 저장 |
| **RequestCacheAwareFilter** | 복원된 요청을 감싸서 다음 필터로 전달 |
| **ExceptionTranslationFilter** | 인증/인가 예외 발생 시 요청 저장을 트리거 |

---

## 6\. 설정 및 비활성화 방법

기본적으로 Spring Security에 의해 자동 등록되며, 보통 별도의 설정 없이 동작한다.  
다만, 로그인 후 원래 요청으로 리디렉트되는 기능이 필요 없거나, 직접 제어하고자 하는 경우 아래처럼 비활성화할 수 있다.

```
http.requestCache(cache -> cache.disable());
```

---

## 7\. 요약 정리

| **항목** | **설명** |
| --- | --- |
| **필터명** | RequestCacheAwareFilter |
| **위치** | Security Filter Chain의 12번째 |
| **목적** | 로그인 전 요청을 기억하고 로그인 후 복원 |
| **동작 전제** | ExceptionTranslationFilter가 저장을 수행함 |
| **저장소** | 기본은 HttpSessionRequestCache |
| **비활성화** | .requestCache(cache -> cache.disable()) |
| **주 사용 시점** | 로그인 후 리디렉션 기능이 필요할 때 |