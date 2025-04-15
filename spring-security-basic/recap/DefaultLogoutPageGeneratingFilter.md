## Spring Security - 기본 로그아웃 확인 페이지를 생성하는 필터, DefaultLogoutPageGeneratingFilter(SecurityFilterChain 10번째 필터)

블로그 : https://pjs-world.tistory.com/entry/Spring-Security-%EA%B8%B0%EB%B3%B8-%EB%A1%9C%EA%B7%B8%EC%95%84%EC%9B%83-%ED%99%95%EC%9D%B8-%ED%8E%98%EC%9D%B4%EC%A7%80%EB%A5%BC-%EC%83%9D%EC%84%B1%ED%95%98%EB%8A%94-%ED%95%84%ED%84%B0-DefaultLogoutPageGeneratingFilterSecurityFilterChain-10%EB%B2%88%EC%A7%B8-%ED%95%84%ED%84%B0

## 1\. DefaultLogoutPageGeneratingFilter란?

Spring Security는 로그아웃 요청이 들어왔을 때 바로 세션을 종료하고 로그아웃 처리하는 것이 일반적이다. 하지만 기본 설정에서는 사용자가 **GET /logout** 요청을 보냈을 때, 중간 확인용 HTML 페이지를 동적으로 생성하여 응답하는 기능을 제공하고 있다.

이 기능을 담당하는 필터가 바로 **DefaultLogoutPageGeneratingFilter**이다. 로그아웃 버튼을 누르기 전, "정말 로그아웃하시겠습니까?"와 같은 확인 화면을 보여주기 위한 용도로 동작한다.

해당 필터는 기본적으로 **SecurityFilterChain의 10번째** 위치에 존재한다.

[##_Image|kage@c4zpO9/btsNmf3tyu7/x9DU3stUnuLtg2Tl08NZY1/img.png|CDM|1.3|{"originWidth":1552,"originHeight":592,"style":"alignCenter","caption":"SecurityFilterChain의 10번째 필터"}_##]

## 2\. 필터 동작 조건

DefaultLogoutPageGeneratingFilter는 다음 조건이 만족될 때에만 동작한다.

| **조건** | **설명** |
| --- | --- |
| **GET /logout 요청** | 내부에 등록된 AntPathRequestMatcher("/logout")가 요청을 감지 |
| **formLogin() 활성화** | Spring Security에서 폼 로그인 방식이 설정되어 있어야 필터가 등록됨 |

**예시 설정**

```
http.formLogin(Customizer.withDefaults());
```

## 3\. 내부 동작 흐름

해당 필터는 요청이 **/logout**인지 검사하고, 맞다면 HTML을 문자열로 생성하여 응답한다. 그 외 요청은 단순히 필터 체인을 따라 다음 필터로 전달된다.

```
protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {

    if (this.matcher.matches(request)) {
        renderLogout(request, response); // HTML 직접 생성
    } else {
        chain.doFilter(request, response); // 다음 필터로
    }
}
```

이때 호출되는 renderLogout() 메서드는 StringBuilder를 사용하여 HTML 문자열을 동적으로 조립하고, 다음과 같은 로그아웃 확인 화면을 만들어낸다.

```
<form method="post" action="/logout">
  Are you sure you want to log out?
  <button type="submit">Log Out</button>
</form>
```

## 4\. 클래스 구조 및 핵심 메서드

| **구성 요소** | **설명** |
| --- | --- |
| **OncePerRequestFilter** | Spring Security의 공통 추상 필터. 요청당 한 번만 실행되는 것을 보장함 |
| **DefaultLogoutPageGeneratingFilter** | 로그아웃 요청을 감지하여 HTML을 직접 생성하는 필터 |
| **renderLogout()** | 동적으로 HTML 조합 및 응답 수행 |

실제 렌더링 로직에서는 부트스트랩을 적용한 간단한 스타일을 포함한 페이지가 생성되며, 사용자는 확인 후 POST /logout을 통해 진짜 로그아웃을 수행하게 된다.

## 5\. 커스터마이징 및 비활성화 방법

기본 제공되는 로그아웃 확인 페이지를 직접 커스터마이징하거나, 완전히 비활성화하고 싶은 경우 아래와 같은 방식으로 처리할 수 있다.

| **상황** | **설명** |
| --- | --- |
| **❌ 비활성화** | /logout 경로에 대응하는 @GetMapping 컨트롤러를 만들면 필터는 자동 비활성화됨 |
| **✅ 커스터마이징** | 커스텀 뷰 또는 전용 로그아웃 확인 페이지를 구성하여 필터 사용을 대체 가능 |

## 6\. 필터에서 HTML을 직접 렌더링하는 이유

Spring Security는 전통적인 Spring MVC보다 먼저 동작하는 **Servlet Filter 기반 구조**를 가진다.  
그 결과, 로그인 및 로그아웃 관련 기본 기능들은 Spring MVC의 @Controller를 사용하지 않고, **Filter 레벨에서 직접 처리**하도록 설계되었다.

이 방식은 다음과 같은 장점을 제공한다

-   사용자 커스텀 페이지와의 **충돌 방지**
-   컨트롤러 없이도 **기본 동작 보장**
-   보안 기능의 **독립성과 우선순위 유지**

## 7\. 요약

| **항목** | **설명** |
| --- | --- |
| **필터명** | DefaultLogoutPageGeneratingFilter |
| **위치** | SecurityFilterChain의 10번째 필터 |
| **동작 조건** | GET /logout 요청 & formLogin() 사용 중 |
| **응답 방식** | StringBuilder로 HTML을 조합하여 직접 응답 |
| **목적** | 로그아웃 전 확인 화면 제공 |
| **비활성화 조건** | 커스텀 /logout GET 컨트롤러 존재 시 자동 비활성화 |
| **커스터마이징** | 전용 HTML 페이지 또는 다른 경로로 리디렉션 구성 가능 |