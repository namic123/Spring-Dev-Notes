## Spring Security 필터 계층 구조 및 구현하는 법 (GenericFilterBean, OncePerRequestFilter)

작성 블로그 : https://pjs-world.tistory.com/entry/Spring-Security-%ED%95%84%ED%84%B0-%EA%B3%84%EC%B8%B5-%EA%B5%AC%EC%A1%B0-%EB%B0%8F-%EA%B5%AC%ED%98%84%ED%95%98%EB%8A%94-%EB%B2%95-GenericFilterBean-OncePerRequestFilter

### 📌 목차

[1\. 필터 구조 개요](#filter-architecture-overview) [2\. 필터 계층 구조](#filter-inheritance-hierarchy) [3\. 상속 구조의 이점](#benefits-of-inheritance) [4\. 핵심 추상 클래스 설명](#core-abstract-classes) [5\. Servlet Filter 인터페이스 구조](#servlet-filter-interface) [6\. 필터 체인 내 다음 필터 호출 방법](#how-to-call-next-filter) [7\. 필터 메소드 비교](#method-comparison) [8\. 결론 요약](#summary)

## 1\. 필터 구조 개요

Spring Security는 **인증, 인가, 세션 처리, 로그아웃, 예외 처리 등 다양한 보안 관련 기능을 일련의 필터 체인 구조로 처리**한다. 이러한 보안 필터들은 SecurityFilterChain에 등록되어 있으며, 서블릿 요청이 들어올 때 체인에 따라 순차적으로 실행된다.

이 구조 덕분에 개발자는 보안 로직을 세분화된 책임 단위로 구현할 수 있으며, 필요한 경우 커스텀 필터를 삽입하거나 특정 필터를 비활성화하는 방식으로 유연한 구성이 가능하다.

[##_Image|kage@bcq8bz/btsNfE9wkXa/kIh857FRvjZmVs9z1xduFK/img.png|CDM|1.3|{"originWidth":1281,"originHeight":443,"style":"alignCenter","caption":"Security filter chain 구조"}_##]

## 2\. 필터 계층 구조

Spring Security의 필터들은 다음과 같은 상속 계층 구조를 갖는다.

```
Servlet Filter (javax.servlet.Filter 인터페이스)
  └─ GenericFilterBean (추상 클래스)
       └─ OncePerRequestFilter (추상 클래스)
            └─ 구현1 (공통 로직 구현)
                 └─ 구현2 (실제로 필터로 등록됨)
```

**하나의 필터에 대한 모식도**

[##_Image|kage@p1mlo/btsNfHdXCZq/8s31ZruFu8NjQrW8ghA1Ek/img.png|CDM|1.3|{"originWidth":443,"originHeight":384,"style":"alignCenter","width":411,"height":356}_##]

**SecurityFilterChain에서 여러 필터에 대한 모식도**

[##_Image|kage@x0uc8/btsNdV57whD/w5BYVTBIojJqx6SfRvHPL1/img.png|CDM|1.3|{"originWidth":873,"originHeight":516,"style":"alignCenter"}_##]

-   **❗어떤 필터는 구현1 → 구현2의2단계 구조로 되어 있고, 어떤 필터는 구현2만 있는 이유는 공통 로직 구현이 필요한 것만 구현 1을 두고 아닌거는 구현 2로 두는 것.**

**핵심 개념 요약**

-   **구현1**: 필터의 **공통 동작** 또는 **기반 기능**을 담당
-   **구현2**: 실제로 **필터로 등록되는 구체 구현**이며, 구현1을 상속받아 필요한 로직을 완성함

**✔ 구현1이 존재하는 이유**

-   로그인 필터, 인증 필터, JWT 필터 등 다양한 필터에서 **공통으로 사용하는 로직**을 구현
-   예: 로깅, 에러 핸들링, 인증 객체 추출, 필터 공통 조건 검사 등
-   한 번만 구현하고 여러 필터에서 재사용 가능

**✔ 구현2가 존재하는 이유**

-   @Component, @Bean, 또는 SecurityFilterChain에서 **직접 필터로 등록되는 객체**
-   Spring Security 필터 체인에 등록되려면 반드시 실제 클래스로 존재해야 하며, 주로 OncePerRequestFilter 또는 GenericFilterBean을 직접 상속한 클래스만 등록

#### **예시로 이해하기**

-   **JWT 인증 필터 예시**

```
// 구현1: 공통 기능 구현
public abstract class AbstractJwtFilter extends OncePerRequestFilter {
    protected String resolveToken(HttpServletRequest request) {
        return request.getHeader("Authorization");
    }

    protected boolean validateToken(String token) {
        // 공통 토큰 유효성 검사
    }
}
```

```
// 구현2: 실제 등록되는 필터
@Component
public class JwtAuthenticationFilter extends AbstractJwtFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
        String token = resolveToken(request);
        if (validateToken(token)) {
            // SecurityContext에 인증 정보 저장
        }
        chain.doFilter(request, response);
    }
}
```

-   AbstractJwtFilter가 **공통 동작(구현1)**을 담당
-   JwtAuthenticationFilter가 **실제 필터로 등록(구현2)**

| **구분** | **역할** | **등록 여부** |
| --- | --- | --- |
| **구현1** | 공통 기능을 구현하는 **추상 또는 일반 클래스** | ❌ 직접 필터로 등록 X |
| **구현2** | 실제 필터로 등록되어 동작하는 **구체 클래스** | ✅ 필터로 등록됨 |

**실제로 많이 쓰이는 구조 예**

**예 1: JWT 계열 인증 필터 (공통 로직)**

-   **AbstractJwtFilter → JwtAuthenticationFilter**
-   토큰 파싱, 검증 로직은 부모에서 하고
-   실제 사용자 인증/인증정보 저장은 자식에서 함

**예 2: CSRF 필터, 로그아웃 필터 등 (공통 로직 無)**

-   LogoutFilter 같이 하나의 목적만 수행하는 경우는
-   그냥 OncePerRequestFilter나 GenericFilterBean 상속 후 바로 필터로 등록

## 3\. 상속 구조의 이점

Spring Security의 필터 구조는 상속 기반의 추상화 덕분에 다음과 같은 장점을 제공한다.

-   **중복 코드 제거**: 공통 로직은 상위 클래스에서 처리하고, 개별 필터는 핵심 로직만 구현함.
-   **일관성 유지**: 필터 간 공통된 구조로 일관된 관리가 가능함.
-   **유연한 확장**: 하위 클래스에서 필요에 따라 메서드를 오버라이드할 수 있음.

예를 들어 UsernamePasswordAuthenticationFilter와 같은 인증 필터와 LogoutFilter는 서로 다른 기능을 수행하지만 동일한 추상 클래스에서 파생되어 통합된 구조를 유지한다.

## 4\. 핵심 추상 클래스 설명

**GenericFilterBean**

```
public abstract class GenericFilterBean implements Filter, BeanNameAware, EnvironmentAware, ServletContextAware, InitializingBean, DisposableBean
```

-   Spring 통합 환경에서의 서블릿 필터 기본 클래스
-   doFilter() 메서드를 구현해야 하며, Bean 이름, 환경 설정 등을 주입받을 수 있다.
-   대부분의 Spring Security 필터는 이 클래스를 기반으로 구현된다.

**OncePerRequestFilter**

```
public abstract class OncePerRequestFilter extends GenericFilterBean {
    protected abstract void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException;
}
```

-   GenericFilterBean을 상속한 추상 클래스
-   동일 요청 내에서 필터가 **단 한 번만 실행되도록 보장**
-   **주로 보안 필터에서 중복 호출을 방지**하고자 할 때 사용됨

## 5\. Servlet Filter 인터페이스 구조

```
public interface Filter {
    default void init(FilterConfig filterConfig) throws ServletException {}
    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException;
    default void destroy() {}
}​
```

| **메서드** | **설명** |
| --- | --- |
| **init()** | 필터 초기화 시 1회 실행됨 |
| **doFilter()** | 매 요청마다 실행되는 메인 로직 |
| **destroy()** | 서버 종료 또는 필터 제거 시 실행됨 |

doFilter() 메서드 안에서 반드시 chain.doFilter()를 호출하여 다음 필터로 요청을 넘겨야 한다. 호출하지 않을 경우 필터 체인이 중단된다.

## 6\. 필터 체인 내 다음 필터 호출 방법

다음은 LogoutFilter의 내부 구현 예시이다.

```
public class LogoutFilter extends GenericFilterBean {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
	}

	private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		// 요청 전 처리
		// ...

		// 다음 필터 호출
		chain.doFilter(request, response);

		// 응답 후 처리
		// ...
	}
}
```

이처럼 chain.doFilter()는 다음 필터로 요청을 넘기는 역할을 하며, 이후 로직은 후처리로 수행된다.

## 7\. 필터 메소드 비교

| **클래스** | **실행 메서드** | **특징** |
| --- | --- | --- |
| **Filter** | doFilter() | 서블릿 표준 인터페이스 |
| **GenericFilterBean** | doFilter() | Spring 통합 기능 포함 |
| **OncePerRequestFilter** | doFilterInternal() | 요청당 1회만 실행 보장 |

특히 OncePerRequestFilter는 내부적으로 doFilter()를 오버라이드하여 doFilterInternal()을 한 번만 실행하도록 제어한다.

## 8\. 결론 요약

| **항목** | **설명** |
| --- | --- |
| **필터 구조** | Filter → GenericFilterBean → OncePerRequestFilter 순으로 계층화 |
| **상속 이점** | 공통 로직 분리, 필터 재사용성 향상 |
| **GenericFilterBean** | Spring 기반 필터 구현의 기반 |
| **OncePerRequestFilter** | 요청당 1회 실행을 보장하는 보안 전용 필터 베이스 |
| **실행 메서드** | 외부는 doFilter(), 내부 구현은 doFilterInternal() |

Spring Security에서 보안 로직을 구현하거나 커스터마이징하고자 할 때, 이와 같은 필터 계층 구조를 명확히 이해하는 것이 무엇보다 중요하다. 특히 커스텀 필터를 작성하거나 기존 필터를 재정의하고자 하는 경우, 각 클래스의 책임과 역할을 구분하여 정확한 위치에 구현하는 것이 핵심이다.