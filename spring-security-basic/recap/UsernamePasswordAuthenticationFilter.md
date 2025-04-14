## Spring Security - 로그인 요청 처리, UsernamePasswordAuthenticationFilter (SecurityFilterChain 8번째 필터)

블로그: https://pjs-world.tistory.com/entry/Spring-Security-%EB%A1%9C%EA%B7%B8%EC%9D%B8-%EC%9A%94%EC%B2%AD-%EC%B2%98%EB%A6%AC-UsernamePasswordAuthenticationFilter-SecurityFilterChain-8%EB%B2%88%EC%A7%B8-%ED%95%84%ED%84%B0

## 1\. UsernamePasswordAuthenticationFilter란?

UsernamePasswordAuthenticationFilter는 Spring Security가 제공하는 인증 처리 필터로서, 주로 로그인 요청(기본적으로 /login)에 대한 처리를 담당한다. 해당 필터는 보안 필터 체인Security Filter Chain상에서 **8번째**에 위치하며, 사용자가 입력한 아이디와 비밀번호를 기반으로 인증 과정을 시작하는 핵심적인 역할을 수행한다.

[##_Image|kage@bJOUij/btsNjO6nR29/A3kOAeQHmWFFRJReehCwrK/img.png|CDM|1.3|{"originWidth":1516,"originHeight":570,"style":"alignCenter","caption":"Security Filter Chain 8번째 필터"}_##]

## 2\. 인증 요청 처리 흐름

1.  **로그인 요청 수신**  
    사용자가 /login 경로로 POST 요청을 보낼 경우, 해당 요청은 본 필터에 의해 가로채어진다. 이때 내부적으로는 AbstractAuthenticationProcessingFilter의 doFilter() 메서드가 호출된다.
2.  **사용자 입력값 추출**  
    필터는 obtainUsername()과 obtainPassword()를 통해 폼 데이터에서 사용자의 입력값을 추출한다.
3.  **인증 토큰 생성**  
    추출된 값은 UsernamePasswordAuthenticationToken.unauthenticated() 메서드를 통해 인증 전 상태의 토큰으로 생성된다.
4.  **AuthenticationManager에 위임**  
    필터는 생성된 토큰을 AuthenticationManager에게 전달하며, 실제 인증은 이 지점에서 시작된다.
5.  **DaoAuthenticationProvider를 통한 사용자 정보 조회 및 검증**  
    기본적으로는 DaoAuthenticationProvider가 이를 처리하며, 내부적으로 UserDetailsService를 호출하여 사용자 정보를 조회한 뒤 비밀번호를 비교한다.
6.  **인증 결과 저장**  
    인증에 성공한 경우, 최종적으로 인증 정보를 담은 토큰이 반환되며, 이는 SecurityContextHolder에 저장되어 이후의 요청에서 인증 정보를 사용할 수 있도록 한다.

[##_Image|kage@dCiXvk/btsNkwxK2Ok/0cffY1oeZBMK4KBOte52a0/img.png|CDM|1.3|{"originWidth":1212,"originHeight":498,"style":"alignCenter","width":953,"height":392,"caption":"UsernamePasswordAuthenticationFilter 내부 로직"}_##]

## 3\. 주요 구성 메서드

UsernamePasswordAuthenticationFilter는 다음과 같은 주요 메서드들로 구성되어 있다



| **메서드** | **역할** |
| --- | --- |
| **attemptAuthentication()** | 인증 전 토큰 생성 및 AuthenticationManager 위임 |
| **successfulAuthentication()** | 인증 성공 시 SecurityContext 저장 및 후처리 수행 |
| **unsuccessfulAuthentication()** | 인증 실패 시 오류 처리 및 응답 반환 |

## 4\. 커스터마이징 시 유의사항

SecurityFilterChain을 수동으로 구성할 경우, UsernamePasswordAuthenticationFilter는 자동으로 등록되지 않으며, 다음과 같이 명시적으로 formLogin() 설정을 해야 한다

http.formLogin(Customizer.withDefaults());


또한 로그인 URL이나 파라미터 이름(username/password)을 변경하고자 할 경우, 다음과 같이 설정을 추가로 구성해야 한다

http.formLogin()
.loginPage("/custom-login")
.usernameParameter("email")
.passwordParameter("pass");


## 5\. 관련 클래스와 역할

UsernamePasswordAuthenticationFilter와 함께 동작하는 주요 클래스들은 다음과 같다.

| **클래스명** | **역할** |
| --- | --- |
| **AbstractAuthenticationProcessingFilter** | 인증 필터의 공통 로직을 정의한 추상 클래스 |
| **UsernamePasswordAuthenticationToken** | 사용자 인증 요청 및 결과를 담는 객체 |
| **AuthenticationManager** | 인증 로직을 위임받아 처리하는 핵심 인터페이스 |
| **DaoAuthenticationProvider** | 사용자 정보 검증 및 인증 처리 구현체 |
| **UserDetailsService** | 사용자 정보를 로딩하는 서비스 인터페이스 |

## 6\. 정리

**UsernamePasswordAuthenticationFilter**는 Spring Security에서 로그인 기능을 담당하는 핵심적인 필터이다.  
Form 로그인 요청을 가로채어 인증 토큰을 생성하고, 이를 AuthenticationManager를 통해 인증 처리한 뒤, 그 결과를 SecurityContext에 저장하는 전체 흐름을 책임진다.

또한 이 필터는 기본 설정에 따라 자동으로 작동되지만, SecurityFilterChain을 수동으로 설정하는 경우에는 명시적으로 formLogin() 설정을 해주어야 하며, 커스터마이징 시 관련 클래스 간의 역할 분담을 정확히 이해하는 것이 중요하다.