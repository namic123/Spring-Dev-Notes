**Tomcat 설치 방법 및 소스코드는 아래 github를 참고**

[https://github.com/namic123/Study-Backend/blob/master/server/recap(%EC%B0%B8%EA%B3%A0%EC%9E%90%EB%A3%8C)/install-tomcat.md](https://github.com/namic123/Study-Backend/blob/master/server/recap\(%EC%B0%B8%EA%B3%A0%EC%9E%90%EB%A3%8C\)/install-tomcat.md)

[Study-Backend/server/recap(참고자료)/install-tomcat.md at master · namic123/Study-Backend

Study projects for backend development . Contribute to namic123/Study-Backend development by creating an account on GitHub.

github.com](https://github.com/namic123/Study-Backend/blob/master/server/recap\(%EC%B0%B8%EA%B3%A0%EC%9E%90%EB%A3%8C\)/install-tomcat.md)

레거시 방식의 WAR 기반 웹 애플리케이션 구성은 현재 기준으로는 다소 번거롭고 복잡하게 느껴질 수 있다. 그러나 이를 직접 구성해보는 경험은 **웹 애플리케이션의 실행 구조**를 근본적으로 이해하는 데 큰 도움이 될 것이라 생각한다.

서블릿 등록, 디렉토리 구조, WAR 배포 방식 등을 직접 다뤄보면, **스프링 부트가 내부적으로 어떻게 작동하는지**, 그리고 **내장 톰캣과 자동 설정이 어떤 과정을 대체하는지** 자연스럽게 체감할 수 있을 것이다. 단순히 편리한 프레임워크 기능만 사용하는 것이 아니라, 그 이면의 원리를 이해하고 싶은 개발자라면 반드시 한번쯤 경험해볼 만한 과정이다.

**📌 목차**  
[1\. 정적 리소스(HTML) 등록](#static-resource)  
[2\. 서블릿 등록](#servlet-register)  
[3\. WAR 파일 빌드](#build-war)  
[4\. WAR 압축 해제 및 구조 확인](#war-structure)  
[5\. 톰캣에 WAR 배포](#deploy-tomcat)  
[6\. 실행 결과 확인](#result-confirm)  
[7\. 마무리 정리](#summary)

## 1\. 정적 리소스(HTML) 등록

웹 애플리케이션을 구축함에 있어 가장 기초적인 테스트는 정적 리소스가 정상적으로 서빙되는지 확인하는 것이다. 이를 위해 먼저 HTML 파일을 프로젝트 내에 구성해야 한다.

우선 **src/main/webap**p 디렉토리를 생성한다. 이 경로는 WAR(Web Application Archive) 구조상 웹 루트에 해당하며, HTML, CSS, JavaScript 등의 정적 파일이 이곳에 위치하게 된다.

다음으로 index.html 파일을 해당 디렉토리에 작성한다

```
<!-- src/main/webapp/index.html -->
<html>
  <body>index html</body>
</html>
```

이 파일은 배포 후 브라우저에서 http://localhost:8080/index.html 주소로 접근했을 때 바로 응답되어야 한다.

## 2\. 서블릿 등록

정적 파일만으로는 동적인 웹 애플리케이션의 기능을 수행하기 어렵다. 따라서 직접 동작하는 서블릿 클래스를 등록하여 동적 응답도 함께 테스트해보는 것이 바람직하다.

예시는 다음과 같다.

```
@WebServlet(urlPatterns = "/test")
public class TestServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        System.out.println("TestServlet.service");
        resp.getWriter().println("test");
    }
}
```

이 서블릿은 /test 경로로 접근했을 때 "test"라는 문자열을 응답으로 출력하며, 서버 로그에는 "TestServlet.service" 메시지를 남긴다.

## 3\. WAR 파일 빌드

모든 리소스 구성이 완료되었으면, 이를 패키징하여 WAR 형식으로 빌드한다. WAR 파일은 Java 웹 애플리케이션을 패키징하는 표준 포맷으로, 대부분의 서블릿 컨테이너(WAS)에서 배포 가능한 형태이다.

**빌드 도구 설정 (Gradle)**

```
plugins {
    id 'java'
    id 'war' // WAR 파일 생성을 위한 플러그인
}
```

이 설정이 존재해야 ./gradlew build 명령으로 .war 파일을 생성할 수 있다.

**빌드 명령 실행**

아래 빌드 명령어를 수행하기 전, 터미널을 켜 프로젝트 폴더로 이동

```
cd 프로젝트 폴더 경로

# macOS / Linux
./gradlew build

# Windows
gradlew build
```

빌드가 완료되면 build/libs/server-0.0.1-SNAPSHOT.war 파일이 생성된다.

## 4\. WAR 압축 해제 및 구조 확인

WAR 파일은 실질적으로 ZIP 압축 형식이다. 이를 직접 해제하여 내부 구조를 확인할 수 있다.

```
jar -xvf server-0.0.1-SNAPSHOT.war
```

압축 해제 후 구조는 다음과 같다

```
index.html                    ← 정적 리소스
WEB-INF/
├── classes/                 ← 컴파일된 클래스 파일
│   └── hello/servlet/TestServlet.class
├── lib/                     ← 의존성 라이브러리
│   └── jakarta.servlet-api-6.0.0.jar
└── web.xml (필요 시 존재)
```

## 5\. 톰캣에 WAR 배포

빌드된 WAR 파일을 직접 톰캣 서버에 배포하면 웹 애플리케이션으로 동작시킬 수 있다.

**배포 절차**

**1\. WAR 파일 복사 및 이름 변경**  
\- WAR 파일을 설치된 톰캣 폴더의 webapps/ 디렉토리에 복사하고, ROOT.war로 이름을 변경한다. (대소문자 주의)

[##_Image|kage@bJMT7E/btsNkjYp1rk/svPncCkshoeKeyl1NcmkZk/img.png|CDM|1.3|{"originWidth":703,"originHeight":61,"style":"alignCenter","caption":"톰캣"}_##][##_Image|kage@QgUe3/btsNjORZsDH/aqgBBIs8KTY2GkAX0gV08K/img.png|CDM|1.3|{"originWidth":777,"originHeight":405,"style":"alignCenter"}_##][##_Image|kage@Sii6i/btsNkuyDDrS/k7dMSQ86WxXM82s10BmukK/img.png|CDM|1.3|{"originWidth":693,"originHeight":68,"style":"alignCenter"}_##]

**톰캣 서버 시작**

터미널에서 톰캣 폴더 -> bin으로 이동 후, 아래 명령어로 톰캣서버 실행

```
./startup.sh      # macOS/Linux
startup.bat       # Windows
```

[##_Image|kage@bAopCV/btsNkIcmPSF/fHMQ8PK4o7BvhxTkVgJyl0/img.png|CDM|1.3|{"originWidth":374,"originHeight":26,"style":"alignLeft"}_##]

## 6\. 실행 결과 확인

정상적으로 배포가 완료되었다면 다음과 같은 결과를 확인할 수 있다

-   http://localhost:8080/index.html  
    → HTML 페이지 내용 "index html" 출력
-   http://localhost:8080/test  
    → "test" 응답, 콘솔에는 TestServlet.service 로그 출력

## 7\. 마무리 정리

| **단계** | **설명** |
| --- | --- |
| **정적 리소스 등록** | src/main/webapp/index.html 생성 |
| **서블릿 구성** | TestServlet 클래스 작성 및 @WebServlet 등록 |
| **WAR 빌드** | ./gradlew build 명령 실행 후 .war 파일 생성 |
| **구조 확인** | jar -xvf 명령으로 WAR 내부 파일 확인 |
| **톰캣 배포** | WAR 파일을 webapps/ROOT.war로 복사 후 재시작 |
| **결과 확인** | /index.html 및 /test 경로를 통해 검증 |