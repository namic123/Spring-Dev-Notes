# Spring MVC의 서블릿 컨테이너 초기화 지원 구조

Spring MVC는 서블릿 컨테이너 초기화를 수동으로 구성해야 했던 기존의 번거로운 과정을 **자동화**하고 **단순화**할 수 있는 구조를 미리 제공하고 있습니다. 이제 우리는 WAS에 배포만 하면, WebApplicationInitializer만 구현하는 것으로 **스프링 컨테이너 등록 + DispatcherServlet 등록**을 한 번에 처리할 수 있습니다.

---

## 1️⃣ 기존의 서블릿 초기화 방식은 왜 번거로운가?

지금까지 Spring 없이 WAS에 직접 애플리케이션을 초기화하기 위해선 다음과 같은 절차를 따랐습니다:

-   ServletContainerInitializer 인터페이스 구현
-   @HandlesTypes 애노테이션 사용
-   /META-INF/services/jakarta.servlet.ServletContainerInitializer 등록 파일 생성
-   애플리케이션 초기화 인터페이스 (ex. AppInit) 구현체 수동 탐색 및 실행

이 과정은 **반복적이고 오류 가능성이 높으며**, 서블릿 컨테이너의 내부 구조에 대한 깊은 이해를 요구합니다.

---

## 2️⃣ Spring MVC는 이 과정을 어떻게 단순화했는가?

Spring MVC는 개발자가 오직 아래 인터페이스만 구현하면 초기화 과정을 자동으로 처리하도록 구조를 만들어두었습니다:

```
public interface WebApplicationInitializer {
    void onStartup(ServletContext servletContext) throws ServletException;
}
```

이 인터페이스를 구현하면 **서블릿 컨테이너가 자동으로 이를 실행**하며, 그 내부에서 스프링 컨테이너와 DispatcherServlet을 구성할 수 있습니다.

---

## 3️⃣ 예제: AppInitV3SpringMvc

다음은 위 인터페이스를 구현하여 DispatcherServlet을 등록하는 예제입니다:

```
package hello.container;

import hello.spring.HelloConfig;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;

public class AppInitV3SpringMvc implements WebApplicationInitializer {
    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        System.out.println("AppInitV3SpringMvc.onStartup");

        AnnotationConfigWebApplicationContext appContext = new AnnotationConfigWebApplicationContext();
        appContext.register(HelloConfig.class);

        DispatcherServlet dispatcher = new DispatcherServlet(appContext);

        ServletRegistration.Dynamic servlet =
                servletContext.addServlet("dispatcherV3", dispatcher);

        servlet.addMapping("/");
    }
}
```

### 핵심 포인트

-   WebApplicationInitializer는 스프링에서 이미 제공하는 표준 인터페이스
-   DispatcherServlet의 이름은 기존 서블릿들과 충돌하지 않도록 "dispatcherV3" 사용
-   addMapping("/")을 통해 모든 요청을 해당 서블릿으로 위임

---

## 4️⃣ 어떻게 자동으로 동작할까? — Spring 내부 구조

Spring MVC의 내부를 들여다보면, 다음 구조를 확인할 수 있습니다.

### 등록 파일:

```
/META-INF/services/jakarta.servlet.ServletContainerInitializer
```

### 등록된 클래스:

```
org.springframework.web.SpringServletContainerInitializer
```

### 내부 구현:

```
@HandlesTypes(WebApplicationInitializer.class)
public class SpringServletContainerInitializer implements ServletContainerInitializer {
    ...
}
```

즉, Spring MVC는 우리가 직접 했던 것과 동일하게:

-   ServletContainerInitializer를 구현하고
-   @HandlesTypes(WebApplicationInitializer.class)로 애플리케이션 초기화 구현체를 수집하며
-   그것들을 onStartup() 시점에 자동 실행합니다.

> 우리가 해야 할 일은 단 하나: WebApplicationInitializer 구현만 하면 됨

---

## 5️⃣ 실행 결과

서버를 실행한 후 http://localhost:8080/hello-spring에 접속하면 다음과 같은 출력이 나타납니다:

### HTTP 응답:

```
@HandlesTypes(WebApplicationInitializer.class)
public class SpringServletContainerInitializer implements ServletContainerInitializer {
    ...
}
```

### 콘솔 로그:

```
AppInitV3SpringMvc.onStartup
HelloController.hello
```

---

## 6️⃣ 현재 등록된 서블릿 비교



|  URL 패턴 | 서블릿 이름 |
| --- | --- |
| / | dispatcherV3 |
| /spring/\* | dispatcherV2 |
| /hello-servlet | helloServlet |
| /test | TestServlet |

> 서블릿 매핑의 우선순위는 **더 구체적인 경로가 먼저 처리**됩니다.

---

## 7️⃣ 일반적인 실무에서는?

위 예제는 학습 목적을 위해 DispatcherServlet과 스프링 컨테이너를 **여러 개 만들었지만**, 일반적인 실무에서는 다음과 같이 구성합니다:

-   스프링 컨테이너 **1개**
-   DispatcherServlet **1개**
-   매핑 경로는 주로 /

---

## 🧠 정리

| 구분 | 기존 방식 | Spring MVC 방식 |
| --- | --- | --- |
| 초기화 방식 | ServletContainerInitializer + SPI 등록 필요 | WebApplicationInitializer 구현만 하면 됨 |
| 구성 요소 연결 | 직접 탐색 및 등록 | Spring이 자동 실행 |
| 편의성 | ❌ 불편, 반복적 | ✅ 간결하고 직관적 |
| 사용 대상 | 프레임워크 개발자, 서블릿 구조 이해용 | 실무 Spring MVC 개발자 모두 사용 가능 |

---

## 📌 마무리

Spring MVC는 **서블릿 컨테이너의 초기화 구조를 추상화**하여 개발자가 **DispatcherServlet만 등록하면 스프링 컨테이너와 자동으로 연동되는 구조**를 만들어두었다.  
이를 통해 Spring Boot 이전 환경에서도 최소한의 코드로 **Spring MVC 애플리케이션을 구성할 수 있게 된다**.

이러한 구조를 이해하고 나면, Spring Boot가 제공하는 **자동 설정과 내장 톰캣 구성의 본질**도 자연스럽게 파악할 수 있다.