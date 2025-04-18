## Spring Security 계층 권한 설정하기, Role Hierarchy 개념과 활용

블로그 : https://pjs-world.tistory.com/entry/Spring-Security-%EA%B3%84%EC%B8%B5-%EA%B6%8C%ED%95%9C-%EC%84%A4%EC%A0%95%ED%95%98%EA%B8%B0-Role-Hierarchy-%EA%B0%9C%EB%85%90%EA%B3%BC-%ED%99%9C%EC%9A%A9

## 1\. Role Hierarchy란 무엇인가?

**Role Hierarchy**, 즉 계층 권한이란 상위 권한을 가진 사용자가 하위 권한까지도 함께 보유하도록 만드는 Spring Security의 기능이다.  
예를 들어 ROLE\_C라는 권한을 가진 사용자가 있다고 가정할 때, 이 사용자는 ROLE\_B, ROLE\_A에 접근할 수 있는 구조를 설정할 수 있다. 이렇게 하면 하나의 권한만으로 여러 하위 리소스에 접근할 수 있어 **권한 관리가 유연해지고 유지보수가 쉬워지는** 장점이 있다.

---

## 2\. 계층 권한 미적용 시의 문제점

계층 권한을 적용하지 않는다면, 각 URL 경로에 대해 접근 가능한 권한을 직접 명시해야 한다. 아래 예시와 같이 /에는 ROLE\_A, ROLE\_B, ROLE\_C를 모두 허용하고, /manager는 ROLE\_B, ROLE\_C만, /admin은 ROLE\_C만 허용하는 식이다.

```
http
    .authorizeHttpRequests((auth) -> auth
        .requestMatchers("/").hasAnyRole("A", "B", "C")
        .requestMatchers("/manager").hasAnyRole("B", "C")
        .requestMatchers("/admin").hasAnyRole("C")
    );
```

문제는 **역할이 늘어날수록 중복 코드와 관리 포인트가 급증**하게 된다는 점이다.

---

## 3\. Role Hierarchy 적용 방법

Spring Security에서는 RoleHierarchyImpl을 Bean으로 등록하여 계층 구조를 정의할 수 있다. 설정 방법은 다음과 같다.

**SecurityConfig 등 설정 클래스에서 빈등록**

```
@Bean
public RoleHierarchy roleHierarchy() {
    RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
    hierarchy.setHierarchy("""
        ROLE_C > ROLE_B
        ROLE_B > ROLE_A
    """);
    return hierarchy;
}
```

-   ROLE\_C > ROLE\_B: C는 B 권한을 포함한다.
-   ROLE\_B > ROLE\_A: B는 A 권한을 포함한다.

결과적으로 다음과 같은 구조가 성립한다

[##_Image|kage@6rudO/btsNslvauus/FnuGgqxHsQ2Gu5sUtuBpC0/img.png|CDM|1.3|{"originWidth":236,"originHeight":144,"style":"alignLeft"}_##]

---

## 4\. 필터 체인에의 간결한 적용

계층 구조가 적용된 이후에는 필터 체인 설정이 훨씬 간결해진다. 예를 들어 다음과 같은 방식으로 정의할 수 있다.

```
http
    .authorizeHttpRequests((auth) -> auth
        .requestMatchers("/").hasAnyRole("A")        // ROLE_A, ROLE_B, ROLE_C 접근 가능
        .requestMatchers("/manager").hasAnyRole("B") // ROLE_B, ROLE_C 접근 가능
        .requestMatchers("/admin").hasAnyRole("C")   // ROLE_C만 접근 가능
    );
```

하위 권한만 설정해두면, 상위 권한을 가진 사용자도 자동으로 접근 가능해지므로 hasAnyRole("A", "B", "C") 같은 중복 선언을 생략할 수 있다.

---

## 5\. 내부 동작 방식 이해

Spring Security는 내부적으로 RoleHierarchyVoter라는 컴포넌트를 통해 권한 계층을 판단한다. 인증 객체(Authentication)가 가지고 있는 권한 리스트는 계층 정보를 기준으로 **확장되어** 인식된다.

즉, ROLE\_C를 가진 사용자가 인증되었을 경우, 시스템 내부에서는 이미 ROLE\_B와 ROLE\_A를 가진 것처럼 간주되어 인가 처리가 이루어진다.

---

## 6\. 실전 적용 팁

-   **ROLE\_ Prefix 주의**  
    hasRole("A")를 사용하면 내부적으로 ROLE\_A로 비교가 이루어진다. 따라서 역할 명명 시 반드시 ROLE\_ 접두사를 사용할 것.
-   **.setHierarchy() 작성 시 주의사항**  
    문자열이므로 줄바꿈이 중요하다. Java 15 이상에서는 """ """ 텍스트 블록 사용을 권장한다.
-   **권한이 많은 시스템일수록 필수적인 기능**  
    등급별 권한(예: ADMIN > MANAGER > USER)이 명확한 시스템이라면 계층 권한 설정은 매우 유용하다.

---

## 7\. 마무리 요약

| **항목** | **설명** |
| --- | --- |
| **기능 이름** | Role Hierarchy (계층 권한) |
| **목적** | 상위 권한 사용자가 하위 권한 자원까지 접근 가능하게 함 |
| **설정 방법** | RoleHierarchy Bean 등록 및 setHierarchy()로 정의 |
| **내부 작동 방식** | RoleHierarchyVoter로 권한 확장 판단 |
| **장점** | 중복 제거, 설정 간결화, 유지보수 용이성 |