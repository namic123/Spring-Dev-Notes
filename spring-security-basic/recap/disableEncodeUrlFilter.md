## Spring Security - DisableEncodeUrlFilter, URL 세션 노출 방지
작성 블로그 : https://pjs-world.tistory.com/entry/Spring-Security-DisableEncodeUrlFilter-URL-%EC%84%B8%EC%85%98-%EB%85%B8%EC%B6%9C-%EB%B0%A9%EC%A7%80

### 📌 목차

[1\. DisableEncodeUrlFilter의 등장 배경](#purpose) [2\. 필터 내부 구조 및 작동 방식](#filter-structure) [3\. 기존 서블릿 컨테이너의 기본 동작 방식](#servlet-default) [4\. 필터 비활성화 방법 및 고려사항](#disable-guide) [5\. 정리 및 실무 적용 팁](#summary)

## 1\. DisableEncodeUrlFilter의 등장 배경

웹 애플리케이션에서 클라이언트의 세션을 유지하기 위해 서버는 일반적으로 쿠키를 활용한다. **그러나 브라우저에서 쿠키를 비활성화한 환경에서는 세션 식별자를 URL에 포함시켜 전달하는 방식이 사용**되며, 이는 다음과 같은 형태로 노출된다.

```
http://localhost:8080/home;jsessionid=123ABC456DEF
```

이와 같은 **URL 노출은 세션 탈취(Session Hijacking)**의 위험을 높이는 요인으로 작용할 수 있다. 이러한 보안 문제를 사전에 차단하고자, Spring Security에서 DisableEncodeUrlFilter를 통해 해당 기능을 무력화하는 보안 필터를 기본으로 제공한다.

## 2\. 필터 내부 구조 및 작동 방식

**DisableEncodeUrlFilter는 OncePerRequestFilter를 상속하여 구현**되며, HTTP 응답 객체를 감싸는 래퍼를 통해 URL 인코딩 메서드들의 동작을 변경한다.

```
@Override
protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
    filterChain.doFilter(request, new DisableEncodeUrlResponseWrapper(response));
}
```

이때 감싸지는 래퍼 클래스 DisableEncodeUrlResponseWrapper는 encodeURL() 및 encodeRedirectURL() 메서드를 오버라이딩하여, **어떠한 경우에도 세션 ID가 URL에 추가되지 않도록 설정**한다.

```
// Response 클래스 메서드
@Override
public String encodeURL(String url) {
    return url; // 원본 URL 그대로 반환
}
```

결과적으로, 클라이언트가 쿠키를 비활성화하더라도 JSESSIONID가 URL에 노출되지 않도록 방지할 수 있다.

## 3\. 기존 서블릿 컨테이너의 기본 동작 방식

Tomcat과 같은 표준 서블릿 컨테이너에서는 **HttpServletResponse.encodeURL()** 호출 시 **내부적으로 세션 정보를 포함하여 URL을 자동 인코딩**한다. 다음은 그 대표적인 구현 방식이다.

```
@Override
public String encodeRedirectURL(String url) {
    if (isEncodeable(toAbsolute(url))) {
        return toEncoded(url, request.getSessionInternal().getIdInternal());
    } else {
        return url;
    }
}
```

이로 인해 URL에 ;jsessionid=XYZ와 같은 문자열이 추가되어 출력 로그, 이메일 링크, 외부 API 전송 경로 등에서 민감한 세션 정보가 노출될 수 있다. 이는 의도치 않은 보안 결함으로 이어질 수 있으며, 반드시 제어가 필요한 부분이다.

## 4\. 필터 비활성화 방법 및 고려사항

Spring Security에서는 해당 필터를 **SecurityFilterChain의 가장 앞단에 자동 등록**한다. 하지만, 다음과 같이 세션 관리 자체를 비활성화하면 필터 또한 제거된다.

```
http
    .sessionManagement((manage) -> manage.disable());
```

이는 API 서버나 Stateless 환경 등 세션 자체가 필요 없는 구조에서 활용될 수 있다. 단, 이 경우 **로그인 기반의 인증 구조는 사용하지 않도록 설계되어야 하며**, JWT 등 대체 수단을 사용해야 한다.

## 5\. 정리 및 실무 적용 팁

| **항목** | **설명** |
| --- | --- |
| **목적** | 세션 ID가 URL에 인코딩되어 노출되는 것을 방지하기 위함 |
| **기본 동작** | encodeURL() 및 encodeRedirectURL() 메서드를 무력화 |
| **위치** | SecurityFilterChain의 가장 앞단에 자동 등록 |
| **비활성화 조건** | sessionManagement().disable() 설정 시 필터 자동 제거 |
| **실무 권장** | 쿠키 대신 세션 ID를 URL로 전달하지 않도록 기본 활성화 상태 유지 필요 |

DisableEncodeUrlFilter는 **애플리케이션 보안을 위한 최소한의 기본 장치**로 작용한다. 개발자가 따로 신경 쓰지 않아도 Spring Security가 알아서 보호해주는 영역이지만, 동작 방식을 명확히 이해하고 있어야 커스터마이징이나 디버깅 과정에서도 불필요한 혼란을 줄일 수 있다.