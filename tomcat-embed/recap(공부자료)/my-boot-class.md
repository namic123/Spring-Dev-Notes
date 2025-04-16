## 1\. 개요

MySpringApplication은 스프링 부트의 SpringApplication.run() 과 유사하게,  
내장 톰캣 설정 + Spring 컨테이너 생성 + DispatcherServlet 등록 + 컴포넌트 스캔을 **한 줄**로 처리할 수 있게 만든 **커스텀 부트 클래스**입니다.

---

## 2\. MySpringApplication 클래스 상세 설명

```
public static void run(Class configClass, String[] args) {
```

-   configClass: 보통 @Configuration 혹은 @ComponentScan이 붙은 클래스를 의미하며, 스프링 컨테이너가 이 클래스를 기준으로 빈 등록을 시작합니다.
-   args: main() 메서드의 인자이며 필요시 실행 인자로 사용 가능.

### 🔸 주요 처리 과정

1.  **Tomcat 인스턴스 생성 및 포트 설정**

```
Tomcat tomcat = new Tomcat();
Connector connector = new Connector();
connector.setPort(8080);
tomcat.setConnector(connector);
```

**2.Spring 컨테이너 생성**

-   설정 클래스를 등록하여 스프링 컨테이너를 구성합니다.

```
AnnotationConfigWebApplicationContext appContext = new AnnotationConfigWebApplicationContext();
appContext.register(configClass);
```

3.**DispatcherServlet 연결**

```
DispatcherServlet dispatcher = new DispatcherServlet(appContext);
```

4. **Tomcat에 DispatcherServlet 등록**

```
Context context = tomcat.addContext("", "/");
tomcat.addServlet("", "dispatcher", dispatcher);
context.addServletMappingDecoded("/", "dispatcher");
```

5\. **Tomcat 시작**

```
tomcat.start();
```

---

## 3\. @MySpringBootApplication 애노테이션 설명

```
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ComponentScan
public @interface MySpringBootApplication {
}
```

-   @ComponentScan을 포함하고 있어, 해당 애노테이션이 붙은 클래스 기준으로 하위 패키지를 스캔하여 Bean 등록을 자동화합니다.
-   @SpringBootApplication이 @ComponentScan + @Configuration + @EnableAutoConfiguration을 포함한 것처럼, **간소화된 커스텀 버전**입니다.

---

## 4\. HelloConfig 변경 이유

```
//@Configuration // 주석 처리
public class HelloConfig {
 @Bean
 public HelloController helloController() {
   return new HelloController();
 }
}
```

-   @ComponentScan으로 HelloController를 찾아서 Bean 등록할 것이기 때문에 @Configuration은 주석 처리합니다.
-   즉, **직접 Bean 등록을 하지 않고 자동 스캔 방식으로 전환**한 것입니다.

---

## 5\. MySpringBootMain 실행 클래스

```
@MySpringBootApplication
public class MySpringBootMain {
 public static void main(String[] args) {
   MySpringApplication.run(MySpringBootMain.class, args);
 }
}
```

-   이 클래스를 기준으로 컴포넌트 스캔이 시작되며, 개발자는 **한 줄만 호출**하면 웹 애플리케이션이 시작됩니다.
-   @MySpringBootApplication → @ComponentScan → hello.spring.HelloController 자동 스캔

---

## 6\. 컴포넌트 스캔 동작 원리

-   @ComponentScan은 **애노테이션이 붙은 클래스의 패키지를 기준으로 하위 패키지까지 탐색**합니다.
-   hello.MySpringBootMain은 hello 패키지에 위치하므로 hello.spring도 자동으로 스캔됩니다.

---

## 7\. 스프링 부트와의 비교



| 항목 | MySpringApplication | Spring Boot |
| --- | --- | --- |
| 서버 실행 | Tomcat 직접 생성 및 실행 | 내장 톰캣 자동 구성 |
| DispatcherServlet 등록 | 수동 등록 | 자동 등록 |
| 빈 등록 | AnnotationConfigWebApplicationContext 수동 구성 | 자동 구성 및 설정 클래스 처리 |
| 컴포넌트 스캔 | 수동 애노테이션 구성 (@MySpringBootApplication) | @SpringBootApplication에 기본 포함 |
| 장점 | 학습용으로 내부 구조를 완벽히 이해 가능 | 빠른 개발, 자동 설정, 다양한 스타터 제공 |

---

## 8\. 마무리

이 구조는 단순히 작동하는 서버를 만드는 것이 목적이 아니라,

> **Spring Boot가 내부에서 어떻게 애플리케이션을 구성하는지**  
> **핵심 메커니즘을 이해하는 데 목적이 있습니다.**

실무에서는 대부분 Spring Boot를 사용하지만, 이처럼 수작업으로 내부 동작을 구성해보면  
**스프링 부트가 얼마나 많은 일을 대신 해주는지**,  
그리고 **어떤 철학(자동 구성, 명확한 진입점)을 가지고 있는지** 명확하게 이해할 수 있습니다.