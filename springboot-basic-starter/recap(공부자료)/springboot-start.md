# Spring Boot와 내장 웹 서버 - 프로젝트 구성 및 실행

이 문서는 내장 톰캣 기반 수동 설정 방식에서 발전하여 **Spring Boot**가 어떻게 그 문제를 해결하고 웹 서버 설정을 자동화하는지 소개하는 실습 예제입니다.

---

## 🧩 스프링 부트 도입 배경

기존의 내장 톰캣 수동 설정 방식은 다음과 같은 단점을 갖고 있었습니다:

-   톰캣 수동 설정, DispatcherServlet 수동 등록 등 번거로운 코드 작성 필요
-   Fat Jar 직접 구성 필요 (중복 클래스 관리, main-class 지정 등)
-   라이브러리 버전 수동 관리

**Spring Boot는 위 문제를 자동화하여 다음과 같은 이점을 제공합니다:**

-   **내장 톰캣 자동 포함** (spring-boot-starter-web 사용 시)
-   **빌드 시 자동 Fat Jar 구성**
-   **main()에서 한 줄로 서버 실행**
-   **라이브러리 의존성 버전 자동 관리**

---

## 🚀 프로젝트 생성

### 1\. 생성 방법

#### ▶ IntelliJ에서 직접 Gradle 프로젝트 임포트

-   기존 boot-start → boot 로 디렉토리명 변경
-   build.gradle 열기 → "Open as Project" 선택

#### ▶ 또는 Spring Initializr 사용

-   사이트: [https://start.spring.io](https://start.spring.io)
-   설정 예시:
    -   **Project**: Gradle
    -   **Language**: Java
    -   **Spring Boot**: 3.0.x
    -   **Group**: hello
    -   **Artifact**: boot
    -   **Package**: hello.boot
    -   **Packaging**: Jar
    -   **Java**: 17
    -   **Dependencies**: Spring Web

---

## ⚙ Gradle 설정 예시

```
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.0.2'
    id 'io.spring.dependency-management' version '1.1.0'
}

group = 'hello'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '17'

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

tasks.named('test') {
    useJUnitPlatform()
}
```

> ✅ 참고: 스프링 부트를 사용하면 의존성에 버전 정보가 명시되지 않아도 됩니다. 버전 관리 자동화 덕분입니다.

---

## 💻 동작 확인

### 기본 메인 클래스 실행

```
@SpringBootApplication
public class BootApplication {
    public static void main(String[] args) {
        SpringApplication.run(BootApplication.class, args);
    }
}
```

-   실행: BootApplication.main()
-   접속: [http://localhost:8080](http://localhost:8080)
-   결과: Whitelabel Error Page가 보이면 정상 작동

### HelloController 등록 후 확인

```
package hello.boot.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @GetMapping("/hello-spring")
    public String hello() {
        System.out.println("HelloController.hello");
        return "hello spring!";
    }
}
```

-   접속: [http://localhost:8080/hello-spring](http://localhost:8080/hello-spring)
-   결과: hello spring! 출력

---

## 🧱 내장 톰캣 자동 포함 확인

Spring Boot는 spring-boot-starter-web 내부에서 다음 라이브러리를 자동 포함합니다:

-   tomcat-embed-core
-   spring-webmvc
-   기타 웹 서버 실행에 필요한 모든 구성 요소

```
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
}
```

---

## ✅ 핵심 요약

| 항목 | Springboot 이전 | Springboot 사용 |
| --- | --- | --- |
| 톰캣 설정 | 수동 (Tomcat 객체 생성) | 자동 (starter-web 사용 시 자동 포함) |
| DispatcherServlet 등록 | 수동 | 자동 구성 |
| Fat Jar 구성 | buildFatJar 수동 작업 필요 | 자동 생성됨 (bootJar) |
| 실행 | main() + Tomcat.start() | SpringApplication.run() 한 줄 |
| 버전 관리 | 각 라이브러리 수동 지정 | 의존성 자동 관리 |