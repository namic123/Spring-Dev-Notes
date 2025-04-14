# 🚀 내장 톰캣 설정과 실행 구조 이해하기

Spring Boot 이전 혹은 직접 WAS를 컨트롤해야 하는 상황에서, **내장 톰캣을 직접 자바 코드로 실행하는 구조**는 매우 유용한 학습 소재가 됩니다. 톰캣을 라이브러리로 포함하고 Java 코드로 서버를 실행하는 방식은 Spring Boot의 동작 원리 이해에도 큰 도움이 됩니다.

---

## 📦 1. build.gradle 설정

내장 톰캣을 사용하기 위해선 다음과 같은 의존성과 빌드 설정이 필요합니다.

### 핵심 라이브러리

-   spring-webmvc: Spring MVC 기본 기능 제공
-   tomcat-embed-core: 톰캣을 서버로 직접 실행할 수 있게 해주는 핵심 라이브러리

---

## 🛠 2. Fat JAR 빌드 설정

두 가지 빌드 작업이 정의되어 있습니다:

```
// 일반 JAR
task buildJar(type: Jar) {
    manifest {
        attributes 'Main-Class': 'hello.embed.EmbedTomcatSpringMain'
    }
    with jar
}

// Fat JAR (라이브러리 포함 전체 JAR)
task buildFatJar(type: Jar) {
    manifest {
        attributes 'Main-Class': 'hello.embed.EmbedTomcatSpringMain'
    }
    duplicatesStrategy = DuplicatesStrategy.WARN
    from { configurations.runtimeClasspath.collect { it.isDirectory() ? it : zipTree(it) } }
    with jar
}
```

-   buildJar: 단순하게 메인 클래스만 포함
-   buildFatJar: 내장 톰캣을 포함한 모든 의존성을 묶은 실행 가능한 JAR

> 나중에 Spring Boot의 spring-boot-maven-plugin 또는 bootJar가 하는 역할과 비슷합니다.

---

## 💡 3. 내장 톰캣 실행 예제 - 서블릿 등록

자바 코드로 내장 톰캣을 구성하는 예시는 다음과 같습니다.

```
package hello.embed;

import hello.servlet.HelloServlet;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;

public class EmbedTomcatServletMain {
    public static void main(String[] args) throws LifecycleException {
        System.out.println("EmbedTomcatServletMain.main");

        // 톰캣 인스턴스 생성
        Tomcat tomcat = new Tomcat();

        // 포트 설정
        Connector connector = new Connector();
        connector.setPort(8080);
        tomcat.setConnector(connector);

        // Context 설정
        Context context = tomcat.addContext("", "/");

        // [문제 방지용 코드 추가 - 윈도우 등에서 디렉토리 오류 방지]
        File docBaseFile = new File(context.getDocBase());
        if (!docBaseFile.isAbsolute()) {
            docBaseFile = new File(((org.apache.catalina.Host) context.getParent()).getAppBaseFile(), docBaseFile.getPath());
        }
        docBaseFile.mkdirs();

        // 서블릿 등록
        tomcat.addServlet("", "helloServlet", new HelloServlet());
        context.addServletMappingDecoded("/hello-servlet", "helloServlet");

        // 톰캣 시작
        tomcat.start();
    }
}
```

---

## 🔄 실행 흐름 요약



| 단계 | 설명 |
| --- | --- |
| 1 | Tomcat 객체를 생성한다 |
| 2 | Connector를 생성하여 포트를 설정 (8080) |
| 3 | Context를 생성하여 루트 컨텍스트 경로(/) 설정 |
| 4 | HelloServlet을 등록하고 경로 매핑(/hello-servlet) |
| 5 | tomcat.start()로 서버 실행 시작 |

실행 후 브라우저에서 다음 URL로 접속하면 결과를 확인할 수 있습니다:

```
http://localhost:8080/hello-servlet
```

출력 결과:

```
hello servlet!
```

---

## ⚠️ 주의 사항

실행 시 다음과 같은 오류가 발생할 수 있습니다:

```
java.lang.IllegalArgumentException: The main resource set specified ... is not valid
```

이는 톰캣이 내부적으로 사용하는 docBase 경로가 유효하지 않을 때 발생하는데, 아래와 같은 코드로 보완할 수 있습니다:

```
File docBaseFile = new File(context.getDocBase());
if (!docBaseFile.isAbsolute()) {
    docBaseFile = new File(((org.apache.catalina.Host) context.getParent()).getAppBaseFile(), docBaseFile.getPath());
}
docBaseFile.mkdirs();
```

---

## 📌 참고 및 요약



| 항목 | 설명 |
| --- | --- |
| 목적 | 자바 코드로 톰캣 실행 및 서블릿 등록 |
| 이점 | IDE나 WAS 설치 없이 실행 가능 |
| 사용 예 | 내장 서버 환경 구현, 테스트 서버 구성, Spring Boot 이해용 |
| 주의점 | docBase 경로 오류 시 mkdirs() 보완 필요 |

---

## ✅ 마무리

Spring Boot는 이 내장 톰캣 구조를 완전히 감싸서 자동 설정 및 실행을 제공하지만, 내부적으로는 유사한 방식으로 톰캣을 구성합니다.  
따라서 내장 톰캣을 직접 실행해보는 경험은 **Spring Boot의 실행 원리**를 이해하는 데 매우 큰 도움이 됩니다.

하지만 실무에서는 이 구조를 직접 구현할 일은 거의 없으며, **이해 수준에 그치는 것이 적절합니다**