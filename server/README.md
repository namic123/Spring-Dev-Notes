## 본 학습용 프로젝트는 김영한님의 <스프링부트 - 핵심 원리와 활용> 강의를 기반으로 작성되었습니다.

# Legacy Spring Web Application (Pre-Spring Boot)

이 프로젝트는 Spring Boot 도입 이전의 **전통적인 서블릿 기반 Spring 웹 애플리케이션 구조**를 다루며, WAR 파일을 생성하여 톰캣(WAS)에 배포하는 방식으로 구성되어 있습니다.

---

## 🛠 프로젝트 개요

-   **JDK 17 기반의 WAR 프로젝트 구성**
-   **Servlet API 기반 순수 서블릿 등록 방식**
-   **ServletContainerInitializer를 이용한 컨테이너 초기화**
-   **Spring MVC 통합 및 DispatcherServlet 직접 등록**
-   **Spring Web MVC 없이 순수 HTML/Servlet 방식부터 단계적 확장**

---

## 📁 디렉토리 구조

```
src/main/
├── java/
│   └── hello/
│       ├── servlet/              # TestServlet, HelloServlet 등 서블릿 클래스
│       └── container/            # 초기화 관련 클래스 (ServletContainerInitializer 등)
│       └── spring/               # 스프링 관련 구성 및 컨트롤러
├── resources/
│   └── META-INF/services/       # 초기화 클래스 등록 설정
└── webapp/
    └── index.html               # 정적 리소스 (HTML)
```

---

## 📦 build.gradle 구성

```
plugins {
    id 'java'
    id 'war'
}

group = 'hello'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '17'

repositories {
    mavenCentral()
}

dependencies {
    implementation 'jakarta.servlet:jakarta.servlet-api:6.0.0'
    implementation 'org.springframework:spring-webmvc:6.0.4'
}

tasks.named('test') {
    useJUnitPlatform()
}

task explodedWar(type: Copy) {
    into "$buildDir/exploded"
    with war
}
```

---

## 🌐 주요 기능별 설명

### 1\. 정적 HTML 페이지 제공

-   /src/main/webapp/index.html 경로에 HTML 파일을 두면 정적 리소스로 제공됩니다.
-   톰캣 배포 후 http://localhost:8080/index.html 로 확인 가능

### 2\. 기본 서블릿 등록 방식

-   @WebServlet 어노테이션을 통한 정적 서블릿 등록: /test
-   직접 코드에서 ServletContext를 통해 등록하는 프로그래밍 방식도 예제에 포함

### 3\. ServletContainerInitializer 기반 초기화

-   MyContainerInitV1, MyContainerInitV2 를 통해 WAS 실행 시 동작
-   /META-INF/services/jakarta.servlet.ServletContainerInitializer 에 초기화 클래스 경로 등록 필수

### 4\. Spring 컨테이너와 통합

-   AppInitV2Spring: DispatcherServlet 직접 등록, 경로: /spring/\*
-   AppInitV3SpringMvc: WebApplicationInitializer 사용, 경로: /\*
-   HelloController: @RestController + @GetMapping 으로 /hello-spring 응답 처리

---

## ⚙ WAR 파일 생성 및 배포

### WAR 생성

```
./gradlew build
```

생성된 WAR 파일 위치:

```
build/libs/server-0.0.1-SNAPSHOT.war
```

### WAR 압축 풀기

```
cd build/libs
jar -xvf server-0.0.1-SNAPSHOT.war
```

### WAR 배포 (Tomcat)

1.  톰캣 /webapps 디렉토리 내 기존 파일 삭제
2.  WAR 파일을 ROOT.war로 이름 변경 후 배포

```
cp server-0.0.1-SNAPSHOT.war $TOMCAT_HOME/webapps/ROOT.war
```

1.  톰캣 실행 후 접속 확인:

```
http://localhost:8080/index.html
http://localhost:8080/test
http://localhost:8080/spring/hello-spring
http://localhost:8080/hello-spring
```

---

## 🧩 실행 Servlet 정리

URL Path등록 방식클래스명

| /test | @WebServlet | TestServlet |
| --- | --- | --- |
| /hello-servlet | 프로그래밍 등록 | AppInitV1Servlet |
| /spring/hello-spring | DispatcherServlet (V2) | AppInitV2Spring |
| /hello-spring | DispatcherServlet (V3) | AppInitV3SpringMvc |

---

## 📝 참고 사항

-   실행 중 8080 포트 사용 중 에러 발생 시 기존 톰캣 프로세스를 종료하거나 컴퓨터를 재부팅해야 할 수 있음
-   IntelliJ 무료 버전은 Smart Tomcat 플러그인을 이용하거나 exploded WAR 디렉토리를 설정해야 함
-   스프링 컨테이너는 AnnotationConfigWebApplicationContext 사용

---

## 📚 학습 포인트 요약

| 학습 항목 | 설명 |
| --- | --- |
| WAR 구조 이해 | classes, lib, webapp, WEB-INF 구조 |
| Servlet 등록 방식 비교 | 어노테이션 기반, 프로그래밍 등록 방식 |
| ServletContainerInitializer 사용법 | 서블릿 컨테이너 초기화 원리 이해 |
| Spring 컨테이너와의 통합 | DispatcherServlet 등록 및 Spring Bean 처리 |
| WebApplicationInitializer 이해 | 스프링이 제공하는 초기화 인터페이스 활용 |

---

## ✅ 결론

이 프로젝트는 **Spring Boot 이전의 전통적인 Java EE 기반의 서블릿 초기화 흐름과 Spring MVC 통합 방법**을 학습하기 위한 실습 프로젝트입니다. WAR 파일 구조 및 배포 방식, DispatcherServlet 등록 원리, 서블릿 초기화 과정 전반을 학습하는 데 적합합니다.

이후에는 **Spring Boot 기반의 내장 톰캣 방식으로 전환하며 자동 구성과 더 편리한 개발 환경**으로 넘어가는 흐름을 자연스럽게 이해하게 됩니다.
