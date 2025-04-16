# 📦 Spring Boot 실행 가능 JAR 구조 및 원리

스프링 부트는 기존 Fat JAR의 단점을 해결하고, \*\*jar 내부에 jar 파일들을 안전하게 포함할 수 있는 실행 가능 JAR(Executable JAR)\*\*을 설계하여 배포와 실행을 매우 편리하게 만듭니다.

---

## ❌ Fat JAR의 한계

Fat JAR는 모든 라이브러리를 클래스 단위로 병합하여 하나의 JAR에 넣는 방식입니다. 이 방식은 다음과 같은 문제를 가집니다:

### Fat JAR의 단점

| 문제 | 설명 |
| --- | --- |
| 포함된 라이브러리 추적 어려움 | 모든 클래스가 풀려 있기 때문에 어떤 라이브러리가 포함되어 있는지 알기 어렵습니다. |
| 파일명 충돌 | META-INF/services/... 같은 경로에서 중복된 리소스가 존재할 경우, 하나만 포함되어 나머지는 무시됩니다. |

예시: jakarta.servlet.ServletContainerInitializer가 두 개의 라이브러리에 중복 포함된 경우 하나만 적용됨 → **기능 손실 발생**

---

## ✅ 실행 가능 JAR(Executable JAR)의 특징

Spring Boot는 이런 문제를 해결하기 위해 **jar 내부에 jar를 포함**할 수 있도록 새로운 구조를 설계했습니다. 이를 통해 다음을 달성합니다:

| 문제 | 해결 방식 |
| --- | --- |
| 라이브러리 추적 어려움 | 라이브러리별 JAR 파일을 그대로 내부에 보존하여 시각적으로 확인 가능 |
| 리소스 중복 문제 | JAR 파일을 그대로 유지하므로 리소스 경로 충돌 없이 독립적으로 보존 |

📌 참고: 이 구조는 **Java 표준 JAR 구조는 아니며**, Spring Boot에서 고안한 독자적인 실행 방식입니다.

---

## 🗂 실행 가능 JAR의 구조 예시

```
boot-0.0.1-SNAPSHOT.jar
├── META-INF/
│   └── MANIFEST.MF
├── org/springframework/boot/loader/  ← 스프링 부트 로더
│   └── JarLauncher.class
├── BOOT-INF/
│   ├── classes/        ← 개발한 코드 및 리소스
│   └── lib/            ← 외부 라이브러리 (jar 파일 형태로 유지)
├── classpath.idx       ← 클래스 경로 인덱스
└── layers.idx          ← 계층형 배포 구성 정보
```

---

## 🧠 실행 구조 원리

### MANIFEST.MF 설정

```
Manifest-Version: 1.0
Main-Class: org.springframework.boot.loader.JarLauncher
Start-Class: hello.boot.BootApplication
Spring-Boot-Version: 3.0.2
Spring-Boot-Classes: BOOT-INF/classes/
Spring-Boot-Lib: BOOT-INF/lib/
Spring-Boot-Classpath-Index: BOOT-INF/classpath.idx
Spring-Boot-Layers-Index: BOOT-INF/layers.idx
```

### 주요 항목 설명

| 항목 | 설명 |
| --- | --- |
| Main-Class | 실행 시 최초로 호출될 클래스 → JarLauncher |
| Start-Class | 실제로 우리가 작성한 main() 메서드 포함 클래스 |
| Spring-Boot-Classes | 개발자가 작성한 클래스 경로 |
| Spring-Boot-Lib | 포함된 라이브러리 jar 경로 |

---

## ⚙️ 실행 과정 요약

1.  java -jar boot-0.0.1-SNAPSHOT.jar 명령 실행
2.  MANIFEST.MF에서 Main-Class를 읽어 JarLauncher 실행
3.  JarLauncher가 내부 구조 분석:
    -   BOOT-INF/classes/ → 클래스 로딩
    -   BOOT-INF/lib/\*.jar → 라이브러리 로딩
4.  Start-Class에 명시된 BootApplication.main() 호출

📌 IDE에서는 JarLauncher가 필요 없음 → IDE가 직접 필요한 라이브러리를 로딩하여 실행

---

## 🔁 WAR vs 실행 가능 JAR 비교

| 항목 | WAR 방식 | 실행 가능 JAR |
| --- | --- | --- |
| 구조 경로 | WEB-INF/classes, WEB-INF/lib | BOOT-INF/classes, BOOT-INF/lib |
| 실행 방식 | 서블릿 컨테이너에 배포 | 단일 jar로 실행 (java -jar) |
| 독립성 | WAS 설치 필요 | 독립 실행 가능 |

---

## ✅ 정리

Spring Boot의 실행 가능 JAR는 단순한 Fat JAR의 한계를 넘어서 **안정성, 실행 편의성, 확장성**을 모두 만족시키는 구조입니다.

-   jar 내부에 jar 파일을 포함 가능
-   충돌 없는 라이브러리 관리
-   IDE와 CLI 모두 유연한 실행 지원

덕분에 Spring Boot 애플리케이션은 클라우드, Docker, CI/CD 환경에서도 **유지보수와 배포가 쉬운 구조**를 갖추게 되었습니다.