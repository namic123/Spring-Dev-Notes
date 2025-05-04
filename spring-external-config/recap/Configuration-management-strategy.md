## Spring Boot 외부 설정(5) - 설정 관리 전략: 외부 파일, 내부 분리, 내부 통합 비교 

블로그 : https://pjs-world.tistory.com/entry/Spring-Boot-%EC%99%B8%EB%B6%80-%EC%84%A4%EC%A0%955-%EC%84%A4%EC%A0%95-%EA%B4%80%EB%A6%AC-%EC%A0%84%EB%9E%B5-%EC%99%B8%EB%B6%80-%ED%8C%8C%EC%9D%BC-%EB%82%B4%EB%B6%80-%EB%B6%84%EB%A6%AC-%EB%82%B4%EB%B6%80-%ED%86%B5%ED%95%A9-%EB%B9%84%EA%B5%90

## 1\. 외부 설정 파일이 필요한 이유

현대의 애플리케이션은 단일 환경이 아닌, **개발(dev), 테스트(test), 운영(prod)** 등 다양한 환경에서 실행되어야 한다. 이때 각 환경에 따라 데이터베이스 주소, 사용자 인증 정보, API 키 등의 설정값이 달라지게 되며, 이러한 설정을 코드 내에 하드코딩하는 것은 유지보수성과 보안성 측면에서 매우 불리하다.

앞서 사용한 OS 환경변수, 자바 시스템 속성, 커맨드 라인 옵션 인수는 설정 항목이 몇 개일 때는 편리하지만, 실무처럼 **수십 개 이상의 설정 값**이 존재할 경우엔 매우 번거롭고 오류 가능성도 크다.

**그래서 해결책은?**

**설정 값을 application.properties 또는 application.yml 파일로 만들어 관리**하는 것이다.

## 2\. 외부 설정 파일 방식

외부 설정 파일 방식은 application.properties 또는 application.yml 파일을 서버 외부에 두고, 실행 시 해당 설정 파일을 읽어 필요한 설정값을 주입하는 전략이다.

**동작 방식**

1.  **코드를 작성하고 빌드**  
    gradle clean build (window)  
    → app.jar 생성
2.  **동일한 JAR 파일을 개발 서버/운영 서버에 배포**
3.  **서버마다 위치한 외부 설정 파일을 참조**
    -   개발 서버: application.properties → url=dev.db.com
    -   운영 서버: application.properties → url=prod.db.com
4.  **Spring Boot는 실행 시 외부 설정 파일을 읽어 설정값을 주입**  
    → Environment.getProperty("url")을 통해 해당 값 사용

**테스트**

**테스트를 위한 Environment 코드**

```
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EnvironmentCheck {

    private final Environment env;

    @PostConstruct
    public void init() {
        String url = env.getProperty("url");
        String user = env.getProperty("user");
        String password = env.getProperty("password");
        log.info("env url={}", url);
        log.info("env user={}", user);
        log.info("env pwd={}", password);
    }
}
```

**1\. 빌드**

