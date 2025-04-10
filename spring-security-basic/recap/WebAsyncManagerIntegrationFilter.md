## Spring Security 비동기 컨트롤러에서 인증 정보 유지 - WebAsyncManagerIntegrationFilter]
블로그: https://pjs-world.tistory.com/entry/Spring-Security-%EB%B9%84%EB%8F%99%EA%B8%B0-%EC%BB%A8%ED%8A%B8%EB%A1%A4%EB%9F%AC%EC%97%90%EC%84%9C-%EC%9D%B8%EC%A6%9D-%EC%A0%95%EB%B3%B4-%EC%9C%A0%EC%A7%80-WebAsyncManagerIntegrationFilter

### 📌 목차

[1\. 왜 WebAsyncManagerIntegrationFilter가 필요한가?](#why-need) [2\. 필터의 동작 방식](#how-it-works) [3\. 클래스 구조](#code-structure) [4\. 비동기 컨트롤러 동작 예시](#async-controller-example) [5\. 내부 호출 흐름 요약](#flow-summary) [6\. 결론 및 정리](#summary)

## 1\. 왜 WebAsyncManagerIntegrationFilter가 필요한가?

Spring Security는 기본적으로 **ThreadLocal 기반의 SecurityContextHolder** 를 통해 인증 정보를 저장하고 공유한다. 이 구조는 요청이 단일 스레드 내에서 처리될 경우, 안전하고 효율적이다. 그러나, **@Async, Callable, DeferredResult**와 같은 **Spring MVC의 비동기 처리 기능을 사용할 때**는 문제가 발생할 수 있다.

이유는 간단하다. 비동기 컨트롤러는 **컨트롤러 이후의 실행 흐름이 별도의 스레드에서 처리**되기 때문에, 기존 요청 스레드에 존재하던 SecurityContext 정보가 새로운 쓰레드에 전달되지 않는 것이다.

이러한 문제를 해결하기 위해 Spring Security는 **WebAsyncManagerIntegrationFilter**를 Security Filter Chain의 **앞단(두 번째 위치)** 에 자동으로 등록하여, 인증 정보를 비동기 흐름에서도 안전하게 전파한다.

[##_Image|kage@qnfMi/btsNfeKBsMS/BVcVUQhU2sHGUsnm3A6E8K/img.png|CDM|1.3|{"originWidth":1070,"originHeight":676,"style":"alignCenter"}_##]

## 2\. 필터의 동작 방식

이 필터는 DispatcherServlet 내부에 존재하는 WebAsyncManager에 SecurityContextCallableProcessingInterceptor를 등록한다. 이 인터셉터는 비동기 처리 시, **기존 요청 스레드의 보안 컨텍스트(SecurityContext)를 새로운 스레드에 복사**하는 역할을 수행한다.

따라서, 인증 정보를 필요로 하는 로직이 비동기 컨텍스트에서 수행되더라도 문제없이 SecurityContextHolder.getContext()를 통해 인증 정보를 조회할 수 있게 된다.

## 3\. 클래스 구조

```
/**
 * 비동기 처리에서 SecurityContext를 새로운 쓰레드로 전달하기 위한 필터.
 * 
 * Callable, DeferredResult 등으로 요청 처리 시 쓰레드가 변경되더라도
 * 기존 요청 쓰레드의 SecurityContext를 유지하도록 도와준다.
 *
 */
public final class WebAsyncManagerIntegrationFilter extends OncePerRequestFilter {

    private static final Object CALLABLE_INTERCEPTOR_KEY = new Object();
    private SecurityContextHolderStrategy securityContextHolderStrategy =
        SecurityContextHolder.getContextHolderStrategy();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);

        if (asyncManager.getCallableInterceptor(CALLABLE_INTERCEPTOR_KEY) == null) {
            SecurityContextCallableProcessingInterceptor interceptor =
                new SecurityContextCallableProcessingInterceptor();
            interceptor.setSecurityContextHolderStrategy(this.securityContextHolderStrategy);

            asyncManager.registerCallableInterceptor(CALLABLE_INTERCEPTOR_KEY, interceptor);
        }

        filterChain.doFilter(request, response);
    }
}
```

## 4\. 비동기 컨트롤러 동작 예시

다음 예시는 Spring MVC에서 비동기 컨트롤러를 사용했을 때, SecurityContext가 어떻게 유지되는지를 보여준다.

```
@GetMapping("/async")
@ResponseBody
public Callable<String> asyncPage() {
    System.out.println("start " + SecurityContextHolder.getContext().getAuthentication().getName());

    return () -> {
        Thread.sleep(4000);
        System.out.println("end " + SecurityContextHolder.getContext().getAuthentication().getName());
        return "async";
    };
}
```

이 예제에서 start와 end 모두 동일한 사용자 이름이 출력된다면, SecurityContext가 성공적으로 비동기 스레드로 전파되었다는 뜻이다.

**출력 로그**

[##_Image|kage@blAuhc/btsNesiv11O/0l27pwTnHM2wtAunlHhRT0/img.png|CDM|1.3|{"originWidth":1713,"originHeight":565,"style":"alignLeft"}_##][##_Image|kage@0e4iZ/btsNgBR451X/2SVaKoF0jfxHZHyGmOFsoK/img.png|CDM|1.3|{"originWidth":164,"originHeight":41,"style":"alignLeft"}_##]

## 5\. 내부 호출 흐름 요약

1. 클라이언트 요청  
2. WebAsyncManagerIntegrationFilter 실행 → SecurityContext 인터셉터 등록  
3. DispatcherServlet이 핸들러 실행  
4. 핸들러에서 Callable 리턴  
5. WebAsyncManager가 Callable 실행 요청  
6. 등록된 인터셉터가 기존 SecurityContext를 새로운 스레드로 전파  
7. 비동기 스레드에서도 SecurityContext 유지

## 6\. 결론 및 정리

| **항목** | **설명** |
| --- | --- |
| **필터 명칭** | WebAsyncManagerIntegrationFilter |
| **필요성** | 비동기 컨트롤러에서 인증 정보 유지 |
| **작동 원리** | WebAsyncManager에 인터셉터를 등록하여 SecurityContext를 전파 |
| **기반 클래스** | OncePerRequestFilter |
| **위치** | SecurityFilterChain의 앞단 (기본 두 번째) |
| **적용 대상** | Callable, DeferredResult, @Async 기반 컨트롤러 |
| **이점** | ThreadLocal의 한계를 극복하여 인증 상태의 일관성 유지 |