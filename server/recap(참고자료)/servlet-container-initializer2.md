# 서블릿 컨테이너 초기화 2단계 - 프로그래밍 방식 서블릿 등록과 AppInit 연동

서블릿 컨테이너 초기화에 대해 조금 더 깊이 살펴보자. 이번에는 @WebServlet 애노테이션 방식이 아닌, **프로그래밍 방식으로 서블릿을 직접 등록**하고 이를 보다 유연하게 구성할 수 있는 구조를 구현해볼 것이다.

---

## 📌 1. 서블릿을 등록하는 두 가지 방법

서블릿은 다음 두 가지 방식으로 등록할 수 있다:

-   @WebServlet 애노테이션 기반 자동 등록
-   ServletContext를 활용한 **프로그래밍 방식 등록**

후자는 초기화 시점에 조건을 걸거나 동적으로 등록할 수 있어 유연성이 높다.

---

## 📄 HelloServlet 클래스 정의

먼저 기본이 되는 서블릿 클래스부터 정의해보자.

```
package hello.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class HelloServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        System.out.println("HelloServlet.service");
        resp.getWriter().println("hello servlet!");
    }
}
```

브라우저에서 /hello-servlet으로 요청이 오면 로그를 출력하고 "hello servlet!"을 응답한다.

---

## 🛠 2. 애플리케이션 초기화용 인터페이스 정의

프로그래밍 방식으로 서블릿을 등록하려면, 초기화 시점에 실행될 **사용자 정의 인터페이스**가 필요하다.

```
package hello.container;

import jakarta.servlet.ServletContext;

public interface AppInit {
    void onStartup(ServletContext servletContext);
}
```

이 인터페이스는 서블릿 컨텍스트를 전달받아 개발자가 원하는 초기화 작업을 자유롭게 수행할 수 있도록 설계되었다.

---

## 🧩 3. AppInit 구현체: HelloServlet 등록

이제 위 인터페이스를 구현하여 HelloServlet을 초기화 시점에 등록하는 클래스를 만들어보자.

```
package hello.container;

import hello.servlet.HelloServlet;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;

public class AppInitV1Servlet implements AppInit {
    @Override
    public void onStartup(ServletContext servletContext) {
        System.out.println("AppInitV1Servlet.onStartup");

        // 순수 서블릿 등록
        ServletRegistration.Dynamic helloServlet =
                servletContext.addServlet("helloServlet", new HelloServlet());

        helloServlet.addMapping("/hello-servlet");
    }
}
```

이 방식은 실행 시점에 조건 분기 또는 외부 설정에 따라 동적으로 등록이 가능하며, 하드코딩된 @WebServlet보다 훨씬 유연하다.

---

## 🚀 4. 애플리케이션 초기화 연동: MyContainerInitV2

다음은 위에서 만든 AppInit 구현체들을 실행 시점에 자동으로 불러와 실행하는 초기화 클래스다. 핵심은 @HandlesTypes(AppInit.class)이다.

```
package hello.container;

import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.HandlesTypes;

import java.util.Set;

@HandlesTypes(AppInit.class)
public class MyContainerInitV2 implements ServletContainerInitializer {
    @Override
    public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
        System.out.println("MyContainerInitV2.onStartup");
        System.out.println("MyContainerInitV2 c = " + c);
        System.out.println("MyContainerInitV2 container = " + ctx);

        for (Class<?> appInitClass : c) {
            try {
                AppInit appInit = (AppInit) appInitClass.getDeclaredConstructor().newInstance();
                appInit.onStartup(ctx);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
```

-   @HandlesTypes(AppInit.class)  
    → 해당 인터페이스의 **모든 구현 클래스들을 탐지**해 Set<Class<?>>로 전달
-   newInstance()  
    → 전달받은 클래스 정보로 **실제 객체를 리플렉션을 통해 생성**
-   onStartup(ctx)  
    → 초기화 작업 실행

---

## 📂 5. SPI 등록

해당 초기화 클래스가 WAS에서 인식되도록 하기 위해, 다음 파일을 등록해야 한다.

경로:

```
resources/META-INF/services/jakarta.servlet.ServletContainerInitializer
```

내용:

```
hello.container.MyContainerInitV1
hello.container.MyContainerInitV2
```

> 이 파일은 **JDK의 SPI(Service Provider Interface)** 메커니즘을 따르며, 클래스 경로 기반으로 초기화 클래스를 WAS가 자동 인식하게 한다.

---

## ✅ 6. 실행 결과

서버를 실행하면 다음과 같은 로그가 출력된다

```
MyContainerInitV1.onStartup
MyContainerInitV2.onStartup
MyContainerInitV2 c = [class hello.container.AppInitV1Servlet]
AppInitV1Servlet.onStartup
```

-   /hello-servlet 요청 시 출력

```
hello servlet!
```

---

## 🧠 왜 이렇게 구성하는가?

### 1\. 프로그래밍 방식 vs 애노테이션 방식



| 방식 | 장점 | 단점 |
| --- | --- | --- |
| @WebServlet | 간편하게 등록 가능 | 유연성 낮음, 설정 변경 어려움 |
| 프로그래밍 방식 | 동적 등록 가능, 조건 분기 가능 | 코드 많고 복잡함 |

### 2\. AppInit 패턴의 이점

-   ServletContainerInitializer를 구현할 필요 없이, **인터페이스만 구현하면 자동 실행**
-   서블릿 컨테이너에 종속적이지 않은 구조 → 의존성 낮춤
-   프레임워크나 라이브러리에서 확장 포인트로 사용 가능

---

## 📌 마무리 요약



| 단계 | 설명 |
| --- | --- |
| 1 | AppInit 인터페이스 정의 |
| 2 | AppInitV1Servlet에서 서블릿 프로그래밍 방식 등록 |
| 3 | @HandlesTypes(AppInit.class)로 초기화 대상 탐색 |
| 4 | MyContainerInitV2에서 탐색된 클래스를 실행 |
| 5 | META-INF/services/로 SPI 등록 |