[##_Image|kage@BBG4c/btsNKfoiBoY/8ferXMHHko914lIoX6eKhk/img.png|CDM|1.3|{"originWidth":846,"originHeight":616,"style":"alignCenter"}_##]

**2\. 배포 및 외부 설정 생성**

-   현재 jar 파일이 위치한 libs가 개발/운영 서버라고 가정
-   외부 설정 파일(properties)을 libs 하위에 생성 

[##_Image|kage@lHHVu/btsNJY1nygK/dpvCqukVbZuv5gdPjToJ31/img.png|CDM|1.3|{"originWidth":554,"originHeight":290,"style":"alignCenter"}_##]

**3\. 실행 및 외부 설정 정상 주입 여부 테스트**

-   jar가 위치한 곳으로 이동 (예: cd build/libs/)
-   아래 명령어 입력하여 실행

```
java -jar external-0.0.1-SNAPSHOT.jar
```

[##_Image|kage@cJtVDt/btsNJpLS8hJ/yPYwafGKSUK56KdazdEPA1/img.png|CDM|1.3|{"originWidth":1034,"originHeight":496,"style":"alignCenter"}_##]

**장점**

-   설정 변경을 위해 재빌드할 필요가 없다.
-   운영 환경과 개발 환경을 동일한 코드베이스로 관리할 수 있다.
-   설정 파일만 교체하면 새로운 환경에도 대응 가능하다.

**단점**

-   서버 수가 많아지면 각 서버마다 설정 파일을 관리해야 한다.
-   설정 파일은 코드 외부에 있으므로 버전 관리가 어렵고, 실수의 가능성이 존재한다.

이와 같은 단점들이 존재하므로, 개선한 방식이 바로 **"내부 설정 파일 분리"** 방식이다.

## 3\. 내부 파일 분리 방식

Spring Boot는 환경별 설정을 JAR 내부에 포함시키면서도 유연하게 관리할 수 있는 방법을 제공한다. 바로 **application-{profile}.properties** 또는 .yml 파일을 사용하는 방식이다.

**예시 구조**

[##_Image|kage@bMU96s/btsNKc56LfQ/6Qj5NYjj6Q4sxM23MGg11k/img.png|CDM|1.3|{"originWidth":280,"originHeight":226,"style":"alignCenter"}_##]

**동작 흐름**

| **단계** | **설명** |
| --- | --- |
| **1** | 프로젝트 내부에 application-dev.properties, application-prod.properties 설정 파일을 생성해 둔다. |
| **2** | 해당 설정 파일들과 함께 app.jar로 빌드됨. 이 때 설정 파일은 JAR 내부에 포함된다. |
| **3** | 하나의 동일한 JAR 파일을 개발/운영 서버에 배포한다. |
| **4** | 서버 실행 시, --spring.profiles.active=dev 또는 prod 값을 전달하여 어떤 설정 파일을 사용할지 결정한다. |

-   **application-dev.properties**

```
url=dev.db.com 
user=dev_user 
password=dev_pw
```

-   **application-prod.properties**

```
url=prod.db.com 
user=prod_user 
password=prod_pw
```

**테스트**

**커맨드라인 옵션 인수** 

[##_Image|kage@bHPypQ/btsNK32kBWL/bvd7cydaYiyMYxtqXwYpy0/img.png|CDM|1.3|{"originWidth":1100,"originHeight":632,"style":"alignCenter"}_##][##_Image|kage@k6uYw/btsNK3VwLHI/qA0kORtPntpbbAwmPhvCOk/img.png|CDM|1.3|{"originWidth":1196,"originHeight":710,"style":"alignCenter"}_##]

**자바 시스템 속성**

[##_Image|kage@5uB76/btsNLoSGz9P/ugLRvtXGPFyI7KS23QWU9k/img.png|CDM|1.3|{"originWidth":1094,"originHeight":632,"style":"alignCenter"}_##][##_Image|kage@ciEhq9/btsNL3HhybZ/DxlN65cV4hI4HoMtX8HbiK/img.png|CDM|1.3|{"originWidth":1090,"originHeight":683,"style":"alignCenter"}_##]

**커맨드라인 옵션 인수로 값 전달 후, jar 실행**

[##_Image|kage@yhwIP/btsNLuFbPzy/wkkCu7KKMueWGpe1mVuBsk/img.png|CDM|1.3|{"originWidth":1216,"originHeight":484,"style":"alignCenter"}_##]

**장점 vs 단점**

| **장점** | **단점** |
| --- | --- |
| **JAR 파일 하나로 모든 환경 대응 가능** | **설정 파일이 많아질 경우 가독성 저하** |
| **코드와 설정의 일관된 버전 관리 가능 (Git 관리)** | **하나의 설정에 대해 전체 구조를 한눈에 보기 어려움** |
| **빌드/배포 자동화와 친화적** | **프로필 오타 또는 누락 시 오류 발생** |

**보완 방법**

-   모든 설정을 하나의 application.properties 혹은 .yml 파일로 **통합**하고 **--- 구문으로 프로필별 설정 분리**도 가능

## 4\. 내부 파일 합체 방식

앞서 application-dev.properties, application-prod.properties로 **환경마다 별도 설정 파일을 분리**해 관리하는 방식은 구조적으로 좋지만, **설정 파일이 늘어나며 전체를 한눈에 보기 어렵다는 단점**이 존재한다.

이를 해결하기 위해 **하나의 설정 파일 안에 여러 프로필을 논리적으로 구분해서 관리**할 수 있는 방식이 바로 **내부 파일 합체 방식**이다.

**주요 포인트**

-   기존에는 파일을 application-dev.properties, application-prod.properties로 나눴다면,
-   이제는 **하나의 application.properties 파일 내에서 논리 구획을 나눠 관리**한다.
-   각 구획은 spring.config.activate.on-profile=dev 또는 prod로 시작하여 해당 프로필 활성화 시만 사용된다.

**application.properties (dev, prod 통합)**

```
spring.config.activate.on-profile=dev
url=dev.db.com
user=dev_user
password=dev_pw
#---
spring.config.activate.on-profile=prod
url=prod.db.com
user=prod_user
password=prod_pw
```

[##_Image|kage@UNp7w/btsNK14ulrI/OHjfLFCRiBfCdqbKNO7T31/img.png|CDM|1.3|{"originWidth":790,"originHeight":246,"style":"alignCenter"}_##]

**중요 조건**

-   #--- 구분자는 \*\*공백 없이 정확히 #---\*\*여야 한다.
-   구분선 **위아래에 다른 주석이나 공백 라인이 있으면 오류**가 발생할 수 있다.

테스트 진행 전 dev, prod를 삭제하고 위 내부 파일 분리 방식과 똑같이 테스트해보면 각 프로필로 분리되어 값이 전달되는 것을 확인할 수 있다.

## 5\. 마무리 정리

Spring Boot의 외부 설정 전략은 유연성과 유지보수성을 고려하여 다양한 방식을 제공하고 있다. 설정값을 어디에 두는지에 따라 접근 방법이 달라졌던 기존 문제를, Environment와 PropertySource라는 일관된 추상화를 통해 해결하고 있으며, 개발자는 오직 env.getProperty("key") 하나로 모든 설정에 접근할 수 있다

**선택 기준 정리**

| **전략** | **목적** | **특징** | **추천 상황** |
| --- | --- | --- | --- |
| **외부 설정 파일** | 서버마다 설정 파일 따로 관리 | 설정 분리, JAR 파일은 고정 | 서버별 설정이 명확히 나뉘는 경우 |
| **내부 파일 분리** | 설정을 JAR 내부에 포함 | 프로필별 파일 관리 | CI/CD 파이프라인, 설정 버전 관리 필요 시 |
| **내부 파일 합체** | 한 파일에 모든 설정 통합 | 논리적 분리, 가독성 좋음 | 설정이 많지 않은 경우, 관리 통합 필요 시 |