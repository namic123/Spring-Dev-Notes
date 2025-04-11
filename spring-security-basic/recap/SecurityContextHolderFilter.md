## Spring Security SecurityContextHolderFilter의 동작 구조 (SecurityFilterChain 3번째 필터)

**참고용 발행 블로그**
https://pjs-world.tistory.com/entry/Spring-Security-SecurityContextHolderFilter%EC%9D%98-%EB%8F%99%EC%9E%91-%EA%B5%AC%EC%A1%B0-SecurityFilterChain-3%EB%B2%88%EC%A7%B8-%ED%95%84%ED%84%B0

### 📌 목차

[1\. SecurityContextHolderFilter란?](#intro) [2\. 필터의 위치와 역할](#role) [3\. 요청 처리 흐름 상세](#flow) [4\. SecurityContextRepository의 구조와 구현체](#repository) [5\. SecurityContextPersistenceFilter와의 차이점](#comparison) [6\. 실무 적용 시 주의 사항](#considerations)

## 1\. SecurityContextHolderFilter란?

Spring Security 5.8부터 도입된 SecurityContextHolderFilter는 기존의 SecurityContextPersistenceFilter를 대체하기 위해 설계된 보안 필터이다. 이 필터는 보안 컨텍스트의 저장과 초기화를 담당하며, 특히 Stateless 환경에서의 Context 보장 강화를 목표로 한다.

해당 필터는 Spring Security 필터 체인에서 **세 번째 위치에 존재**하며, 다음과 같은 기능을 수행한다.

-   요청이 들어오면 **인증 정보를 로딩하여 SecurityContextHolder에 등록**
-   요청 처리가 끝나면 **SecurityContextHolder.clearContext()를 통해 인증 정보 초기화**

[##_Image|kage@rAq3T/btsNixa6BVZ/XO8MnJeSdKLkDTYk6koUNk/img.png|CDM|1.3|{"originWidth":1221,"originHeight":473,"style":"alignCenter","caption":"SecurityFilterChain 3번째 필터"}_##]

## 2\. 필터의 위치와 역할

SecurityContextHolderFilter는 내부적으로 **SecurityContextRepository를 활용하여 인증 정보를 저장하거나 로딩하는 작업을 수행한다**. 해당 클래스의 핵심 역할은 다음과 같다.

```
public class SecurityContextHolderFilter extends GenericFilterBean {
    // SecurityContextRepository를 통해 인증 정보 위임 처리
}
```

또한 설정을 통해 해당 필터의 등록 여부를 조정할 수 있다.

```
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.securityContext(context -> context.disable()); // SecurityContextHolderFilter 비활성화
    return http.build();
}
```

## 3\. 요청 처리 흐름 상세

Spring Security는 요청 시 **SecurityContextRepository.loadDeferredContext(request)**를 호출하여 인증 정보를 로딩하고, 이를 **SecurityContextHolder.setDeferredContext()**를 통해 저장한다. 이후 컨트롤러 또는 다른 필터들은 해당 컨텍스트에서 인증 정보를 참조할 수 있다.

요청 종료 시에는 다음과 같이 인증 정보가 초기화된다.

Spring Security 6에서는 이러한 로직을 lazy evaluation 방식으로 처리하여, 필요할 때만 인증 정보를 참조하도록 최적화하였다.

```
finally {
    SecurityContextHolder.clearContext();
}
```

Spring Security 6에서는 이러한 로직을 lazy evaluation 방식으로 처리하여, 필요할 때만 인증 정보를 참조하도록 최적화하였다.

| **\* Lazy evaluation?** Lazy Evaluation(지연 평가은 어떤 값이나 객체를 **바로 계산하지 않고, 실제로 필요할 때 계산**하는 방식      **Spring Security에서의 적용: loadDeferredContext()      **이 메서드는 SecurityContext를 바로 로드하지 않는다. 대신, 내부적으로 DeferredSecurityContext 같은 proxy 객체를 반환해서,**SecurityContextHolder.getContext()가 호출될 때, 비로소 로드한다.**  |
| --- |

## 4\. SecurityContextRepository의 구조와 구현체

SecurityContextRepository는 인증 정보를 저장하거나 로딩하는 저장소 역할을 추상화한 인터페이스이다. 아래는 그 대표적인 구현체들이다.

| **구현체** | **설명** |
| --- | --- |
| **HttpSessionSecurityContextRepository** | 세션을 통해 인증 정보를 저장/조회 (기본값) |
| **RequestAttributeSecurityContextRepository** | 요청 객체에 인증 정보를 저장 |
| **NullSecurityContextRepository** | 인증 정보를 저장하지 않음 (Stateless 환경에 적합) |

설정을 통해 다음과 같이 명시적으로 지정할 수 있다.

```
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.securityContext(context -> 
        context.securityContextRepository(new RequestAttributeSecurityContextRepository())
    );
    return http.build();
}
```

**아래는 저장 구현체와 SecurityContextHolder를 표현하는 모식도이다.**

[##_Image|kage@zYJ3o/btsNi3ujrp3/kFGFksxOWaiXFL8WUkEynk/img.png|CDM|1.3|{"originWidth":1121,"originHeight":594,"style":"alignCenter"}_##]

**역할 정리**

**SecurityContextRepository**

-   인증 정보를 저장하거나 꺼내오는 역할을 담당하는 인터페이스
-   다양한 구현체를 통해 실제 저장소를 선택할 수 있음

| **구현체** | **설명** |
| --- | --- |
| **서버 세션 (메모리)** | 기본적으로 HttpSessionSecurityContextRepository를 의미 |
| **Redis** | 예: 커스텀 저장소 또는 인증 서버에서 토큰 정보를 저장하는 경우 |
| **HTTP 메서드 기반** | RequestAttributeSecurityContextRepository처럼 Request 속성에 저장하는 방식 |

-   이미지는 이 Repository가 **구현체에 따라 다르게 연결될 수 있음**을 시각화한 것

**SecurityContextHolder**

-   ThreadLocal 기반으로 인증 정보를 저장하는 곳
-   현재 요청을 처리하는 쓰레드 내에서만 접근 가능한 SecurityContext를 유지함
-   여러 개의 박스로 표현된 건 **여러 쓰레드마다 각각의 Context를 갖는 구조**를 표현한 것

## 5\. SecurityContextPersistenceFilter와의 차이점

| **항목** | **SecurityContextPersistenceFilter** | **SecurityContextHolderFilter** |
| --- | --- | --- |
| **사용 버전** | Spring Security 5.7 이하 | 5.8 이상 |
| **자동 저장** | Context 변경 시 자동 저장 | ❌ 자동 저장 없음 |
| **Deprecated 여부** | ✅ Deprecated | ❌ 권장 방식 |
| **저장 방식** | 저장소에 자동 저장 | 명시적 저장 필요 |
| **적용 구조** | saveContext() 포함 | load만 수행 |

핵심적으로, SecurityContextHolderFilter는 보안 컨텍스트의 저장 작업을 자동으로 수행하지 않는다. 이는 JWT 기반의 Stateless 인증 환경을 고려한 설계이며, 개발자가 명시적으로 SecurityContext를 저장하도록 유도한다.

## 6\. 실무 적용 시 주의 사항

보안 필터 설정 시 다음과 같은 환경별 고려 사항이 존재한다.

-   **JWT 기반 인증 환경**
    -   NullSecurityContextRepository를 사용해야 함
    -   인증 성공 시 SecurityContextHolder.getContext().setAuthentication()을 명시적으로 수행
    -   저장 생략이 기본이므로 필요한 경우 명시적으로 save 구현 필요
-   **세션 기반 인증 환경**
    -   기본 HttpSessionSecurityContextRepository 사용
    -   로그인 이후 자동 저장 가능하나, 최근 버전에서는 명시적 저장이 권장됨
-   **SecurityContextHolderStrategy**
    -   내부적으로 ThreadLocal 기반 저장 전략을 사용
    -   멀티 쓰레드 환경에서도 인증 정보가 각 요청별로 안전하게 분리됨