## Spring Security - 기본 로그인 뷰 제공 필터, DefaultLoginPageGeneratingFilter (SecurityFilterChain 8번째 필터)

블로그 : https://pjs-world.tistory.com/entry/Spring-Security-%EA%B8%B0%EB%B3%B8-%EB%A1%9C%EA%B7%B8%EC%9D%B8-%EB%B7%B0-%EC%A0%9C%EA%B3%B5-%ED%95%84%ED%84%B0-DefaultLoginPageGeneratingFilter-SecurityFilterChain-8%EB%B2%88%EC%A7%B8-%ED%95%84%ED%84%B0

## 1\. DefaultLoginPageGeneratingFilter란?

Spring Security는 formLogin() 설정을 기본적으로 활성화하는 경우, 별도로 로그인 화면을 구성하지 않아도 자체적으로 동작하는 로그인 페이지를 제공한다.  
이 기능은 DefaultLoginPageGeneratingFilter라는 필터를 통해 구현되어 있으며, **GET /login** 요청이 들어왔을 때 HTML을 생성하여 응답하는 역할을 수행한다.

이 필터는 **SecurityFilterChain의 9번째** 필터로 등록되며, 사용자가 직접 로그인 페이지를 커스터마이징하지 않은 경우에만 활성화된다.

[##_Image|kage@blkCCd/btsNmlWBUIl/acEdhk9DkwdwrQ9l0JLpf0/img.png|CDM|1.3|{"originWidth":1530,"originHeight":562,"style":"alignCenter","caption":"SecurityFilterChain 9번째 필터"}_##]

## 2\. 필터의 동작 조건 및 흐름

해당 필터는 아래와 같은 조건에 해당하는 요청이 들어올 경우 동작하게 된다

| **요청 상황** | **설명** |
| --- | --- |
| **GET /login** | 사용자가 직접 로그인 페이지에 접근한 경우 |
| **GET /login?error** | 인증 실패 후 리디렉션된 경우 |
| **GET /login?logout** | 로그아웃 후 리디렉션된 경우 |

이러한 요청이 감지되면, 필터는 HTML을 문자열로 직접 생성하여 응답 본문에 작성한다.

## 3\. 필터의 실제 처리 방식

```
if (isLoginUrlRequest(request) || loginError || logoutSuccess) {
    String loginPageHtml = generateLoginPageHtml(request, loginError, logoutSuccess);
    response.getWriter().write(loginPageHtml);
    return;
}
```

위와 같이 isLoginUrlRequest() 메서드를 통해 요청이 로그인 페이지에 해당하는지 판단한 후, 필요 시 generateLoginPageHtml() 메서드를 통해 HTML을 생성하고, 이를 response.getWriter().write()로 클라이언트에 직접 응답한다.  
만약 해당 조건에 해당하지 않으면, 다음 필터로 요청이 넘어가게 된다.

## 4\. 클래스 구성 및 내부 메서드

| **구성 요소** | **설명** |
| --- | --- |
| **GenericFilterBean** | Spring Security 필터의 기반이 되는 추상 클래스 |
| **DefaultLoginPageGeneratingFilter** | 로그인 페이지 생성을 담당하는 실제 필터 |
| **doFilter()** | 조건을 검사한 후 HTML 응답 또는 다음 필터로 전달하는 메서드 |
| **generateLoginPageHtml()** | HTML 템플릿을 문자열로 동적으로 생성하는 메서드 |

## 5\. 커스터마이징 및 비활성화 방법

Spring Security는 기본 로그인 페이지를 제공하지만, 실제 서비스에서는 종종 사용자 정의 로그인 페이지가 필요하다.  
이 경우 다음과 같이 커스터마이징이 가능하다.

**기본 로그인 페이지 사용**

```
http.formLogin(Customizer.withDefaults());
```

**커스텀 로그인 페이지 설정**

```
http.formLogin(login -> login.loginPage("/my-login"));
```

loginPage()를 지정하면 기본 필터는 자동으로 비활성화되며, 사용자가 제공한 뷰를 통해 로그인 화면이 노출된다.

## 6\. 필터에서 HTML을 직접 응답하는 이유

Spring Security는 Spring MVC보다 앞서 **Filter** 단계에서 요청을 가로채 처리하는 구조를 기반으로 하고 있다.  
이러한 구조 상, 기본 로그인 뷰 역시 Controller가 아닌 **필터에서 직접 응답**하는 방식으로 설계되었다.

이유는 다음과 같다.

-   **충돌 방지**: 만약 Security가 Controller 방식으로 기본 로그인 페이지를 제공한다면, 사용자가 직접 작성한 커스텀 Controller와 충돌이 발생할 수 있다.
-   **유연한 제거**: 커스터마이징 시 필터 수준에서 쉽게 비활성화가 가능하다.

## 7\. 기타 참고 사항

| **항목** | **설명** |
| --- | --- |
| **기본 로그인 요청** | GET /login (→ 폼은 POST /login) |
| **활성 조건** | formLogin(), oauth2Login(), saml2Login() 중 하나라도 설정된 경우 |
| **비활성화 조건** | loginPage()를 명시하면 비활성화됨 |
| **필터 위치** | Security Filter Chain의 9번째 |

## 8\. 정리

DefaultLoginPageGeneratingFilter는 Spring Security가 제공하는 기본 로그인 페이지를 HTML 형태로 생성하여 클라이언트에 직접 응답하는 필터이다.  
이 필터는 Spring MVC보다 앞서 실행되며, 요청이 /login, /login?error, /login?logout에 해당하는 경우 필터 수준에서 HTML을 직접 응답한다.

실제 서비스에서는 대부분 커스텀 로그인 페이지를 사용하므로, loginPage() 설정을 통해 이 필터를 비활성화하는 것이 일반적이다.