# ☕ 서블릿 컨테이너 초기화 - ServletContainerInitializer

## 1\. 서블릿 초기화의 필요성

WAS(Web Application Server)가 실행될 때는 다양한 초기화 작업이 필요합니다. 예를 들어:

-   서블릿 및 필터 등록
-   스프링 컨테이너 생성
-   스프링과 서블릿을 연결하는 **DispatcherServlet** 설정 등

과거에는 web.xml을 통해 이러한 초기화를 했지만, 현재는 **자바 코드 기반 초기화 방식**이 지원되며 더 유연하게 구성할 수 있습니다.

---

## 2\. 서블릿 컨테이너와 스프링 컨테이너 연결을 위한 준비

서블릿 컨테이너가 애플리케이션을 구동하면서 직접 초기화 작업을 수행할 수 있도록, ServletContainerInitializer 라는 인터페이스가 제공됩니다. 이 인터페이스는 WAS가 시작되면서 자동으로 실행됩니다.

---

## 3\. ServletContainerInitializer 인터페이스

```
public interface ServletContainerInitializer {
    public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException;
}
```

-   Set<Class<?>> c : 초기화 대상 클래스 목록. @HandlesTypes 애노테이션과 함께 사용하여 원하는 클래스를 자동 탐지 가능
-   ServletContext ctx : 서블릿 컨테이너의 설정 및 기능을 제공하는 객체 (서블릿 및 필터 등록 등에 사용)

---

## 4\. 초기화 클래스 구현 예시

```
package hello.container;

import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import java.util.Set;

public class MyContainerInitV1 implements ServletContainerInitializer {
    @Override
    public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
        System.out.println("MyContainerInitV1.onStartup");
        System.out.println("MyContainerInitV1 c = " + c);
        System.out.println("MyContainerInitV1 ctx = " + ctx);
    }
}
```

위 코드는 ServletContainerInitializer를 구현한 클래스이며, 톰캣 같은 WAS가 실행될 때 자동으로 onStartup() 메서드가 호출됩니다.

---

## 5\. 서비스 등록 파일 작성

위 클래스가 **초기화 클래스임을 WAS에 알리기 위해**, 아래 위치에 서비스 파일을 생성해야 합니다.

### 경로 및 파일명:

```
resources/META-INF/services/jakarta.servlet.ServletContainerInitializer
```

### 파일 내용:

```
hello.container.MyContainerInitV1
```

> 즉, 이 파일의 경로와 이름이 정확해야 초기화 클래스가 인식됩니다.

📌 주의사항

-   META-INF는 대문자입니다.
-   services는 반드시 \*\*복수형(s)\*\*입니다.
-   파일명은 **jakarta.servlet.ServletContainerInitializer**여야 합니다.

---

## 6\. 실행 결과

WAS를 실행하면 다음과 같은 로그가 출력됩니다:

```
MyContainerInitV1.onStartup
MyContainerInitV1 c = null
MyContainerInitV1 ctx = org.apache.catalina.core.ApplicationContextFacade@65112751
```

-   onStartup() 메서드가 호출되어 초기화 코드가 실행되었음을 의미합니다.
-   ctx는 실제 서블릿 컨텍스트 객체이며, 이를 활용하여 서블릿 및 필터 등을 등록할 수 있습니다.

---

## ✅ 요약

| 항목 | 설명 |
| --- | --- |
| 초기화 방법 | ServletContainerInitializer 인터페이스 구현 |
| 자동 인식 방법 | META-INF/services 디렉토리에 SPI 등록 파일 작성 |
| 주요 기능 | WAS 시작 시점에 onStartup() 호출, 초기화 로직 수행 |
| 활용 사례 | DispatcherServlet 등록, 스프링 컨텍스트 초기화 등 |