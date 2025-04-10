# 🛡️ Spring Security 내부 구조 학습 정리

이 문서는 Spring Security의 내부 필터 구조와 주요 컴포넌트에 대한 핵심 개념을 간단히 정리한 자료입니다. 실습 프로젝트와 함께 필터의 동작 순서와 역할을 학습하기 위해 작성되었습니다.

### 작성한 블로그 : https://pjs-world.tistory.com/
---

### 2. 시큐리티 동작 원리
Spring Security는 서블릿 필터 기반으로 동작하며, 요청이 들어오면 여러 보안 필터들을 통과한 후 DispatcherServlet으로 전달됩니다. 이 필터 체인 구조를 통해 인증, 인가, 세션, 예외 처리 등이 수행됩니다.

### 3. DelegatingFilterProxy & FilterChainProxy
`DelegatingFilterProxy`는 Spring이 관리하는 빈으로 필터 역할을 수행하고, 내부적으로 실제 필터 체인인 `FilterChainProxy`에 요청을 위임합니다. `FilterChainProxy`는 URL 패턴에 따라 `SecurityFilterChain`을 선택하여 실행합니다.

### 4. SecurityFilterChain 등록 방식
`SecurityFilterChain`은 하나 이상의 보안 필터들을 포함하는 필터 체인입니다. `HttpSecurity`를 통해 필터 구성 및 등록이 가능하며, 여러 체인을 조건에 따라 분기하여 설정할 수도 있습니다.

### 5. SecurityFilterChain 구조 분석
하나의 `SecurityFilterChain`에는 수십 개의 필터가 순차적으로 구성되어 있으며, 각 필터는 특정한 보안 기능(예: 인증, 로그아웃, CSRF 등)을 담당합니다. 필터 순서는 Spring Security 내부적으로 엄격히 정의되어 있습니다.

### 6. SecurityContextHolder의 역할
`SecurityContextHolder`는 현재 인증된 사용자 정보를 보관하는 객체입니다. `ThreadLocal`을 이용하여 각 요청 쓰레드마다 독립적으로 `SecurityContext`를 유지합니다.

### 7. 필터 상속과 요청 전파 구조
보안 필터들은 `GenericFilterBean` 또는 `OncePerRequestFilter`를 상속받아 구현되며, `doFilter()` 또는 `doFilterInternal()`을 통해 다음 필터로 요청을 전파합니다.

### 8. GenericFilterBean vs OncePerRequestFilter
- `GenericFilterBean`: 요청이 필터를 통과할 때마다 실행됨
- `OncePerRequestFilter`: 동일 요청 내에서 한 번만 실행됨 (주로 보안 필터에 사용)

### 9. DisableEncodeUrlFilter
세션 ID가 URL에 노출되는 것을 방지하기 위한 필터입니다. `encodeURL()`과 `encodeRedirectURL()`을 무력화하여 `;jsessionid=...` 노출을 차단합니다.

### 10. WebAsyncManagerIntegrationFilter
비동기 컨트롤러(`Callable`)에서 쓰레드가 바뀌더라도 `SecurityContext`를 유지할 수 있도록 `WebAsyncManager`에 인터셉터를 등록하는 필터입니다.

### 11. SecurityContextHolderFilter
이전 요청에서 인증된 사용자의 `SecurityContext`를 불러와 현재 요청에 설정하고, 요청이 끝나면 `SecurityContextHolder`를 초기화하는 필터입니다.

### 12. HeaderWriterFilter
보안 관련 헤더(`X-Content-Type-Options`, `X-Frame-Options` 등)를 응답에 추가하는 필터입니다. 보안 헤더 정책을 설정할 때 사용됩니다.

### 13. CorsFilter
브라우저의 CORS 정책을 처리하는 필터입니다. Origin, Method, Header 등을 제어하여 도메인 간 요청을 허용하거나 차단합니다.

### 14. CsrfFilter
CSRF 공격을 방지하기 위한 필터로, 요청 시 CSRF 토큰의 유효성을 검증합니다. 주로 `POST`, `PUT`, `DELETE` 요청에 적용됩니다.

### 15. LogoutFilter
사용자 로그아웃을 처리하는 필터입니다. 로그아웃 요청을 감지하여 세션 제거 및 SecurityContext 초기화를 수행합니다.

### 16. UsernamePasswordAuthenticationFilter
폼 로그인 요청을 처리하는 핵심 필터입니다. `username`과 `password`를 검증하고 인증 객체를 생성하여 SecurityContext에 저장합니다.

### 17. DefaultLoginPageGeneratingFilter
로그인 페이지를 별도로 정의하지 않은 경우, 기본 로그인 HTML 페이지를 자동으로 생성하여 응답하는 필터입니다.

### 18. DefaultLogoutPageGeneratingFilter
로그아웃 URL이 지정되었지만 별도의 뷰가 없을 경우, 기본 로그아웃 페이지를 생성해주는 필터입니다.

### 19. BasicAuthenticationFilter
HTTP Basic 인증을 처리하는 필터입니다. 요청 헤더에 포함된 Authorization 정보를 파싱하여 인증을 수행합니다.

### 20. RequestCacheAwareFilter
인증이 완료된 후 원래 요청한 URI로 리다이렉트하기 위한 캐시 기능을 제공합니다. 주로 로그인 성공 후 이전 페이지로 이동할 때 사용됩니다.

### 21. SecurityContextHolderAwareRequestFilter
HttpServletRequest에 보안 관련 메서드를 추가하여 인증 정보 확인, 권한 체크 등의 기능을 제공합니다.

### 22. AnonymousAuthenticationFilter
인증되지 않은 사용자도 SecurityContext에 익명 사용자 객체(`AnonymousAuthenticationToken`)를 부여하여 일관된 방식으로 권한을 처리할 수 있도록 합니다.

### 23. ExceptionTranslationFilter
예외(인증 실패, 인가 실패 등)를 처리하는 필터입니다. 적절한 인증 페이지 리다이렉트 또는 오류 응답을 반환합니다.

### 24. AuthorizationFilter
인증이 완료된 사용자가 특정 요청에 접근 가능한지 인가(Authorization)를 체크하는 필터입니다. `hasRole`, `hasAuthority` 등의 표현식 기반으로 동작합니다.

### 25. SessionManagementConfigurer
세션 생성 전략, 동시 로그인 제어, 세션 고정 보호 등을 설정하는 구성 요소입니다. 세션 보안을 강화하고 다양한 인증 전략을 조절할 수 있습니다.

