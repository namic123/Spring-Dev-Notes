## SpringBoot 외부 설정(7) - YAML 프로필 구성과 @Profile 기반 환경별 빈 등록 전략

블로그 : https://pjs-world.tistory.com/entry/SpringBoot-%EC%99%B8%EB%B6%80-%EC%84%A4%EC%A0%957-YAML-%ED%94%84%EB%A1%9C%ED%95%84-%EA%B5%AC%EC%84%B1%EA%B3%BC-Profile-%EA%B8%B0%EB%B0%98-%ED%99%98%EA%B2%BD%EB%B3%84-%EB%B9%88-%EB%93%B1%EB%A1%9D-%EC%A0%84%EB%9E%B5

## 1\. YAML을 이용한 외부 설정 관리

YAML은 "YAML Ain't Markup Language"의 약자로, **사람이 읽기 좋은 계층형 데이터 표현**을 지향하는 설정 언어입니다. 구조가 들여쓰기 기반이기 때문에, XML이나 JSON보다 **가독성이 뛰어나고 간결한 문법**을 가지고 있다.

예를 들어, 다음과 같은 properties 설정은

```
environments.dev.url=https://dev.example.com
environments.dev.name=Developer Setup
```

YAML에서는 다음과 같이 표현된다

```
environments:
  dev:
    url: "https://dev.example.com"
    name: "Developer Setup"
```

스프링 부트는 내부적으로 이와 같은 계층 구조를 flatten(평탄화)하여 처리하며, key-value 구조로 변환하여 @Value 또는 @ConfigurationProperties를 통한 주입과 호환되도록 설계되어 있다.

#### **우선순위 주의사항**

-   application.properties와 application.yml 파일이 **동시에 존재**할 경우,  
    → application.properties가 우선 적용된다.
-   따라서 둘 중 **하나만 사용하는 것이 일관성** 있고 유지보수에도 좋다.

## 2\. YAML에서의 프로필 기반 설정 분리

환경에 따라 설정값이 달라지는 경우, YAML은 --- 구분자를 이용하여 하나의 파일 내에서 여러 프로필의 설정을 정의할 수 있도록 지원한다. 예를 들어, 개발(dev)과 운영(prod) 환경을 다음과 같이 구성할 수 있다

```
my:
  datasource:
    url: local.db.com
    username: local_user

---
spring:
  config:
    activate:
      on-profile: dev
my:
  datasource:
    url: dev.db.com
    username: dev_user

---
spring:
  config:
    activate:
      on-profile: prod
my:
  datasource:
    url: prod.db.com
    username: prod_user
```

실행 시에는 다음과 같이 활성화할 프로필을 지정하면 된다

**커맨드 라인 옵션 인수**

```
--spring.profiles.active=dev
```

**JVM 속성** 

```
-Dspring.profiles.active=dev
```

## 3\. @Profile을 이용한 Bean 등록 분기

앞서 설명한 바와 같이, 스프링 부트에서는 application.properties 또는 application.yml 파일을 이용하여 외부 설정값을 환경별로 분리할 수 있는 메커니즘을 이미 제공하고 있다. 그러나 단순히 값만 다른 것이 아니라, **환경에 따라 아예 다른 종류의 빈(Bean)을 등록해야 하는 경우**도 존재한다. 예컨대, 로컬 개발 환경에서는 결제 기능이 실제로 동작하면 안 되기 때문에 **가짜 결제 빈**을 등록하고, 운영 환경에서는 **실제 결제를 수행하는 빈**을 등록해야 하는 상황이 있을 수 있다

#### **예제 코드**

**구조 및 인터페이스 정의**

먼저, 결제 기능을 위한 공통 인터페이스를 정의한다.

```
public interface PayClient {
    void pay(int money);
}
```

그 후, 각각의 환경에 맞는 구현체를 만든다.

-   **로컬 환경용 가짜 결제 클라이언트**

```
@Slf4j
public class LocalPayClient implements PayClient {
    @Override
    public void pay(int money) {
        log.info("로컬 결제 money={}", money);
    }
}
```

-   **운영 환경용 실제 결제 클라이언트**

