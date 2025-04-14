# WAS와 Spring 컨테이너 통합 초기화 과정 정리

기존에는 서블릿과 필터를 WAS 초기화 과정에서 수동으로 등록하는 구조를 학습했다. 이번에는 거기에 더해 **Spring 컨테이너를 생성하고 등록한 뒤**, 이를 통해 **Spring MVC 컨트롤러를 동작시키는 방법**을 정리해본다.

---

## 📦 1. 의존성 추가

먼저 프로젝트에 Spring 관련 라이브러리를 추가해야 한다. spring-webmvc 모듈은 Spring MVC 뿐 아니라 spring-core, spring-context 등의 핵심 컴포넌트도 함께 포함한다.

```
dependencies {
    // 서블릿
    implementation 'jakarta.servlet:jakarta.servlet-api:6.0.0'

    // 스프링 MVC
    implementation 'org.springframework:spring-webmvc:6.0.4'
}
```

---

## 🧾 2. HelloController 정의

HTTP 요청 /hello-spring에 대한 응답을 처리할 간단한 컨트롤러를 생성한다.

```
package hello.spring;

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

---

## 🛠 3. HelloConfig: 스프링 설정 클래스

Spring 컨테이너에 등록할 HelloController를 Java Config 방식으로 명시한다.  
여기서는 **컴포넌트 스캔 없이 직접 등록**한다.

```
package hello.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HelloConfig {

    @Bean
    public HelloController helloController() {
        return new HelloController();
    }
}
```

---

## 🔧 4. AppInitV2Spring: 스프링 컨테이너 + 디스패처 서블릿 등록

이제 AppInit 인터페이스를 구현한 클래스를 통해, 초기화 시점에 Spring 컨테이너와 DispatcherServlet을 수동으로 등록한다.

```
package hello.container;

import hello.spring.HelloConfig;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;

public class AppInitV2Spring implements AppInit {
    @Override
    public void onStartup(ServletContext servletContext) {
        System.out.println("AppInitV2Spring.onStartup");

        // 스프링 컨테이너 생성
        AnnotationConfigWebApplicationContext appContext = new AnnotationConfigWebApplicationContext();
        appContext.register(HelloConfig.class);

        // 디스패처 서블릿 생성 및 스프링 컨테이너 연결
        DispatcherServlet dispatcher = new DispatcherServlet(appContext);

        // 서블릿 컨테이너에 디스패처 서블릿 등록
        ServletRegistration.Dynamic servlet = servletContext.addServlet("dispatcherV2", dispatcher);

        // URL 매핑 등록
        servlet.addMapping("/spring/*");
    }
}
```

### 설명 요약:

| 단계 | 내용 |
| --- | --- |
| 1 | AnnotationConfigWebApplicationContext 생성 → 스프링 컨테이너 생성 |
| 2 | HelloConfig.class를 스프링 설정으로 등록 |
| 3 | DispatcherServlet에 위 컨테이너 연결 |
| 4 | 해당 디스패처 서블릿을 WAS에 등록 (서블릿 이름은 "dispatcherV2") |
| 5 | URL 패턴 /spring/\* 지정 → /spring으로 시작하는 모든 요청 처리 |

---

## 🔍 실행 결과 확인

서버 실행 후 브라우저에서 다음 주소로 접근해보자:

```
http://localhost:8080/spring/hello-spring
```

출력 결과:

```
hello spring!
```

콘솔 로그:

```
AppInitV2Spring.onStartup
HelloController.hello
```

---

## 📌 동작 흐름 요약

1.  WAS가 구동되며 AppInitV2Spring의 onStartup()이 실행된다.
2.  해당 메서드는 스프링 컨테이너를 생성하고, HelloController를 등록한 설정 클래스를 연결한다.
3.  생성된 컨테이너는 DispatcherServlet에 주입된다.
4.  DispatcherServlet은 /spring/\* 경로에 등록된다.
5.  브라우저 요청 /spring/hello-spring은 DispatcherServlet을 통해 스프링 컨트롤러에 위임된다.
6.  HelloController가 실행되어 "hello spring!" 문자열이 응답으로 출력된다.

---

## 🎯 왜 이렇게 구현하는가?



| 항목 | 설명 |
| --- | --- |
| DispatcherServlet 직접 등록 | 스프링 부트가 자동으로 하는 작업을 수동으로 구성함으로써 내부 구조 이해 |
| 컨트롤러 수동 등록 (@Bean) | 컴포넌트 스캔 없이 명시적으로 빈을 정의하는 방식을 학습 |
| 경로 분리(/spring/\*) | 서블릿 경로와 스프링 경로를 분리해 요청 흐름 제어 가능 |
| AppInit 인터페이스 사용 | 서블릿 컨테이너에 종속되지 않으면서 초기화 구조 분리 가능 |