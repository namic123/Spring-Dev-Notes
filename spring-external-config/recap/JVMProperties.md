
## SpringBoot 프로젝트 외부 설정(2) - Java 시스템 속성 설정 및 조회 방법

블로그 : https://pjs-world.tistory.com/entry/SpringBoot-%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8-%EC%99%B8%EB%B6%80-%EC%84%A4%EC%A0%952-Java-%EC%8B%9C%EC%8A%A4%ED%85%9C-%EC%86%8D%EC%84%B1-%EC%84%A4%EC%A0%95-%EB%B0%8F-%EC%A1%B0%ED%9A%8C-%EB%B0%A9%EB%B2%95

## 1\. 자바 시스템 속성의 개념

자바 시스템 속성(Java System Properties)은 **JVM(Java Virtual Machine) 실행 시 전달할 수 있는 key-value 형식의 설정값**으로, 해당 애플리케이션 내부에서만 유효하게 작동하는 구성 방식이다.

이는 운영체제 전역에 영향을 주지 않고, 특정 애플리케이션에 국한된 설정값을 동적으로 주입할 수 있다는 점에서 유용하다.

예를 들어, 다음과 같은 커맨드라인 실행 시 설정을 적용할 수 있다.

```
java -Durl=dev -jar app.jar // jar 실행 명령어
```

위 설정은 System.getProperty("url")을 통해 애플리케이션 코드에서 읽을 수 있으며, 이와 같은 방식은 주로 DB 접속 정보, 환경 전환, API 인증 키 등 민감하거나 환경에 따라 달라지는 설정에 널리 사용된다.

## 2\. OS 환경 변수와의 차이점

자바 시스템 속성과 OS 환경 변수는 모두 외부 설정을 주입하는 수단이지만, 적용 범위와 접근 방식에서 아래와 같은 차이가 존재한다.

| **항목** | **OS 환경 변수** | **자바 시스템 속성** |
| --- | --- | --- |
| **적용 범위** | 시스템 전체 | JVM 인스턴스 단위 |
| **설정 방법** | OS에 등록 | JVM 실행 시 -D 옵션 |
| **코드 접근** | System.getenv() | System.getProperty() |
| **격리성** | 낮음 (전역 노출) | 높음 (로컬 JVM 제한) |
| **대표 용도** | 공통 환경 설정 | 애플리케이션별 구성 값 |

즉, 보안이나 격리성을 중시하는 상황에서는 자바 시스템 속성이 더 적합하다.

## 3\. 기본 속성 확인 예제

자바는 실행 시 자체적으로 다양한 시스템 속성을 자동으로 로딩한다. 다음은 현재 JVM에 설정된 모든 시스템 속성을 출력하는 간단한 테스트 코드 예시이다.

**테스트 코드 생성**

