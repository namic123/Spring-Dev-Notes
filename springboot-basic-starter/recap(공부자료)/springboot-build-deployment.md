## 📦 스프링 부트와 웹 서버 - 빌드와 배포 설명

### ✅ 1. JAR 빌드

Spring Boot 프로젝트는 다음 명령어로 빌드합니다:

```
./gradlew clean build  # macOS / Linux
gradlew clean build    # Windows
```

빌드가 완료되면 다음 위치에 실행 가능한 JAR 파일이 생성됩니다:

```
build/libs/boot-0.0.1-SNAPSHOT.jar
```

---

### ✅ 2. JAR 실행

빌드된 JAR 파일을 아래처럼 java -jar 명령어로 실행할 수 있습니다:

```
java -jar build/libs/boot-0.0.1-SNAPSHOT.jar
```

### 🔍 실행 결과 예시:

```
Tomcat started on port(s): 8080 (http) with context path ''
Started BootApplication in 0.961 seconds
```

-   내장 톰캣이 자동으로 8080 포트에서 실행됩니다.
-   Spring 애플리케이션의 엔트리포인트(BootApplication)도 자동 실행됩니다.

---

### ✅ 3. 컨트롤러 실행 확인

```
http://localhost:8080/hello-spring
```

정상적으로 접속된다면 HelloController가 등록되고, DispatcherServlet에 의해 매핑되었음을 의미합니다.

---

## 🔍 Spring Boot JAR 내부 구조 분석

Spring Boot의 JAR는 일반적인 Fat Jar와는 다릅니다. 구조를 확인하기 위해 JAR를 압축 해제해 봅니다:

```
jar -xvf boot-0.0.1-SNAPSHOT.jar
```

### 📁 압축 해제 후 주요 구조:

```
boot-0.0.1-SNAPSHOT.jar/
├── META-INF/
│   └── MANIFEST.MF  (Main-Class 및 실행 정보)
├── org/springframework/boot/loader/
│   └── JarLauncher.class  ← 스프링 부트 실행 진입점
├── BOOT-INF/
│   ├── classes/            ← 우리가 작성한 클래스 및 리소스
│   │   └── hello/boot/...  (컨트롤러, 설정 등)
│   └── lib/                ← 외부 라이브러리 (tomcat, spring 등)
│       ├── spring-webmvc-6.0.4.jar
│       ├── tomcat-embed-core-10.1.5.jar
│       └── ...
├── classpath.idx           ← 클래스 경로 인덱스
└── layers.idx              ← 계층화 배포에 사용되는 메타데이터
```

### 🔧 핵심 특징:

| 요소 | 설명 |
| --- | --- |
| BOOT-INF/classes/ | 우리가 작성한 코드 및 설정 클래스 |
| BOOT-INF/lib/ | 외부 라이브러리들이 개별 .jar 파일로 존재 |
| JarLauncher.class | 스프링 부트가 실행 시 사용하는 부트스트랩 클래스 |
| META-INF/MANIFEST.MF | Main-Class가 org.springframework.boot.loader.JarLauncher 로 지정되어 있음 |

---

### 🧠 어떻게 Fat Jar 문제를 해결했나?

-   **Fat Jar 문제점**: 일반적으로 JAR 안에 JAR를 넣으면 JVM이 classpath로 인식하지 못함.
-   **Spring Boot 방식**: 내부적으로 JarLauncher, LaunchedURLClassLoader 등을 사용해 BOOT-INF/lib에 있는 라이브러리들을 **동적으로 classpath에 추가**함.
-   이는 Spring Boot만의 커스텀 class loader와 JAR 런처 덕분에 가능한 구조입니다.

---

### ⚠️ 참고 - boot-0.0.1-SNAPSHOT-plain.jar

-   plain.jar: 우리가 작성한 클래스만 들어 있음 (라이브러리 제외)
-   실행 불가, 사용하지 않아도 됨
-   Gradle의 기본 jar task로 생성된 결과물

---

## ✅ 요약

| 항목 | 설명 |
| --- | --- |
| 실행 명령 | java -jar build/libs/boot-0.0.1-SNAPSHOT.jar |
| 실행 구조 | 내장 톰캣 포함, Fat Jar 형태이지만 Spring Boot 전용 구조 사용 |
| 내부 구조 | BOOT-INF/classes, BOOT-INF/lib, JarLauncher, MANIFEST.MF |
| 장점 | 라이브러리 자동 관리, 실행 파일 하나로 배포 가능, 별도 톰캣 설치 불필요 |