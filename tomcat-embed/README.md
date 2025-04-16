### 본 학습용 프로젝트는 김영한님의 <스프링부트 - 핵심 원리와 활용> 강의를 기반으로 작성되었습니다.

# Embedded Tomcat 기반 Spring Web Application

이 프로젝트는 Spring Boot 도입 이전 단계에서 **내장 톰캣(Embedded Tomcat)** 을 직접 설정하여 사용하는 전통적인 방식의 스프링 웹 애플리케이션 구성 예제입니다.

---

## 📌 개요

-   WAR 기반 외장 톰캣 배포의 불편함을 개선하기 위해 **톰캣을 자바 라이브러리로 포함하여 직접 실행**
-   main() 메서드 실행만으로 **톰캣 서버 + 스프링 컨테이너까지 직접 구동 가능**
-   Gradle로 **Fat Jar 생성**하여 외부 환경에 톰캣 설치 없이 바로 실행 가능

---

## 📁 디렉토리 구조

```
src/main/
├── java/
│   └── hello/
│       ├── embed/                # 내장 톰캣 실행 진입점 (main 메서드)
│       ├── servlet/              # HelloServlet 등 직접 등록 서블릿
│       ├── spring/               # 스프링 설정 및 컨트롤러
│       └── boot/                 # 커스텀 부트 클래스, 애노테이션 정의
└── resources/
```

---

## ⚙ Gradle 설정 예시

```
plugins {
    id 'java'
}

group = 'hello'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '17'

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework:spring-webmvc:6.0.4'
    implementation 'org.apache.tomcat.embed:tomcat-embed-core:10.1.5'
}

tasks.named('test') {
    useJUnitPlatform()
}

task buildJar(type: Jar) {
    manifest {
        attributes 'Main-Class': 'hello.embed.EmbedTomcatSpringMain'
    }
    with jar
}

task buildFatJar(type: Jar) {
    manifest {
        attributes 'Main-Class': 'hello.embed.EmbedTomcatSpringMain'
    }
    duplicatesStrategy = DuplicatesStrategy.WARN
    from { configurations.runtimeClasspath.collect { it.isDirectory() ? it : zipTree(it) } }
    with jar
}
```

---

## 🚀 실행 방식

### 1\. 내장 톰캣 + 서블릿 직접 등록

EmbedTomcatServletMain.java 실행

```
Tomcat tomcat = new Tomcat();
tomcat.setPort(8080);
tomcat.addServlet("", "helloServlet", new HelloServlet());
context.addServletMappingDecoded("/hello-servlet", "helloServlet");
tomcat.start();
```

-   실행: http://localhost:8080/hello-servlet

### 2\. 내장 톰캣 + 스프링 컨테이너 통합

EmbedTomcatSpringMain.java 실행

```
AnnotationConfigWebApplicationContext appContext = new AnnotationConfigWebApplicationContext();
appContext.register(HelloConfig.class);
DispatcherServlet dispatcher = new DispatcherServlet(appContext);
context.addServletMappingDecoded("/", "dispatcher");
```

-   실행: http://localhost:8080/hello-spring

---

## 💡 Fat Jar 빌드 및 실행

### Fat Jar 생성

```
./gradlew clean buildFatJar
```

-   결과 위치: build/libs/embed-0.0.1-SNAPSHOT.jar

### Fat Jar 실행

```
java -jar build/libs/embed-0.0.1-SNAPSHOT.jar
```

-   톰캣 및 스프링이 포함된 단일 JAR 실행 가능

---

## ✅ MySpringApplication 도입

MySpringApplication 클래스는 다음 기능을 한 번에 처리합니다:

-   내장 톰캣 구동
-   스프링 컨테이너 설정 및 DispatcherServlet 등록

```
MySpringApplication.run(MySpringBootMain.class, args);
```

@MySpringBootApplication 애노테이션은 컴포넌트 스캔을 자동화합니다:

```
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ComponentScan
public @interface MySpringBootApplication {}
```

실행 진입점은 MySpringBootMain 클래스입니다:

```
@MySpringBootApplication
public class MySpringBootMain {
    public static void main(String[] args) {
        MySpringApplication.run(MySpringBootMain.class, args);
    }
}
```

---

## 📦 JAR vs WAR 차이 요약

항목WAR 배포 방식Fat Jar 방식

| WAS 필요 | 필요 (외부 설치) | 불필요 (내장 포함) |
| --- | --- | --- |
| 배포 방식 | WAR 파일 → Tomcat | JAR 파일 단독 실행 |
| 개발 편의성 | 설정 복잡 | IDE에서 main() 실행만으로 가능 |
| 유연성 | Tomcat 버전 수동 관리 | Gradle로 버전 관리 가능 |

---

## 📚 학습 포인트 요약

| 항목 | 설명 |
| --- | --- |
| 내장 톰캣 설정 | Tomcat 인스턴스 생성 및 포트 설정 |
| 서블릿 수동 등록 | Context, addServlet(), addMapping 활용 |
| Spring 통합 | AnnotationConfigWebApplicationContext 사용 |
| DispatcherServlet 직접 등록 | 스프링 MVC 직접 설정 |
| Fat Jar 생성 | 모든 클래스 포함한 실행 JAR 구성 |
| 커스텀 부트 클래스 작성 | MySpringApplication + 애노테이션 기반 설정 |

---

## 📝 결론

이 프로젝트는 전통적인 WAR 방식에서 벗어나, **내장 톰캣을 자바 코드로 직접 실행하는 구조**를 실습하며 **Spring Boot 이전 방식의 자동화 원리를 이해**하는 데 초점을 맞췄습니다.

main() 메서드 하나로 WAS와 Spring 컨테이너를 동시에 실행하고 배포까지 JAR 파일로 해결하는 경험을 통해, 스프링 부트가 왜 등장했는지, 무엇을 자동화하고 간편하게 해주는지를 실감할 수 있습니다.