[##_Image|kage@LF4mI/btsNHSFXtZe/2oMRbLou4WPwe7B7PPIzp0/img.png|CDM|1.3|{"originWidth":404,"originHeight":506,"style":"alignCenter","caption":"테스트 코드 생성"}_##]

**예제 코드**

```
import lombok.extern.slf4j.Slf4j;
import java.util.Properties;

@Slf4j
public class JavaSystemProperties {
    public static void main(String[] args) {
        Properties properties = System.getProperties();
        for (Object key : properties.keySet()) {
            log.info("properties {}={}", key, System.getProperty(String.valueOf(key)));
        }

    }
}
```

**실행 결과**

[##_Image|kage@dfbnPz/btsNGufzt3m/IKKTjIQxIRTVV9nrXlohvK/img.png|CDM|1.3|{"originWidth":1406,"originHeight":524,"style":"alignCenter"}_##]

위 로그는 System.getProperties()를 통해 출력된 자바 시스템 속성이다.

대표적인 기본 속성은 다음과 같다.

| **속성명** | **설명** |
| --- | --- |
| **java.version** | 자바 버전 (예: 17.0.10) |
| **java.vendor** | 자바 배포사 정보 (Oracle Corporation) |
| **java.home** | JDK 또는 JRE 설치 경로 |
| **java.class.path** | 클래스 경로 (JAR 포함), 현재 프로젝트가 의존하는 모든 경로 |
| **java.library.path** | 네이티브 라이브러리(JNI 등)를 찾는 경로 (.dll, .so) |
| **java.io.tmpdir** | 임시 파일 저장 디렉토리 경로 |
| **java.vm.name** | JVM 이름 (Java HotSpot(TM) 64-Bit Server VM) |
| **java.runtime.version** | 런타임 상세 버전 (빌드 포함) |
| **java.specification.version** | 자바 사양 버전 (예: 17) |
| **os.name** | 운영체제 이름 (Windows 11) |

이처럼 시스템 속성을 통해 현재 실행 환경을 손쉽게 파악할 수 있다.

## 4\. 사용자 정의 속성 설정 및 조회

사용자는 **\-Dkey=value** 형식으로 JVM에 속성을 전달하고, 자바 코드에서는 **System.getProperty("key")**를 통해 이를 읽을 수 있다.

JVM 속성은 IDE 또는 배포 시 명령어를 통해 전달할 수 있으며, 아래 코드를 통해 속성 값을 조회할 수 있다.

**조회 코드 추가**

```
import lombok.extern.slf4j.Slf4j;
import java.util.Properties;

@Slf4j
public class JavaSystemProperties {
    public static void main(String[] args) {
        Properties prop = System.getProperties();
        
        for (Object key : prop.keySet()) {
            log.info("prop {}={}", key,
                    System.getProperty(String.valueOf(key)));
        }
        String url = System.getProperty("url");
        String username = System.getProperty("username");
        String password = System.getProperty("password");
        log.info("url={}", url);
        log.info("username={}", username);
        log.info("password={}", password);
    }
}
```

**다음은 IDE를 통해 JVM 속성을 전달하는 방법이다.**

**IDE를 통해 JVM 속성 전달 (IntelliJ 기준)**

**상단 우측 메뉴 > Edit > Run/Debug Configurations 열기**

[##_Image|kage@ckaByT/btsNHPoR6YB/vM1HHN2o6hn2E2P0ZejnuK/img.png|CDM|1.3|{"originWidth":344,"originHeight":114,"style":"widthContent"}_##]

**Modify options > Add VM options > VM options에 다음 인수를 추가** 

```
-Durl=devdb -Dusername=dev_user -Dpassword=dev_pw
```

[##_Image|kage@6eXhf/btsNF9im0Ht/FtfTLtpRA2SiBBknLJJbS0/img.png|CDM|1.3|{"originWidth":1366,"originHeight":624,"style":"alignCenter"}_##][##_Image|kage@d14qKz/btsNGCxzGmj/OqEgnOxW6THB7hFbV1TKg1/img.png|CDM|1.3|{"originWidth":790,"originHeight":119,"style":"alignCenter"}_##]

**실행 결과**

[##_Image|kage@djdJSH/btsNIh6wJbB/0FUfrc37hFWkekzooI1vAk/img.png|CDM|1.3|{"originWidth":940,"originHeight":216,"style":"alignCenter"}_##]

위 예시와 같이 IDE VM Option을 통해 속성값 전달이 가능하며, 다음은 jar 파일 배포시에 명령어로 전달하는 방식이다

**jar 파일 배포시에 명령어로 전달하는 방식**

**조회 코드 작성**

앞서 작성한 코드는 test 패키지 하위에 작성했으므로, jar 실행 후 main 메서드에서 조회하기 위해  main 패키지 하위에 다음 클래스를 추가

```
import lombok.extern.slf4j.Slf4j;
import java.util.Properties;

@Slf4j
public class JavaSystemProperties {
    public static void print(){
        Properties prop = System.getProperties();

        for (Object key : prop.keySet()) {
            log.info("prop {}={}", key,
                    System.getProperty(String.valueOf(key)));
        }
        String url = System.getProperty("url");
        String username = System.getProperty("username");
        String password = System.getProperty("password");
        log.info("url={}", url);
        log.info("username={}", username);
        log.info("password={}", password);
    }
}
```

**main함수에서 호출**

```
@SpringBootApplication
public class ExternalApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExternalApplication.class, args);
        JavaSystemProperties.print();
    }

}
```

**jar 파일 gradle 빌드**

IDE 내, 또는 터미널을 열고 아래 명령어 입력 (윈도우 기준)

**window**

```
gradle clean build
```

[##_Image|kage@wLjTw/btsNGA0TFjn/I4MR47aO0M1BzwEsfg2Pqk/img.png|CDM|1.3|{"originWidth":1276,"originHeight":496,"style":"alignCenter"}_##]

jar 파일이 있는 위치로 이동 후, 아래 명령어를 입력

```
java -Durl=devdb -Dusername=dev_user -Dpassword=dev_pw -jar <jar 파일명>
```

참고로 -D 는 Define의 약자다. 

[##_Image|kage@bI4rfg/btsNHynowlD/Ob9Ex096HzOK3Odx7FGigk/img.png|CDM|1.3|{"originWidth":1470,"originHeight":352,"style":"alignCenter"}_##]

**실행 결과**

[##_Image|kage@dvJDlD/btsNHKVH4SN/6FgsoOrWBvUNvQF2lD1aJ1/img.png|CDM|1.3|{"originWidth":1026,"originHeight":87,"style":"alignCenter"}_##]

## 5\. 코드 내 설정 사용 시 유의점

자바는 실행 중에도 System.setProperty()를 사용하여 속성을 추가할 수 있다.

```
System.setProperty("env", "local");
String env = System.getProperty("env");
```

그러나 이 방식은 설정이 코드에 하드코딩되므로 외부 설정을 사용하는 본래 목적을 훼손하게 된다. 실무 환경에서는 가급적 외부에서 주입받는 구조로 유지하는 것이 권장된다.

## 6\. 요약 및 마무리

자바 시스템 속성은 JVM 내에서만 유효한 설정값으로, 외부 설정의 유연성과 보안성을 동시에 확보할 수 있는 좋은 수단이다. -Dkey=value 방식은 설정의 명확성과 배포의 일관성을 보장하면서도, 코드 변경 없이 다양한 환경을 수용할 수 있게 해준다.

| **항목** | **설명** |
| --- | --- |
| **설정 대상** | DB 접속 정보, API Key, 환경 모드 등 |
| **설정 방법** | java -Dkey=value -jar app.jar |
| **접근 방식** | System.getProperty("key") |
| **장점** | 높은 격리성, 배포 유연성, 유지보수 효율 |
| **주의점** | JVM 내부에만 유효하므로 전역 공유 불가 |