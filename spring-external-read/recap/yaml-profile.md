<h2 id="yaml-overview" style="border-bottom: 2px solid #ddd; padding-bottom: 5px; margin-top: 30px;">1. YAML을 이용한 외부 설정 관리</h2>
스프링 부트에서는 전통적인 application.properties 외에도 application.yml 또는 application.yaml 형식의 파일을 통한 설정 방식을 공식적으로 지원한다. 특히 실무에서는 가독성과 계층적 구조 표현에 용이한 application.yml 파일이 널리 활용된다.

YAML(YAML Ain't Markup Language)은 들여쓰기를 기반으로 한 구조적 설정 언어로, XML이나 JSON보다 간결하고 직관적인 문법을 제공한다. 예를 들어, 다음과 같은 properties 설정은:

properties
복사
편집
environments.dev.url=https://dev.example.com
environments.dev.name=Developer Setup
YAML에서는 다음과 같이 표현된다:

yaml
복사
편집
environments:
dev:
url: "https://dev.example.com"
name: "Developer Setup"
스프링 부트는 내부적으로 이와 같은 계층 구조를 flatten(평탄화)하여 처리하며, key-value 구조로 변환하여 @Value 또는 @ConfigurationProperties를 통한 주입과 호환되도록 설계되어 있다.

<h2 id="yaml-profile" style="border-bottom: 2px solid #ddd; padding-bottom: 5px; margin-top: 30px;">2. YAML에서의 프로필 기반 설정 분리</h2>
환경에 따라 설정값이 달라지는 경우, YAML은 --- 구분자를 이용하여 하나의 파일 내에서 여러 프로필의 설정을 정의할 수 있도록 지원한다. 예를 들어, 개발(dev)과 운영(prod) 환경을 다음과 같이 구성할 수 있다:

yaml
복사
편집
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
실행 시에는 다음과 같이 활성화할 프로필을 지정하면 된다:

bash
복사
편집
--spring.profiles.active=dev
또는

bash
복사
편집
-Dspring.profiles.active=dev
이와 같이 하나의 YAML 파일 내에서 환경별 설정을 구조적으로 관리할 수 있다는 점은 유지보수성과 가독성 측면에서 큰 장점으로 작용한다.

<h2 id="profile-bean" style="border-bottom: 2px solid #ddd; padding-bottom: 5px; margin-top: 30px;">3. @Profile을 이용한 Bean 등록 분기</h2>
단순히 설정값이 아닌, 환경별로 서로 다른 구현체(Bean)를 등록해야 하는 경우도 존재한다. 예컨대 로컬에서는 테스트용 결제 모듈을, 운영 환경에서는 실제 결제 모듈을 사용하고자 할 수 있다. 이때 스프링은 @Profile 애노테이션을 제공하여, 프로필에 따라 빈의 등록 여부를 조건부로 제어할 수 있도록 한다.

다음은 결제 클라이언트 인터페이스와 각 환경에 따른 구현체 예시이다:

java
복사
편집
public interface PayClient {
void pay(int money);
}
로컬용 구현체:

java
복사
편집
@Profile("default")
public class LocalPayClient implements PayClient {
public void pay(int money) {
log.info("로컬 결제 money={}", money);
}
}
운영용 구현체:

java
복사
편집
@Profile("prod")
public class ProdPayClient implements PayClient {
public void pay(int money) {
log.info("운영 결제 money={}", money);
}
}
설정 클래스에서는 다음과 같이 환경별로 Bean을 등록할 수 있다:

java
복사
편집
@Configuration
public class PayConfig {
@Bean
@Profile("default")
public PayClient localPayClient() {
return new LocalPayClient();
}

    @Bean
    @Profile("prod")
    public PayClient prodPayClient() {
        return new ProdPayClient();
    }
}
이 구조는 @Conditional 메커니즘을 내부적으로 활용하며, 설정 파일과 달리 실행 시 로직의 분기 자체가 달라지는 구조를 반영할 수 있다는 점에서 매우 유용하다.

<h2 id="summary" style="border-bottom: 2px solid #ddd; padding-bottom: 5px; margin-top: 30px;">4. 마무리 정리</h2>
스프링 부트에서는 application.yml을 통해 복잡한 설정 구조를 명확하게 표현할 수 있으며, --- 구분자를 통해 프로필별 설정 분리를 지원한다. 또한 @Profile을 이용하면 환경에 따라 전혀 다른 Bean을 유연하게 구성할 수 있다.

이러한 외부 설정 전략은 다음과 같은 장점을 제공한다:

설정 파일을 통해 환경에 따라 유연하게 대응 가능

코드에 하드코딩된 설정 제거로 보안성과 유지보수성 향상

Bean 등록 조건을 통해 로직 자체를 환경별로 분기 가능

테스트 환경, 로컬 개발, 운영 환경 간의 명확한 역할 분리 가능

실무에서는 위의 기능들을 적절히 조합하여 사용하는 것이 중요하며, 특히 @ConfigurationProperties와 함께 사용할 경우 설정 객체를 더욱 타입 안정적으로 구성할 수 있다는 점도 기억해둘 필요가 있다.