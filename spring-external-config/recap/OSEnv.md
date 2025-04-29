## SpringBoot 프로젝트 외부 설정(1) 개념 및 활용 - OS 환경 변수

블로그: https://pjs-world.tistory.com/entry/SpringBoot%EA%B8%B0%EB%B0%98-%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8-%EC%99%B8%EB%B6%80-%EC%84%A4%EC%A0%951-%EA%B0%9C%EB%85%90-%EB%B0%8F-%ED%99%9C%EC%9A%A9-OS-%ED%99%98%EA%B2%BD-%EB%B3%80%EC%88%98

## 1\. 외부 설정의 개념

외부 설정(external configuration)은 실행 환경마다 달라질 수 있는 설정값(DB 주소, 포트, API 키 등)을 코드가 아닌 외부에서 주입하는 구조를 말한다. 이는 개발, 운영, 테스트 등 각 환경의 특수성을 고려할 수 있도록 구성된 방식으로, 스프링에서는 이를 유연하게 처리할 수 있는 여러 기능을 제공한다.

## 2\. 환경별 JAR 빌드의 문제점

[##_Image|kage@bCogbA/btsNELORPyy/HKPOEN6EJP2JEEcoieeZQk/img.png|CDM|1.3|{"originWidth":976,"originHeight":456,"style":"alignCenter"}_##]

위 이미지는 각 환경(dev, prod)에 따라 애플리케이션을 따로 빌드하는 구조를 보여준다. 예컨대 개발용 jar에는 개발용 DB 주소(dev.db.com)가 하드코딩되어 있으며, 운영용 jar에는 운영 DB 주소(prod.db.com)가 포함된다.

이 방식의 문제점은 다음과 같다.

-   빌드를 매번 환경마다 수행해야 하므로 비효율적이다.
-   동일한 코드라도 환경별 jar가 다르기 때문에 신뢰성 있는 검증이 어렵다.
-   새로운 환경이 추가될 경우, 빌드를 반복해야 하므로 유지보수가 어렵다.

## 3\. 외부 설정 주입 방식

[##_Image|kage@bSUxUr/btsNEcTsQBl/fEu1GikqrE8x019SYOKLCK/img.png|CDM|1.3|{"originWidth":856,"originHeight":448,"style":"alignCenter"}_##]

이와 대조적으로 외부 설정을 활용하면, 단 하나의 jar만 빌드한 후 모든 환경에서 동일하게 사용할 수 있다. 설정값은 서버별로 주입되며, 아래와 같은 여러 방식으로 적용이 가능하다.

| **방식** | **설명** |
| --- | --- |
| **OS 환경 변수** | OS에서 지원하는 외부 설정, 해당 OS를 사용하는 모든 프로세스에서 사용 (예: JAVA 환경변수 설) |
| **자바 시스템 속성** | 자바에서 지원하는 외부 설정, 해당 JVM안에서 사용 |
| **커맨드라인 인수** | 커맨드 라인에서 전달하는 외부 설정, 실행시 main(args) 메서드에서 사용 |
| **외부 설정 파일** | .properties, .yaml 파일로 구성 후 주입  |

## 4\. 운영체제별 환경 변수 조회 및 설정

**Windows**

-   **임시 설정:** set DB\_URL=dev.db.com
-   **영구 설정:** 제어판 → 시스템 → 고급 설정 → 환경 변수

**macOS / Linux**

-   **임시 설정:** export DB\_URL=dev.db.com
-   **영구 설정:** ~/.bashrc, ~/.zshrc 파일에 export 문 추가

**환경 변수 확인 명령어**

-   **Windows**: set
-   **macOS/Linux:** printenv, env

**환경 변수 조회 예시 화면**

[##_Image|kage@lVwBB/btsNFORavZR/Nz3Ymi9uou0cAddy53YgLk/img.png|CDM|1.3|{"originWidth":618,"originHeight":398,"style":"alignCenter","width":724,"height":466}_##]

## 5\. Java에서 환경 변수 사용하는 방법

자바에서는 System.getenv() 메서드를 통해 OS 환경 변수를 읽을 수 있다.

**예제 코드**

```
@Slf4j
public class OsEnv {

    public static void main(String[] args) {
        Map<String, String> envMap = System.getenv();

        for (String key : envMap.keySet()) {
            log.info("env {}={}", key, envMap.get(key));
        }
    }
}
```

**출력 결과**

[##_Image|kage@dJQvGa/btsNFvjVOsi/UveuKI8IHyax4HeL0Kq8rk/img.png|CDM|1.3|{"originWidth":1282,"originHeight":774,"style":"alignCenter"}_##]

## 6\. 실무 적용 사례 및 한계

가령 다음과 같이 환경마다 다른 DB 주소를 설정해야 할 경우 외부 설정은 매우 유용하게 활용된다.

| **환경** | **설정값 예시** |
| --- | --- |
| **개발 환경** | DB\_URL=dev.db.com |
| **운영 환경** | DB\_URL=prod.db.com |

이 설정만 변경하면 .jar 파일은 동일하게 유지한 채 다양한 환경에서 동작시킬 수 있다.

다만 아래와 같은 한계가 있을 수 있다.

| **항목** | **내용** |
| --- | --- |
| **전역 접근** | 모든 애플리케이션이 접근할 수 있기 때문에 민감한 정보는 외부 노출에 주의가 필요함 |
| **관리의 어려움** | 환경 변수 설정은 운영체제마다 다르므로, 프로젝트별로 체계적으로 관리하지 않으면 혼란 발생 가능 |
| **보안 측면** | API Key나 Secret Key는 환경 변수보다 더 안전한 Vault나 Secret Manager 사용이 권장됨 |