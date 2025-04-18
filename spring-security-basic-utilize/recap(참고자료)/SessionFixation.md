## 세션 고정 공격(Session Fixation)과 Spring Security의 대응 전략

블로그 : https://pjs-world.tistory.com/entry/%EC%84%B8%EC%85%98-%EA%B3%A0%EC%A0%95-%EA%B3%B5%EA%B2%A9Session-Fixation%EA%B3%BC-Spring-Security%EC%9D%98-%EB%8C%80%EC%9D%91-%EC%A0%84%EB%9E%B5

## 1\. 세션 고정 공격이란?

**세션 고정(Session Fixation) 공격**이란, 공격자가 먼저 합법적인 세션 ID를 획득한 뒤, 해당 세션 ID를 사용자에게 할당하고 로그인 과정을 유도하여 인증 이후에도 동일한 세션 ID를 통해 권한을 탈취하는 방식의 공격이다. 이와 같은 방식은 서버가 기존 세션 ID를 유지한 채 사용자의 로그인 상태를 반영할 경우에 발생할 수 있는 보안 취약점이다.

---

## 2\. 공격 흐름 설명

공격 시나리오는 다음과 같이 구성된다.

**세션 고정 공격 흐름**

**1\. 해커가 서버로 접근하여 세션 ID 발급**

-   해커는 서버에 요청을 보내고, 응답으로 **세션 쿠키(JSESSIONID 등)** 를 발급받음

```
Set-Cookie: JSESSIONID=abc123
```

**2\. 해커가 사용자에게 세션 ID 전달**

-   다양한 방식으로 사용자의 브라우저에 JSESSIONID=abc123 쿠키를 **고정**시킴
    -   URL 파라미터로 세션 전달
    -   스크립트 공격 (XSS)
    -   이메일 링크에 쿠키 세팅 등

**3\. 사용자가 서버에 로그인**

-   사용자는 해커가 미리 설정한 abc123 세션 ID로 서버에 로그인함
-   서버는 기존 세션(abc123)에 사용자의 인증 정보를 저장함
    -   이 순간, 세션 ID abc123은 인증된 세션이 됨

**4\. 해커가 같은 세션 ID로 접근**

-   해커는 여전히 JSESSIONID=abc123을 알고 있으므로,  
    해당 세션 ID로 서버에 요청 → 이미 로그인된 사용자로 **권한 획득 성공**

아래는 위 흐름을 표현한 이미지이다.

[##_Image|kage@Oo2y6/btsNrmITlI7/y0haE2YIMv7f1NbKn3eFj0/img.png|CDM|1.3|{"originWidth":636,"originHeight":426,"style":"alignCenter","caption":"세션 고정 공격 흐름"}_##]

**핵심 포인트**

| 단계 | 공격 요소 | 설명 |
| --- | --- | --- |
| 1 | **미리 세션 확보** | 해커가 세션을 먼저 확보 |
| 2 | **세션 전달** | 피해자에게 해당 세션을 사용하도록 유도 |
| 3 | **사용자 로그인** | 서버는 기존 세션에 사용자 인증 정보 저장 |
| 4 | **세션 재사용** | 해커가 동일한 세션 ID로 서버에 접근하여 사용자 권한 획득 |

이로 인해 공격자는 로그인하지 않았음에도 사용자의 권한으로 행위할 수 있게 된다.

---

## 3\. Spring Security의 기본 방어 메커니즘

Spring Security는 세션 고정 공격을 방지하기 위한 기본적인 보안 기능을 내장하고 있으며, 로그인 성공 시 기존 세션 ID를 변경하도록 설정되어 있다. 이를 통해 세션 ID를 고정하여 공격하는 방식 자체가 무력화된다.

```
http
  .sessionManagement((auth) -> auth
    .sessionFixation().changeSessionId());
```

이 설정은 세션 객체는 유지하되, 세션의 식별자인 ID만을 새로 생성하여 보안성을 강화하는 방식으로 동작한다.

Spring Security에서 제공하는 .sessionFixation() 옵션의 종류는 아래와 같다.

| **설정 방식** | **설명** |
| --- | --- |
| **.none()** | 기존 세션 ID를 그대로 유지함. 보안상 매우 위험한 설정. |
| **.newSession()** | 기존 세션을 폐기하고 새로운 세션을 생성함. 다만, 이전 세션의 상태를 유지하지 않음. |
| **.changeSessionId()** | 세션은 유지하되 ID만 변경함. 기본 설정값이며 가장 추천되는 방식. |

---

## 4\. 세션 제한 및 만료 시간 설정

추가적으로 세션 관리에 대한 보안성을 강화하기 위해 아래와 같은 설정도 병행할 수 있다.

**최대 세션 개수 제한**

```
http.sessionManagement((auth) -> auth
    .maximumSessions(1)
    .maxSessionsPreventsLogin(true));
```

-   maximumSessions(1): 사용자당 하나의 세션만 허용함.
-   maxSessionsPreventsLogin(true): 기존 세션 유지, 새 로그인 차단.
-   false로 설정 시 기존 세션은 무효화되고 새 로그인만 유지됨.

**세션 타임아웃 설정**

application.properties 또는 application.yml에서 설정 가능하다.

**properties**

```
server.servlet.session.timeout=1800  # 초 단위 설정
server.servlet.session.timeout=90m   # 분 단위 설정
```

**yml**

```
  servlet:
    session:
#      timeout: 1800  // 초 기반
       timeout: 90m   #  // 분 기반
```

---

## 5\. 요약

| **항목** | **설명** |
| --- | --- |
| **공격 명칭** | 세션 고정(Session Fixation) |
| **주요 위협** | 공격자가 사전에 획득한 세션 ID를 이용해 로그인 권한 탈취 |
| **기본 대응** | sessionFixation().changeSessionId() 설정으로 세션 ID 재발급 |
| **부가 대응** | 세션 최대 개수 제한, 세션 타임아웃 설정 |
| **실무 팁** | JWT 기반 인증 시스템은 세션 대신 토큰으로 인증을 수행하므로 해당 공격으로부터 자유로움. 단, 쿠키 기반 JWT 사용 시 유사 공격 가능성 있음 |