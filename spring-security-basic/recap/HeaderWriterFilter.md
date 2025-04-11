## Spring Security- HeaderWriterFilter, 응답 헤더를 자동으로 관리하는 보안 필터 (SecurityFilterChain 4번째 필터)
블로그 : 

### 📌 목차

[1\. HeaderWriterFilter란 무엇인가?](#what-is-headerwriterfilter) [2\. 기본 동작 목적 및 흐름](#how-headerwriterfilter-works) [3\. 기본적으로 추가되는 보안 헤더](#default-http-headers) [4\. 설정 커스터마이징 및 비활성화 방법](#customization) [5\. 실무 적용 시 유의사항](#practical-tips)

## 1\. HeaderWriterFilter란 무엇인가?

HeaderWriterFilter는 Spring Security에서 제공하는 기본 보안 필터 중 하나로, **HTTP 응답에 다양한 보안 관련 헤더를 자동으로 삽입하는 역할**을 수행한다. 이러한 보안 헤더는 브라우저에서 잠재적인 공격을 방지하기 위해 필수적인 요소로 간주되며, 별도의 설정 없이도 기본으로 활성화되어 있다.

이 필터는 OncePerRequestFilter를 상속하여 **HTTP 요청당 한 번만 실행되도록 보장**되며, 기본 필터 체인에서는, **네 번째 위치**에서 동작한다

[##_Image|kage@b4w6lw/btsNi65QZoQ/bwgSn32LD0BtC0xVSJKO10/img.png|CDM|1.3|{"originWidth":1209,"originHeight":470,"style":"alignCenter","caption":"SecurityFilterChain 4번째 필터"}_##]

## 2\. 기본 동작 목적 및 흐름

Spring Security는 사용자의 요청을 처리하면서, 응답을 브라우저로 전송하기 직전에 보안 헤더를 추가한다. 이는 다음과 같은 공격을 방지하기 위한 조치이다:

-   **Clickjacking 방지**: X-Frame-Options
-   **MIME 타입 스니핑 방지**: X-Content-Type-Options
-   **브라우저 XSS 필터 대응**: X-XSS-Protection
-   **캐시 제어**: Cache-Control, Pragma, Expires

**필터 실행 시점**

HeaderWriterFilter는 두 시점 중 하나에서 응답 헤더를 삽입할 수 있다.

| **시점** | **설명** |
| --- | --- |
| **필터 체인을 통과할 때** | 즉시 헤더를 응답에 삽입 |
| **서블릿 응답 반환 직전 (기본값)** | 응답이 최종적으로 구성될 때 보안 헤더를 삽입 |

후자의 방식이 기본이며, 이는 응답 헤더가 이후의 다른 필터 또는 서블릿에서 **덮어씌워지는 것을 방지**하기 위함이다.

## 3\. 기본적으로 추가되는 보안 헤더

HeaderWriterFilter는 아래와 같은 기본 헤더를 응답에 추가하여 보안을 강화한다.

| **헤더** | **설명** |
| --- | --- |
| **X-Content-Type-Options: nosniff** | MIME 타입 스니핑 방지 |
| **X-XSS-Protection: 0** | 브라우저 기본 XSS 필터 비활성화 |
| **Cache-Control: no-cache, no-store, max-age=0, must-revalidate** | 캐시 방지 |
| **Pragma: no-cache** | HTTP/1.0 호환 캐시 방지 |
| **Expires: 0** | 이미 만료된 응답임을 명시 |
| **X-Frame-Options: DENY** | iframe 통한 삽입 차단 (Clickjacking 방지) |

이 설정들은 기본적으로 활성화되어 있으며, 추가적인 설정 없이도 보안 효과를 얻을 수 있다.

## 4\. 설정 커스터마이징 및 비활성화 방법

**전체 비활성화**

```
http
  .headers(headers -> headers.disable());
```

**비활성화 테스트 결과**

[##_Image|kage@cElbZ3/btsNi4GYtEL/0xaYgmpJX301hkc7kXucy1/img.png|CDM|1.3|{"originWidth":1674,"originHeight":641,"style":"alignCenter"}_##]

**비활성화 안했을때**

[##_Image|kage@bDFSvq/btsNizfUfUI/h7DKedrVEUnXi7BkrB6ZV0/img.png|CDM|1.3|{"originWidth":1690,"originHeight":1128,"style":"alignCenter"}_##]

**특정 헤더만 조정**

```
http
  .headers(headers -> headers
      .frameOptions(frame -> frame.sameOrigin())
      .cacheControl(cache -> cache.disable())
      .contentTypeOptions(opt -> opt.disable()));
```

위와 같이 설정하면 iframe만 same-origin 허용하고, 캐시나 MIME 스니핑 관련 헤더는 비활성화할 수 있다.

## 5\. 실무 적용 시 유의사항

HeaderWriterFilter는 강력한 기본 보안 장치지만, 실무에서는 다음과 같은 상황에서 의도치 않은 문제가 발생할 수 있다.

| **상황** | **주의사항** |
| --- | --- |
| **iframe 기반 UI 통합** | X-Frame-Options: DENY로 인해 화면이 차단될 수 있음 |
| **캐싱 테스트 중 캐시가 되지 않음** | Cache-Control, Pragma 헤더로 인한 가능성 있음 |
| **MIME 타입 오류 발생** | nosniff 정책이 엄격하게 작동하고 있을 수 있음 |

이러한 경우에는 헤더를 일부 조정하거나 비활성화하여 테스트하거나 정책을 명확히 반영할 필요가 있다.