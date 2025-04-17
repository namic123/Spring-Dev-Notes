## Spring Security - 인증 API를 확장하는 보안 필터, SecurityContextHolderAwareRequestFilter(SecurityFilterChain 13번째 필터) 

블로그 : https://pjs-world.tistory.com/entry/Spring-Security-%EC%9D%B8%EC%A6%9D-API%EB%A5%BC-%ED%99%95%EC%9E%A5%ED%95%98%EB%8A%94-%EB%B3%B4%EC%95%88-%ED%95%84%ED%84%B0-SecurityContextHolderAwareRequestFilterSecurityFilterChain-13%EB%B2%88%EC%A7%B8-%ED%95%84%ED%84%B0


## 1\. SecurityContextHolderAwareRequestFilter란 무엇인가?

Spring Security의 SecurityContextHolderAwareRequestFilter는 HttpServletRequest 객체를 래핑(wrapping)하여 보안과 관련된 고급 기능들을 요청 객체 수준에서 제공하는 필터이다. 본 필터는 보안 컨텍스트(SecurityContext)와 연계되어 login(), logout(), authenticate() 등의 메서드를 사용할 수 있도록 돕는다.

위 필터는 DefaultSecurityFilterChain에서 13번째로 위치하며, 사용자 인증 및 보안 흐름의 중요한 연결 고리 역할을 수행한다.

[##_Image|kage@b57agX/btsNo4IaMbI/coLjVPkxD8rIkV8ovcGZlK/img.png|CDM|1.3|{"originWidth":1332,"originHeight":480,"style":"alignCenter","width":849,"height":306,"caption":"SecurityFilterChain 13번째 필터"}_##]

---

## 2\. 주요 기능 및 동작 개요

SecurityContextHolderAwareRequestFilter는 다음과 같은 주요 기능들을 제공한다

| 기능 | 설명 |
| --- | --- |
| request.authenticate() | 현재 사용자의 인증 상태를 검사하고, 필요 시 인증을 유도함 |
| request.login() | 프로그램 내에서 명시적으로 로그인 수행 |
| request.logout() | 명시적으로 로그아웃을 실행하고, 관련 핸들러를 호출함 |
| AsyncContext.start() | 비동기 작업 시 보안 컨텍스트(SecurityContext)를 새로운 쓰레드에 자동으로 전파함 |

이러한 기능들은 단순한 HTTP 요청 객체가 아닌, Spring Security의 인증 상태와 긴밀히 연동된 HttpServletRequest 래퍼를 통해 구현된다.

---

## 3\. 해당 필터가 필요한 이유

기본적으로 HttpServletRequest는 서블릿 API에 정의된 메서드만을 제공하며, 이는 Spring Security의 인증 메커니즘과는 별개로 동작한다. 따라서 login(), logout() 같은 메서드를 호출하더라도 보안 컨텍스트에 적절하게 반영되지 않는 문제가 있다.

이 필터는 이를 해결하고자, 내부적으로 HttpServletRequestWrapper를 활용하여 요청 객체에 보안 API를 추가하는 방식으로 동작한다. 이를 통해 보안 흐름을 완벽히 통합할 수 있으며, 명시적인 인증 로직 구현이 가능해진다.

---

## 4\. 클래스 구조 및 내부 구성

SecurityContextHolderAwareRequestFilter는 GenericFilterBean을 상속받으며, 주요 동작은 다음과 같은 방식으로 구성된다.

```
public class SecurityContextHolderAwareRequestFilter extends GenericFilterBean {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        // HttpServletRequest를 보안 메서드를 확장한 래퍼로 변환
        HttpServletRequest wrapped = new SecurityContextHolderAwareRequestWrapper(...);
        chain.doFilter(wrapped, response);
    }
}
```

이처럼 요청 객체를 SecurityContextHolderAwareRequestWrapper로 감쌈으로써, 내부적으로 인증 관련 메서드들을 활용 가능하도록 처리한다.

## 5\. 비동기 처리와의 연계

Spring Security는 기본적으로 ThreadLocal 기반의 보안 컨텍스트를 사용하기 때문에, 쓰레드가 바뀌는 @Async, Callable, CompletableFuture 등의 비동기 작업에서는 인증 정보가 전파되지 않는 문제가 존재한다.

SecurityContextHolderAwareRequestFilter는 이러한 문제를 보완하기 위해 AsyncContext.start() 등의 호출 시, 현재의 SecurityContext를 새 쓰레드로 복사하여 전달한다. 이는 보안 상태의 일관성을 유지하는 데 중요한 역할을 수행하며, 실무 환경에서의 안정성을 크게 향상시킨다.

## 6\. 설정 및 비활성화 방법

해당 필터는 Spring Security에서 자동으로 활성화되며, 대부분의 경우 수동 설정 없이도 문제없이 동작한다. 다만, 요청 객체를 완전히 커스터마이징하고자 할 경우 다음과 같이 비활성화할 수 있다.

```
http.servletApi(servlet -> servlet.disable());
```

---

## 7\. 요약 정리

| **항목** | **설명** |
| --- | --- |
| **필터명** | SecurityContextHolderAwareRequestFilter |
| **위치** | SecurityFilterChain의 13번째 필터 |
| **주요 기능** | login(), logout(), authenticate(), start() 등 제공 |
| **역할** | HttpServletRequest에 보안 기능을 추가하여 API 기반 인증을 가능케 함 |
| **비동기 작업 대응** | AsyncContext.start() 시 보안 컨텍스트를 새로운 쓰레드로 전파 |
| **기본 활성화 여부** | 기본 활성화되어 있으며 .servletApi().disable()로 비활성화 가능 |