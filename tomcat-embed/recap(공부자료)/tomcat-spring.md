# 🌿 내장 톰캣에 스프링 컨테이너 연동하기

이제는 톰캣을 단순히 내장 서버로 실행하는 것을 넘어서, 그 위에 **Spring MVC 기반의 웹 애플리케이션을 실행**하는 구조를 직접 구성해봅니다.

---

## 📦 1. 전체 코드 개요

```
package hello.embed;

import hello.spring.HelloConfig;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class EmbedTomcatSpringMain {
    public static void main(String[] args) throws LifecycleException {
        System.out.println("EmbedTomcatSpringMain.main");

        // 1. 톰캣 설정
        Tomcat tomcat = new Tomcat();
        Connector connector = new Connector();
        connector.setPort(8080);
        tomcat.setConnector(connector);

        // 2. 스프링 컨테이너 생성 및 설정 등록
        AnnotationConfigWebApplicationContext appContext = new AnnotationConfigWebApplicationContext();
        appContext.register(HelloConfig.class); // @Configuration 클래스

        // 3. DispatcherServlet 생성 및 컨테이너 연결
        DispatcherServlet dispatcher = new DispatcherServlet(appContext);

        // 4. 디스패처 서블릿을 톰캣에 등록
        Context context = tomcat.addContext("", "/");
        tomcat.addServlet("", "dispatcher", dispatcher);
        context.addServletMappingDecoded("/", "dispatcher");

        // 5. 톰캣 서버 시작
        tomcat.start();
    }
}
```

---

## 🔧 2. 실행 흐름 설명

각 코드 블록이 어떤 역할을 수행하는지 단계별로 정리하면 다음과 같습니다:

| 단계 | 설명 |
| --- | --- |
| ① 톰캣 인스턴스 생성 | Tomcat 객체를 생성하고, 커넥터를 통해 8080 포트로 바인딩 |
| ② Spring 컨테이너 생성 | AnnotationConfigWebApplicationContext를 통해 Java 기반 설정을 로딩 |
| ③ DispatcherServlet 생성 | 생성한 스프링 컨테이너를 DispatcherServlet 생성자에 주입 |
| ④ 서블릿 등록 | 톰캣의 Context에 dispatcher 서블릿으로 등록하고, "/" 경로에 매핑 |
| ⑤ 서버 시작 | tomcat.start() 호출로 톰캣 실행 |

---

## 📍 HelloConfig와 HelloController

이전 단계에서 정의해두었던 Spring 설정 클래스와 컨트롤러는 그대로 활용됩니다.

### HelloConfig.java

```
@Configuration
public class HelloConfig {
    @Bean
    public HelloController helloController() {
        return new HelloController();
    }
}
```

### HelloController.java

```
@RestController
public class HelloController {
    @GetMapping("/hello-spring")
    public String hello() {
        return "hello spring!";
    }
}
```

---

## 💡 실제 실행

### 실행 방법:

-   EmbedTomcatSpringMain.main() 메서드 실행

### 테스트 URL:

```
http://localhost:8080/hello-spring
```

### 출력 결과:

```
hello spring!
```

콘솔 로그에는 EmbedTomcatSpringMain.main → HelloController.hello() 메시지가 순차적으로 출력됩니다.

---

## ✅ 정리 및 비교

이 구조는 **ServletContainerInitializer 방식**과 매우 유사하지만, 차이점은 **main() 메서드가 진입점이라는 점**입니다.



| 방식 | 설명 |
| --- | --- |
| ServletContainerInitializer 기반 | WAS가 클래스 경로 기반으로 서블릿 초기화 |
| EmbedTomcatSpringMain | 개발자가 직접 main()으로 내장 톰캣 및 Spring 컨테이너 실행 |

> 둘 다 같은 톰캣과 DispatcherServlet을 다루지만, **진입점과 설정 흐름**이 다를 뿐 내부 구성 원리는 거의 동일합니다.

---

## 📌 결론

-   이 구조는 Spring Boot가 내부적으로 실행하는 방식과 거의 유사합니다.
-   톰캣을 코드로 직접 다루면서 Spring 컨테이너를 연동하는 원리를 실습해보면, Spring Boot의 @SpringBootApplication, SpringApplication.run()이 어떤 일을 하는지 깊이 이해할 수 있습니다.
-   다만 **실무에서는 이런 구조를 직접 구현할 일은 없으며**, 학습 목적 또는 커스텀 임베디드 서버 구축 시에만 필요합니다.

> 요약하자면, **Spring + Embedded Tomcat**은 Java 진입점에서 모든 설정을 코드로 직접 제어할 수 있는 구조이며, Spring Boot가 이 구조를 얼마나 잘 추상화하고 자동화했는지를 체감할 수 있는 좋은 예제입니다.