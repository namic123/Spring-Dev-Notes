작성 블로그 : https://pjs-world.tistory.com/entry/Spring-Security%EC%9D%98-%EC%9D%B8%EC%A6%9D-%EC%A0%80%EC%9E%A5-%EA%B5%AC%EC%A1%B0-SecurityContextHolder%EC%9D%98-%EB%82%B4%EB%B6%80-%EB%A9%94%EC%BB%A4%EB%8B%88%EC%A6%98

### 📌 목차

[1\. 인증 저장 구조의 개요](#authentication-overview) [2\. Authentication 객체란 무엇인가?](#authentication-object) [3\. SecurityContext의 역할](#security-context) [4\. SecurityContextHolder란 무엇인가?](#security-context-holder) [5\. ThreadLocal을 사용하는 이유](#why-threadlocal) [6\. 실전 사용 예시](#usage-example) [7\. 인증 정보의 생명주기](#lifecycle) [8\. 정리](#summary)

## 1\. 인증 저장 구조의 개요

Spring Security는 인증(Authentication) 관련 정보를 관리하기 위해 일련의 구조적 체계를 갖추고 있다. 클라이언트가 서버로 요청을 보냈을 때, 인증된 사용자인지 여부를 판단하고 해당 정보에 기반하여 인가(Authorization)를 수행하기 위해, 다음과 같은 구조가 활용된다.

[##_Image|kage@Ti1Zg/btsNd66Tblo/y1h13ipWLCh5X8pPeyRhFK/img.png|CDM|1.3|{"originWidth":852,"originHeight":296,"style":"alignCenter","caption":"SecurityContextHolder Authentication 구조"}_##]

이 계층 구조를 통해 인증 정보는 **Authentication 객체로 캡슐화**되며, 해당 객체는 **SecurityContext** 안에 담기고, 이 SecurityContext는 **SecurityContextHolder**를 통해 **전역(static)으로 접근**할 수 있도록 설계되어 있다.

## 2\. Authentication 객체란 무엇인가?

Authentication 객체는 현재 사용자의 인증 정보를 담고 있는 중심 요소이다. 다음과 같은 구성 요소를 포함한다.

-   **Principal**: 사용자 정보 객체 (일반적으로 UserDetails 구현체)
-   **Credentials**: 인증 수단 (예: 비밀번호, JWT 토큰 등)
-   **Authorities**: 사용자의 권한 목록 (ROLE\_USER, ROLE\_ADMIN 등

```
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
auth.getAuthorities(); // 권한 목록
auth.getPrincipal();   // 사용자 정보
```

로그인 성공 시 이 객체가 생성되어 전역적으로 유지되며, 이후의 인가 로직에 사용된다.

## 3\. SecurityContext의 역할

**SecurityContext**는 하나의 사용자 요청 단위에서 인증 정보를 저장하는 컨테이너 역할을 수행한다. 내부적으로는 **단 하나의 Authentication 객체**만을 가지며, 로그인 성공 이후에는 해당 객체가 이 안에 저장된다.

모든 인증 필터 및 인가 로직은 SecurityContext를 기준으로 작동하며, 요청이 끝날 때까지 보안 상태를 유지한다.

## 4\. SecurityContextHolder란 무엇인가?

SecurityContextHolder는 SecurityContext를 static 방식으로 전역 관리하는 헬퍼 클래스이다. 어디에서든 호출이 가능하며, 인증 정보에 접근할 수 있도록 한다.

```
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
```

컨트롤러, 서비스, 필터 등 다양한 계층에서 인증 상태를 확인해야 할 때 매우 유용하며, 기본적으로 ThreadLocal 기반으로 동작한다.

## 5\. ThreadLocal을 사용하는 이유

기본적으로 WAS(예: 톰캣)는 멀티 쓰레드 환경에서 요청을 처리한다. 만일 static 변수를 이용하여 인증 정보를 저장하게 되면, **사용자 간의 인증 정보가 덮어쓰여지는 심각한 보안 문제가 발생할 수 있다**.

이를 방지하기 위해 S**pring Security는 쓰레드마다 독립적인 저장소인 ThreadLocal을 사용**한다.

```
final class ThreadLocalSecurityContextHolderStrategy implements SecurityContextHolderStrategy {
    private static final ThreadLocal<Supplier<SecurityContext>> contextHolder = new ThreadLocal<>();
}
```

[##_Image|kage@xVKek/btsNdzhrwXC/PPphEKu02DnshjJgkdpdA0/img.png|CDM|1.3|{"originWidth":831,"originHeight":234,"style":"alignCenter","caption":"ThreadLocal 구조"}_##]

| **도식 요소** | **설명** |
| --- | --- |
| **Request** | 사용자로부터 들어온 요청 |
| **Class Field** | SecurityContextHolder 내부의 static 필드 (contextHolder) |
| **Thread Local** | 각 요청에 할당된 **Thread별 저장소 공간** |
| **Thread** | 톰캣 등의 WAS가 사용자 요청에 대해 생성하는 쓰레드 |
| **Authentication** | 쓰레드마다 독립적으로 저장된 인증 객체 |

이 구조 덕분에 여러 사용자가 동시에 요청을 보내더라도 인증 정보가 서로 간섭하지 않으며, 각 사용자에 대한 인증 상태가 안전하게 분리된다.

## 6\. 실전 사용 예시

**로그인 성공 시**: 인증 객체를 생성하여 SecurityContextHolder에 등록

```
SecurityContextHolder.getContext().setAuthentication(authentication);
```

**로그아웃 시**: 인증 정보를 초기화하여 로그아웃 처리

```
SecurityContextHolder.clearContext();
```

이처럼 인증 상태의 설정 및 해제는 SecurityContextHolder를 통해 통제되며, Spring Security 전반의 필터 및 처리 로직에서 해당 객체를 참조하게 된다.

## 7\. 인증 정보의 생명주기

**Spring Security의 인증 정보는 다음과 같은 생명주기를 갖는다.**

1.  사용자가 요청을 보낸다.
2.  필터 체인에서 UsernamePasswordAuthenticationFilter 등이 요청을 가로채 인증을 수행한다.
3.  인증이 성공하면 Authentication 객체가 생성된다.
4.  해당 객체는 SecurityContext에 저장되고, SecurityContextHolder를 통해 관리된다.
5.  요청이 종료되면 clearContext()를 통해 인증 정보가 삭제된다.

이러한 흐름은 Spring Security의 핵심 동작 구조 중 하나로, 요청 기반 인증 보안 모델을 견고하게 유지할 수 있도록 한다.

## 8\. 정리

| **구성 요소** | **설명** |
| --- | --- |
| **Authentication** | 인증 정보 객체. 사용자 정보, 권한, 인증 수단 포함 |
| **SecurityContext** | 인증 정보를 담는 요청 단위 컨테이너 |
| **SecurityContextHolder** | 인증 정보를 전역으로 보관 및 접근하는 클래스 |
| **저장 전략** | 기본적으로 ThreadLocal 기반으로 쓰레드마다 분리 |
| **실전 활용** | 로그인 시 인증 등록, 로그아웃 시 인증 초기화 등 |