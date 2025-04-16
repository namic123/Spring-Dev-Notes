## 📌 1. 내장 톰캣과 JAR 실행 구조 이해

---

### 🔧 전통적인 WAR 방식과의 차이

| 항목 | WAR 방식 | 내장 톰캣 방식 (JAR 방식) |
| --- | --- | --- |
| 실행 환경 | 톰캣 같은 WAS 필요 | 자바만 설치되면 실행 가능 |
| 배포 파일 | .war | .jar |
| 실행 방식 | WAS에 배포 후 실행 | java -jar 로 실행 |
| 설정 복잡성 | 톰캣 설정 필요 | Main 클래스만 있으면 됨 |

---

## 🚧 2. Fat Jar 도입 전 – 실행 실패 이유 분석

---

### 🔍 초기 설정: Main-Class만 정의한 JAR

```
task buildJar(type: Jar) {
    manifest {
        attributes 'Main-Class': 'hello.embed.EmbedTomcatSpringMain'
    }
    with jar
}
```

위 설정은 EmbedTomcatSpringMain 클래스만 entry point로 지정한 가장 기본적인 JAR 빌드 방식입니다.

### ❗ 문제 발생

```
java -jar embed-0.0.1-SNAPSHOT.jar
```

```
Error: Unable to initialize main class hello.embed.EmbedTomcatSpringMain
Caused by: java.lang.NoClassDefFoundError: org/springframework/web/context/WebApplicationContext
```

### 🧨 원인 분석

-   이 오류는 **해당 클래스가 사용하는 스프링/톰캣 관련 라이브러리를 찾을 수 없기 때문**입니다.
-   **JAR 파일은 다른 JAR 파일을 포함하지 않습니다.** (즉, lib/\*.jar처럼 포함된 파일을 자동으로 로딩하지 않음)
-   WAR 파일과 달리 JAR은 class를 모아서 실행하는 구조이고, 라이브러리를 명시적으로 classpath에 넣지 않으면 인식 불가합니다.

---

## ✅ 3. Fat Jar (또는 Uber Jar)로 문제 해결

---

### 🧩 Fat Jar 개념

-   Fat Jar는 의존성으로 사용되는 모든 .jar 파일을 풀어 class 파일 단위로 하나의 .jar 안에 넣는 방식입니다.
-   이 방식은 JAR 스펙의 한계를 우회하여, 하나의 .jar 파일만으로도 **완전한 실행 환경을 구성**할 수 있습니다.

### 💡 build.gradle 설정

```
task buildFatJar(type: Jar) {
    manifest {
        attributes 'Main-Class': 'hello.embed.EmbedTomcatSpringMain'
    }
    duplicatesStrategy = DuplicatesStrategy.WARN
    from {
        configurations.runtimeClasspath.collect { it.isDirectory() ? it : zipTree(it) }
    }
    with jar
}
```

-   zipTree(it)로 각 의존 JAR을 풀어서 .class 파일로 포함
-   duplicatesStrategy = DuplicatesStrategy.WARN은 같은 경로 파일 충돌 시 경고만 출력

### ✅ 빌드 및 실행

```
./gradlew clean buildFatJar
java -jar build/libs/embed-0.0.1-SNAPSHOT.jar
```

```
INFO: Starting Servlet engine: [Apache Tomcat/9.0.65]
INFO: Starting ProtocolHandler ["http-nio-8080"]
```

브라우저에서 http://localhost:8080/hello-spring → 정상 출력

---

## 📦 4. Fat Jar 내부 구조 비교

---

### ⚙️ Fat Jar 압축 해제 결과

```
jar -xvf embed-0.0.1-SNAPSHOT.jar
```

-   프로젝트에서 작성한 .class 파일뿐 아니라,
    -   org.springframework.\*
    -   org.apache.catalina.\*
    -   META-INF/ 등 **라이브러리에서 제공하는 클래스들이 모두 class 파일로 포함**

---

## 📚 5. WAR 방식 대비 Fat Jar 방식의 장점과 단점

---

### ✅ Fat Jar 장점 정리

| 항목 | 설명 |
| --- | --- |
| 단일 실행 파일 | 모든 의존성 포함한 .jar 하나로 실행 가능 |
| WAS 불필요 | 내장 톰캣 포함, 톰캣 설치 없이 실행 |
| 배포 단순화 | WAS 배포 생략 → scp + java -jar로 실행 가능 |
| IDE 연동 간단 | WAS 연동 설정 없이 main() 실행으로 테스트 가능 |
| 톰캣 버전 관리 | Gradle 의존성으로 버전만 바꾸면 적용 가능 |

---

### ❌ Fat Jar 단점 정리

항목문제점설명

| 추적 어려움 | 어떤 라이브러리가 포함됐는지 추적 어려움 |   |
| --- | --- | --- |
| 파일 중복 | 라이브러리 간 중복 리소스(META-INF 등)는 하나만 유지됨 |   |
| 의도치 않은 누락 | ServletContainerInitializer 같은 핵심 초기화 파일 충돌 시 하나만 유지되어 일부 기능 누락 가능 |