```
@Slf4j
public class ProdPayClient implements PayClient {
    @Override
    public void pay(int money) {
        log.info("운영 결제 money={}", money);
    }
}
```
#### **결제 서비스를 사용하는 비즈니스 로직**
PayClient를 주입받는 OrderService는 해당 구현체가 어떤 환경에 따라 달라지더라도 **인터페이스만 사용**하므로 유연하게 동작한다.
```
@Service
@RequiredArgsConstructor
public class OrderService {
    private final PayClient payClient;

    public void order(int money) {
        payClient.pay(money);
    }
}
```

#### **설정 클래스에서 @Profile 활용**

이제 스프링 설정 클래스에서 @Bean 메서드에 @Profile을 붙여, 환경별로 등록할 빈을 다르게 설정할 수 있다.

```
@Configuration
public class PayConfig {
    @Bean
    @Profile("default")	// 로컬용 프로필 사용
    public LocalPayClient localPayClient() {
        log.info("LocalPayClient 빈 등록");
        return new LocalPayClient();
    }

    @Bean
    @Profile("prod")  // 운영용 프로필 사용
    public ProdPayClient prodPayClient() {
        log.info("ProdPayClient 빈 등록");
        return new ProdPayClient();
    }
}
```

-   default 프로필: **명시적인 프로필이 지정되지 않았을 때** 기본으로 적용된다.
-   prod 프로필: **운영 환경에서 명시적으로 활성화해야 적용**된다.

#### **실행 시 프로필 적용 결과**

**테스트를 위한 YML은 위 YML 예시 코드를 사용**

**만약 위 로직이 정상적으로 동작하지 않을 경우, 애플리케이션 클래스에 해당 패키지 scan 대상에 포함할것**

```
@Import(MyDataSourceConfigV3.class)
@ConfigurationPropertiesScan
@SpringBootApplication(scanBasePackages = {"hello.datasource", "hello.pay"}) // 패키지 스캔 대상
public class ExternalReadApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExternalReadApplication.class, args);
    }

}
```

-   **기본(default) 프로필로 실행한 경우 (즉, 옵션 인수를 따로 주지 않은 경우)**

[##_Image|kage@Q6Xo3/btsNTE99ufz/JZNTkcgbBVgX49dLV9eIoK/img.png|CDM|1.3|{"originWidth":1870,"originHeight":230,"style":"alignCenter"}_##]

-   **prod 프로필로 실행한 경우**

```
--spring.profiles.active=prod
```

[##_Image|kage@lc2TV/btsNUeb1E47/B5R4Gmdl1FgkY3gU6yCo50/img.png|CDM|1.3|{"originWidth":1592,"originHeight":1304,"style":"alignCenter"}_##][##_Image|kage@b0nJ9F/btsNULf5vK5/PPOFU3J1vKnjrDLq7ayNQk/img.png|CDM|1.3|{"originWidth":2186,"originHeight":518,"style":"alignCenter"}_##]

#### **@Profile의 내부 구조**

```
@Conditional(ProfileCondition.class)
public @interface Profile {
    String[] value();
}
```

실제로 @Profile은 내부적으로 스프링의 @Conditional을 활용한다. 즉, 주어진 프로필 조건이 만족될 때만 해당 빈이 등록되도록 제어하는 것이다. 이는 조건부 설정의 확장으로 볼 수 있으며, 개발자가 보다 **명확하고 유연하게 환경에 따른 설정을 구성**할 수 있게 해준다.

## 4\. 마무리 정리

스프링 부트에서는 application.yml을 통해 복잡한 설정 구조를 명확하게 표현할 수 있으며, --- 구분자를 통해 프로필별 설정 분리를 지원한다. 또한 @Profile을 이용하면 환경에 따라 전혀 다른 Bean을 유연하게 구성할 수 있다.

이러한 외부 설정 전략은 다음과 같은 장점을 제공한다

-   설정 파일을 통해 환경에 따라 유연하게 대응 가능
-   코드에 하드코딩된 설정 제거로 보안성과 유지보수성 향상
-   Bean 등록 조건을 통해 로직 자체를 환경별로 분기 가능
-   테스트 환경, 로컬 개발, 운영 환경 간의 명확한 역할 분리 가능

실무에서는 위의 기능들을 적절히 조합하여 사용하는 것이 중요하며, 특히 @ConfigurationProperties와 함께 사용할 경우 설정 객체를 더욱 타입 안정적으로 구성할 수 있다는 점도 기억해둘 필요가 있다