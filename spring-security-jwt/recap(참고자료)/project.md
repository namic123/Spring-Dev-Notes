# Spring Security와 JWT 기반 학습용 프로젝트 구성

본 프로젝트는 Spring Security의 구조와 기능을 학습하기 위한 목적으로 구성되었으며, 인증 수단으로 JWT(Json Web Token)를 도입하여 세션 기반 인증 방식이 아닌 토큰 기반 인증 방식을 실습할 수 있도록 설계되었다. 이에 따라 인증 및 인가 처리 과정, 사용자 권한 설정, JWT 토큰 발급 및 검증, 그리고 Spring Security의 필터 체인 구성 등에 대해 실제 코드 기반으로 학습할 수 있다.

---

## 1\. 프로젝트 생성 및 의존성 설정

Spring Initializr([https://start.spring.io)을](https://start.spring.io\)을) 이용하여 프로젝트를 생성하며, 다음과 같은 의존성이 반드시 포함되어야 한다.

### 필수 의존성 목록

-   Spring Web
-   Spring Security
-   Spring Data JPA
-   Lombok
-   H2 Database

```
dependencies {
	implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
	implementation 'org.springframework.boot:spring-boot-starter-security'
	implementation 'org.springframework.boot:spring-boot-starter-web'
	compileOnly 'org.projectlombok:lombok'
	runtimeOnly 'com.h2database:h2'
	annotationProcessor 'org.projectlombok:lombok'
	testImplementation 'org.springframework.boot:spring-boot-starter-test'
	testImplementation 'org.springframework.security:spring-security-test'
	testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}
```

데이터베이스 연결이 완료되지 않은 상태에서 DB (예: H2 Database) 관련 의존성이 포함되어 있으면, 애플리케이션 실행 시 런타임 오류가 발생할 수 있으므로, 초기 개발 단계에서는 해당 의존성을 주석 처리하거나, application.properties에서 DB 관련 설정을 미리 주입하지 않도록 유의해야 한다.

## 2\. JWT 관련 의존성 구성

JWT 토큰을 생성하고 검증하는 기능을 사용하기 위해서는 io.jsonwebtoken 라이브러리를 추가로 설정해야 한다. 현재 가장 많이 사용되는 버전은 0.11.5이나, 최신 안정 버전은 0.12.3이다. 본 프로젝트에서는 최신 버전(0.12.3)을 기준으로 개발하며, 버전 차이에 따른 주요 메서드 변경사항도 병행 학습할 수 있도록 구성하였다.

### JWT 의존성 (버전 0.12.3 기준)

```
implementation 'io.jsonwebtoken:jjwt-api:0.12.3'
implementation 'io.jsonwebtoken:jjwt-impl:0.12.3'
implementation 'io.jsonwebtoken:jjwt-jackson:0.12.3'
```

> jackson 모듈은 JWT의 payload 파싱 시 ObjectMapper를 활용하기 위한 필수 설정이다.

### JWT 의존성 (버전 0.11.5 기준)

```
implementation 'io.jsonwebtoken:jjwt-api:0.11.5'
implementation 'io.jsonwebtoken:jjwt-impl:0.11.5'
implementation 'io.jsonwebtoken:jjwt-jackson:0.11.5'
```

버전에 따라 JwtParser, JwtBuilder의 사용 방식이 다르므로, 학습 시 각 버전 간 비교를 병행하여 습득하는 것을 권장한다.

## 3\. 기본 컨트롤러 구성

초기 테스트 및 라우팅 확인을 위한 컨트롤러를 다음과 같이 구성한다.

### MainController

```
@Controller
@ResponseBody
public class MainController {
    @GetMapping("/")
    public String mainP() {
        return "main Controller";
    }
}
```

루트 경로(/)로 요청이 들어왔을 때, 단순히 문자열 "main Controller"를 응답하는 구조이다.

### AdminController

```
@Controller
@ResponseBody
public class AdminController {
    @GetMapping("/admin")
    public String adminP() {
        return "admin Controller";
    }
}
```