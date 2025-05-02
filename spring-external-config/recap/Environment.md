## SpringBoot 외부설정(4) - 다양한 외부 설정 방법, 하나의 접근 방식 (Environment, PropertySource)

블로그 : https://pjs-world.tistory.com/entry/SpringBoot-%EC%99%B8%EB%B6%80%EC%84%A4%EC%A0%954-%EB%8B%A4%EC%96%91%ED%95%9C-%EC%99%B8%EB%B6%80-%EC%84%A4%EC%A0%95-%EB%B0%A9%EB%B2%95-%ED%95%98%EB%82%98%EC%9D%98-%EC%A0%91%EA%B7%BC-%EB%B0%A9%EC%8B%9D-Environment-PropertySource

## 1\. 외부 설정의 문제점

**외부 설정의 문제점**

Spring 애플리케이션을 운영하면서 설정값은 환경마다 자주 변경될 수밖에 없다. 그러나 설정 위치에 따라 접근 방식이 달라지는 구조는 개발자에게 지속적인 유지보수 부담을 준다. 아래 표는 설정 위치별로 접근 방식이 어떻게 달라지는지를 잘 보여준다.

| **설정 위치** | **접근 방법** |
| --- | --- |
| **OS 환경 변수** | System.getenv("key") |
| **자바 시스템 속성** | System.getProperty("key") |
| **커맨드 라인 옵션 인수** | args\[\] 또는 Spring의 ApplicationArguments |
| **application.properties 또는 .yml 파일** | 별도 로직 필요 (Spring이 기본 지원) |

결과적으로 설정 위치가 변경되면 코드에도 영향을 미치게 되며, 이는 소스 수정 → 재빌드 → 재배포의 비용을 유발한다.

해당 내용 관련해서는 위 이전 포스팅 발행글을 참고하기 바란다.

## 2\. Spring의 해결책: Environment와 PropertySource

Spring은 위와 같은 문제점을 해결하기 위해 **Environment**와 **PropertySource**라는 추상화를 도입하였다.

**PropertySource란?**

설정 값을 가져오는 실제 "출처"를 뜻하며, 아래와 같은 구현체들을 포함한다. 

| **구현체** | **설명** |
| --- | --- |
| **CommandLinePropertySource** | 커맨드라인 인수로부터 설정 읽기 |
| **SystemEnvironmentPropertySource** | OS 환경 변수로부터 읽기 |
| **PropertiesPropertySource** | .properties, .yml 파일 기반 설정 |

**Environment란?**

개발자가 설정 값을 통합적으로 조회할 수 있도록 제공되는 중앙 API이다. 설정의 출처가 무엇이든 상관없이 Environment.getProperty("key") 메서드 하나로 접근할 수 있다.

또한 Spring은 내부적으로 설정 우선순위를 정의하고 있어, 동일한 키에 대해 여러 출처에서 설정이 존재할 경우 가장 적절한 값을 반환한다.

