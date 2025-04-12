# ☕ JAR와 WAR의 차이점과 구조에 대한 자세한 설명

Java 기반 애플리케이션을 패키징할 때 주로 사용되는 파일 형식에는 **JAR**와 **WAR**가 있다. 이 둘은 모두 Java 애플리케이션을 압축하여 배포하거나 실행 가능하게 만드는 데 사용되지만, 그 목적과 구조, 실행 환경에 따라 확연한 차이를 보인다. 이번 글에서는 JAR와 WAR의 개념부터 구조, 실행 방식까지 자세히 살펴보도록 하겠다.

---

## 📦 JAR(Java Archive) 소개

JAR 파일은 이름 그대로 여러 개의 .class 파일, 리소스 파일, 메타 정보 등을 하나로 묶은 **Java 전용 압축 파일**이다. 기술적으로는 ZIP 형식을 따르며, 일반 Java 애플리케이션을 배포하거나 라이브러리로 제공할 때 사용된다.

### ✅ 주요 특징

-   .jar 확장자를 가지며, JVM(Java Virtual Machine)에서 실행 가능하다.
-   JAR 파일을 **직접 실행하기 위해서는 main() 메서드를 포함한 클래스가 필요**하며, 해당 클래스는 JAR 내부의 META-INF/MANIFEST.MF 파일에 명시되어야 한다.
-   실행 명령어:

```
java -jar abc.jar
```

-   라이브러리로 활용되는 경우, 다른 프로젝트에서 dependencies에 포함시켜 참조 가능하다.

### ✅ 용도

사용 형태설명

| 실행용 애플리케이션 | 독립형 프로그램으로 실행 (예: 백엔드 서버, 배치 프로그램) |
| --- | --- |
| 라이브러리 용도 | 다른 애플리케이션에서 import 하여 사용 (예: commons-lang3.jar) |

### ✅ 구조 예시

```
abc.jar
├── META-INF/
│   └── MANIFEST.MF  # main class 정의 가능
├── com/example/MyApp.class
├── application.properties
└── ...
```

## 🌐 WAR(Web Application Archive) 소개

WAR 파일은 \*\*웹 애플리케이션 서버(WAS)\*\*에 배포하기 위해 설계된 Java 아카이브 파일 형식이다. WAR는 단순한 압축 형식 이상의 구조를 가지고 있으며, \*\*서블릿 컨테이너(예: 톰캣, 제티, 웹로직 등)\*\*에서만 실행이 가능하다.

### ✅ 주요 특징

-   .war 확장자를 가지며, WAS(Web Application Server) 위에서 동작한다.
-   HTML, CSS, 이미지 등 **정적 자원**과 .class 파일, 라이브러리 등을 함께 포함한다.
-   전통적으로 WEB-INF/web.xml을 통해 서블릿, 필터, 리스너 등을 등록해왔다. 현재는 애노테이션으로 대체 가능.

### ✅ 용도

-   웹 기반 애플리케이션 (예: JSP, Spring MVC)
-   기업용 레거시 시스템 유지보수
-   WAS 기반 운영 환경에 최적화된 배포

---

## 🗂 WAR의 디렉토리 구조

WAR 파일은 반드시 일정한 디렉토리 구조를 따라야 하며, 이 구조는 웹 서버에서 해당 애플리케이션을 인식하고 실행하기 위한 전제조건이다.

```
myapp.war
├── index.html                   # 정적 리소스 (웹 루트)
├── css/style.css               # 기타 정적 리소스
├── js/app.js
├── WEB-INF/
│   ├── web.xml                 # 배치 설정 파일 (선택)
│   ├── classes/                # 컴파일된 클래스 파일 (.class)
│   │   └── com/example/...
│   └── lib/                    # 의존 라이브러리 (.jar)
│       └── spring-core.jar ...
```

### 📁 디렉토리 설명



| 디렉토리 | 파일설명 |
| --- | --- |
| /WEB-INF | WAS에서 접근할 수 있으나 클라이언트는 접근 불가능 |
| /WEB-INF/classes | 컴파일된 .class 파일이 저장되는 위치 |
| /WEB-INF/lib | 의존 JAR 파일들을 저장하는 위치 |
| /WEB-INF/web.xml | 서블릿 및 필터 매핑 등의 설정을 정의하는 XML 파일 (Spring에서는 생략 가능) |
| /index.html, /static/ | HTML, CSS, JS 등의 정적 리소스 (클라이언트 접근 가능) |

> WAR의 가장 핵심적인 부분은 WEB-INF 폴더이며, 이 내부에 애플리케이션 실행에 필요한 클래스, 라이브러리, 설정 정보가 포함된다.

---

## ⚖️ JAR vs WAR 요약 비교

항목JARWAR

| 확장자 | .jar | .war |
| --- | --- | --- |
| 실행 환경 | JVM (단독 실행 가능) | WAS (톰캣, 웹로직 등 필요) |
| 사용 목적 | 일반 Java 프로그램, 백엔드 API, CLI 도구 등 | 웹 애플리케이션 배포 |
| 구조 | 비교적 단순 | 정해진 디렉토리 구조 필요 (WEB-INF 등) |
| 실행 방법 | java -jar | WAS 서버의 webapps 디렉토리 배포 |
| 설정 방식 | MANIFEST.MF | web.xml 또는 애노테이션 |
| 클라이언트 정적 접근 | 제한적 (별도 설정 필요) | 기본적으로 index.html, CSS, JS 접근 가능 |