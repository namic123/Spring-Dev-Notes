# 톰캣 설치 및 실행 가이드

### ― 로컬 환경에서 Apache Tomcat 설정하기

스프링 프레임워크 또는 순수 서블릿 기반 웹 애플리케이션을 개발하려면, \*\*웹 애플리케이션 서버(WAS)\*\*인 톰캣(Tomcat)을 설치하고 실행할 수 있어야 한다. 특히, 스프링 3.x 버전 이상을 사용할 경우 **Java 17 이상**의 설치가 필수적이므로, 다음 내용을 따라 개발 환경을 올바르게 구성할 수 있도록 하자.

---

## ☕ 자바 버전 확인 및 설정

톰캣 10 및 스프링 프레임워크 6.x / 스프링 부트 3.x 이상 버전을 사용하기 위해서는 **Java 17 이상**이 설치되어 있어야 한다.  
Java가 설치되어 있지 않거나, 구버전이 설치되어 있다면 [Oracle 공식 사이트](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)에서 최신 JDK를 다운로드하여 설치하도록 한다.

---

## ⬇️ 톰캣 다운로드 및 압축 해제

톰캣은 [Apache Tomcat 공식 다운로드 페이지](https://tomcat.apache.org/download-10.cgi)에서 받을 수 있으며, 본 가이드에서는 **Tomcat 10.x** 버전을 기준으로 설명한다.

1.  아래 링크에서 톰캣을 다운로드한다.  
    📦 다운로드 링크:  
    [https://dlcdn.apache.org/tomcat/tomcat-10/v10.1.17/bin/apache-tomcat-10.1.17.zip](https://dlcdn.apache.org/tomcat/tomcat-10/v10.1.17/bin/apache-tomcat-10.1.17.zip)
2.  압축을 해제한 후, 해당 폴더 내의 /bin 디렉토리로 이동한다.

---

## ▶️ 톰캣 실행 방법

### ✅ MacOS / Linux 환경

1.  터미널을 열고 apache-tomcat-10.1.17/bin 디렉토리로 이동한다.
2.  실행 권한을 부여한다:
3.  bash

    복사편집

    chmod 755 \*

4.  톰캣 서버를 실행한다:
5.  bash

    복사편집

    ./startup.sh

6.  서버를 중지하고 싶을 경우:
7.  bash

    복사편집

    ./shutdown.sh


🔎 참고: 실행 권한을 부여하지 않으면 permission denied 오류가 발생할 수 있다.

---

### ✅ Windows 환경

1.  apache-tomcat-10.1.17/bin 폴더로 이동한다.
2.  실행 파일을 더블 클릭하거나, 명령 프롬프트(cmd)에서 다음 명령어로 실행한다:
3.  복사편집

    startup.bat

4.  서버를 중지하려면:
5.  arduino

    복사편집

    shutdown.bat


---

## 🌐 실행 확인

톰캣을 정상적으로 실행한 경우, 다음 URL에 접속하면 톰캣 서버의 초기 화면을 확인할 수 있다:

👉 [http://localhost:8080](http://localhost:8080)

---

## 📋 실행 로그 확인

톰캣의 실행 상태 및 로그는 다음 파일에서 확인할 수 있다:

bash

복사편집

톰캣폴더/logs/catalina.out

문제가 발생하거나 정상적으로 실행되지 않을 경우, 해당 로그 파일을 열어 오류 메시지나 포트 충돌 여부를 확인하도록 하자.

---

## ✅ 요약

항목내용

| 자바 버전 | Java 17 이상 필요 |
| --- | --- |
| 톰캣 버전 | Tomcat 10.x (예: 10.1.17) |
| 실행 주소 | [http://localhost:8080](http://localhost:8080) |
| 실행 스크립트 | startup.sh (Mac/Linux), startup.bat (Windows) |
| 로그 파일 위치 | logs/catalina.out |
| 포트 충돌 시 대처 | 로그 확인 후 포트 변경 또는 해당 프로세스 종료 |