[##_Image|kage@bplGq0/btsNJbG3c8q/TXzhKSJiTcSe0KyD1Crir0/img.png|CDM|1.3|{"originWidth":682,"originHeight":402,"style":"alignCenter"}_##]

## 3\. Environment 활용 예제

다음은 Spring에서 Environment를 이용하여 여러 방식의 외부 설정값을 읽는 간단한 컴포넌트 예시이다.

**여러 설정값을 Environment로 읽기** 

**\* 참고 : Component 애너테이션을 통해 스프링 빈에 등록하였으므로, main에서 호출할 필요없음.**

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
        String username = env.getProperty("username");
        String password = env.getProperty("password");
        log.info("env url={}", url);
        log.info("env username={}", username);
        log.info("env password={}", password);
    }
}
```

정리하자면, 운영체제의 환경 변수, 커맨드라인 옵션 인수, 자바 시스템 속성 등 어떠한 방식으로 설정 값을 전달하더라도, Environment 인터페이스는 이를 getProperty(key) 메서드를 통해 일관된 방식으로 추상화하여 제공한다. 이러한 구조 덕분에 개발자는 설정 방식의 차이를 신경 쓰지 않고, 하나의 메서드만으로 필요한 설정 값을 손쉽게 조회할 수 있다.

그렇다면, 실제로 이러한 방식이 제대로 작동하는지 확인해보도록 하겠다.

**IDE를 통해 여러 방식의 설정 값을 전달 (각 설정에 대한 자세한 설명은 이전 발행글을 참고)**

**커맨드라인 옵션 인수  
**

```
--url=devdb --username=dev_user --password=dev_pw
```

[##_Image|kage@cXcVAr/btsNK3ALpsF/CZyaSRQf31ArmDsAJi5fUk/img.png|CDM|1.3|{"originWidth":938,"originHeight":700,"style":"alignCenter"}_##]

**실행 결과**

[##_Image|kage@cCEoSF/btsNJVcpp4t/ae33YE7d8F9pZxrxwy5K01/img.png|CDM|1.3|{"originWidth":736,"originHeight":284,"style":"alignCenter"}_##]

**자바 시스템 속성**

```
-Durl=java-system-devdb -Dusername=java-system-dev_user -Dpassword=java-system-dev_pw
```

[##_Image|kage@bRpLhs/btsNLody2bb/PmEJIZQZiL87fWAKE0bHP1/img.png|CDM|1.3|{"originWidth":1096,"originHeight":632,"style":"alignCenter"}_##]

**실행 결과**

[##_Image|kage@c9KyBd/btsNKr2ZBeT/zNJOZtKukK6EEhJ9Tr6351/img.png|CDM|1.3|{"originWidth":710,"originHeight":202,"style":"alignCenter"}_##]

위의 예시에서 확인할 수 있듯이, 설정 방식에 따라 값이 정상적으로 출력되는 것을 확인할 수 있다. 그렇다면 이번에는 설정 방식이 서로 다르면서 전달되는 값이 동일한 경우, 어떤 설정 방식이 우선적으로 적용되는지 살펴보도록 하겠다.

**설정 우선순위 규칙**

Spring은 설정이 중복될 경우 다음 기준에 따라 **우선순위를 결정한다**

| **우선 기준** | **설명** |
| --- | --- |
| **더 유연한 설정이 우선** | 런타임에 쉽게 바꿀 수 있는 설정이 우선 |
| **범위가 좁은 설정이 우선** | 특정 객체나 클래스에 가까운 설정이 우선 |

**예시**

-   \--key=value (커맨드라인 옵션 인수) ➝ 범위가 가장 좁고 유연하므로 **가장 우선**
-   \-Dkey=value (자바 시스템 속성) ➝ 그다음
-   OS 환경 변수 ➝ 가장 마지막에 적용

**"좁은 범위가 우선 순위를 갖는다"란?**

위에서 말하는 범위(scope)는 해당 설정값에 접근 가능한 대상의 크기를 의미하며, **좁을수록 더 구체적이고 명시적**이므로 우선 적용된다.

| **설정 방식** | **범위** | **설명** |
| --- | --- | --- |
| **\--key=value (커맨드라인 옵션 인수)** | **매우 좁음** | 오직 해당 실행 명령어에서만 유효. 가장 명시적이므로 **최우선 적용**됨. |
| **\-Dkey=value (자바 시스템 속성)** | **JVM 수준** | 해당 JVM 프로세스 전체에서 접근 가능. 유연하지만 --보다 낮음. |
| **OS 환경 변수 (export KEY=value)** | **시스템 전역** | 모든 애플리케이션에서 공유. 가장 넓은 범위이므로 **우선순위 가장 낮음**. |

**테스트**

[##_Image|kage@uCO8R/btsNJ5e3bft/bILjLQrFwgoXaFGKSofuaK/img.png|CDM|1.3|{"originWidth":1090,"originHeight":626,"style":"alignCenter"}_##]

**실행 결과**

[##_Image|kage@cDzvsU/btsNJWCnuNk/KCBBDj9tP9kkpiDp5s4Tdk/img.png|CDM|1.3|{"originWidth":382,"originHeight":226,"style":"alignCenter"}_##]

앞서 설명한 바와 같이 커맨드라인 인수가 범위가 가장 좁기 때문에, 자바 시스템 속성보다 더 높은 우선순위로 결과값이 출력되는 것을 확인할 수 있다.

그러나 설정 값이 많아질수록 커맨드라인 인수나 시스템 속성으로 외부 설정을 관리하는 것은 비효율적이다. 따라서 Spring Boot는 설정 파일(.properties, yml)을 통해 구조화된 설정 관리를 지원한다.

해당 내용 관련해서 자세한 내용은 다음 글에서 다루도록 하겠다.