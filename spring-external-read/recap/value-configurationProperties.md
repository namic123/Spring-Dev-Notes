## SpringBoot 외부 설정(6) - @Value vs @ConfigurationProperties: 주입 방식, 불변성, 검증 전략 비교와 실무 적용 사례

블로그 : https://pjs-world.tistory.com/entry/SpringBoot-%EC%99%B8%EB%B6%80-%EC%84%A4%EC%A0%956-Value-vs-ConfigurationProperties-%EC%A3%BC%EC%9E%85-%EB%B0%A9%EC%8B%9D-%EB%B6%88%EB%B3%80%EC%84%B1-%EA%B2%80%EC%A6%9D-%EC%A0%84%EB%9E%B5-%EB%B9%84%EA%B5%90%EC%99%80-%EC%8B%A4%EB%AC%B4-%EC%A0%81%EC%9A%A9-%EC%82%AC%EB%A1%80

## 1\. @Value를 통한 외부 설정 주입

스프링 프레임워크는 외부 설정값을 자바 코드에 주입하는 다양한 방법을 제공하는데, 그중 가장 직관적이고 간단한 방식이 @Value 애노테이션이다. 이 애노테이션은 application.properties, application.yml, 환경 변수 등에서 값을 읽어올 수 있으며, Environment 추상화를 내부적으로 활용하여 작동한다.

우선, 여러 테스트를 위해 아래 properties 설정과 클래스를 생성한다.

**application.properties**

```
my.datasource.url=local.db.com
my.datasource.username=local_user
my.datasource.password=local_pw
my.datasource.etc.max-connection=1
my.datasource.etc.timeout=3500ms
my.datasource.etc.options=CACHE,ADMIN
```

#### **POJO 클래스 (MyDataSource)**

-   외부 설정값을 담는 DTO 형태의 객체
-   @PostConstruct로 값 출력

```
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;

@Slf4j
@Data
public class MyDataSource {
    private String url;
    private String username;
    private String password;
    private int maxConnection;
    private Duration timeout;
    private List<String> options;

    public MyDataSource(String url, String username, String password, int
            maxConnection, Duration timeout, List<String> options) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.maxConnection = maxConnection;
        this.timeout = timeout;
        this.options = options;
    }

    @PostConstruct
    public void init() {
        log.info("url={}", url);
        log.info("username={}", username);
        log.info("password={}", password);
        log.info("maxConnection={}", maxConnection);
        log.info("timeout={}", timeout);
        log.info("options={}", options);
    }
}
```


아래는 @Value를 활용해 외부 설정값을 읽어와서 MyDataSource 빈을 생성하는 예제이다.

**MyDataSourceValueConfig 구성 클래스**

```
import hello.datasource.MyDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Slf4j
@Configuration
public class MyDataSourceValueConfig {
    @Value("${my.datasource.url}")
    private String url;
    @Value("${my.datasource.username}")
    private String username;
    @Value("${my.datasource.password}")
    private String password;
    @Value("${my.datasource.etc.max-connection}")
    private int maxConnection;
    @Value("${my.datasource.etc.timeout}")
    private Duration timeout;
    @Value("${my.datasource.etc.options}")
    private List<String> options;

    @Bean
    public MyDataSource myDataSource1() {
        return new MyDataSource(url, username, password, maxConnection, timeout,
                options);
    }

    @Bean
    public MyDataSource myDataSource2(
            @Value("${my.datasource.url}") String url,
            @Value("${my.datasource.username}") String username,
            @Value("${my.datasource.password}") String password,
            @Value("${my.datasource.etc.max-connection}") int maxConnection,
            @Value("${my.datasource.etc.timeout}") Duration timeout,
            @Value("${my.datasource.etc.options}") List<String> options) {
        return new MyDataSource(url, username, password, maxConnection, timeout,
                options);
    }
}
```

이 클래스에서는 두 가지 방식으로 외부 설정값을 주입받는다.

1.  **필드 주입 방식 (myDataSource1)**  
    설정값을 클래스의 필드에 직접 주입받아, 빈 생성 시 해당 필드를 사용한다.
2.  **메서드 파라미터 주입 방식 (myDataSource2)**  
    빈을 생성하는 메서드의 파라미터에 직접 @Value를 붙여 주입받는다.

이처럼 @Value는 상황에 따라 필드, 생성자, 메서드 파라미터 등 다양한 위치에서 사용이 가능하다.

#### **SpringBootApplication class에 설정 클래스 등록하기**

```
@Import(MyDataSourceValueConfig.class)	// 설정 클래스 등록
@SpringBootApplication(scanBasePackages = "hello.datasource")
public class ExternalReadApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExternalReadApplication.class, args);
    }
}
```

실행 결과는 아래와 같으며, 같은 설정으로(myDataSource1, 2) 두 개의 빈이 생성되므로 로그는 두 번 출력된다.

[##_Image|kage@bH8XC1/btsNLTygF1x/7Z0SmMpOBBpwNUJTFx2ejk/img.png|CDM|1.3|{"originWidth":674,"originHeight":270,"style":"alignCenter"}_##]

## 2\. @Value 방식의 한계와 기본값 처리

@Value 방식은 단일 설정을 빠르게 주입할 수 있다는 장점이 있으나, 다음과 같은 한계가 존재한다.

-   설정 키를 문자열로 직접 입력해야 하므로 오타에 취약
-   논리적으로 연관된 설정 그룹을 관리하기 어려움
-   설정 항목이 많아질수록 코드가 장황해지고 복잡해짐

기본값을 설정하는 문법은 다음과 같다

```
@Value("${my.datasource.etc.max-connection:10}")
private int maxConnection;
```

이처럼 : 이후 값을 지정하면 해당 키가 없을 경우 기본값을 사용하게 된다.

## 3\. @ConfigurationProperties의 개념과 구조

복잡한 설정을 구조화하여 객체 단위로 다루고자 할 때는 @ConfigurationProperties를 사용하는 것이 바람직하다. 이 방식은 설정 키의 접두사(prefix)를 기준으로 계층 구조를 자바 객체로 매핑해주며, 다음과 같이 클래스를 정의할 수 있다.

#### **설정 속성 객체 생성: MyDataSourcePropertiesV1**

먼저 외부 설정의 키 구조에 맞추어 자바 클래스를 정의한다. 예컨대 my.datasource로 시작하는 설정 묶음을 표현하기 위해 다음과 같이 클래스를 구성할 수 있다.

```
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties("my.datasource") 
public class MyDataSourcePropertiesV1 {

    private String url;
    private String username;
    private String password;
    private Etc etc;

    @Data
    public static class Etc {
        private int maxConnection;
        private Duration timeout;
        private List<String> options = new ArrayList<>();
    }

}
```

이 클래스는 설정 키의 prefix가 my.datasource인 항목들을 매핑할 수 있도록 설계되었으며, 내부 클래스 Etc를 통해 하위 구조도 명확히 표현하였다. 스프링 부트는 이러한 클래스에 대해 자동으로 application.properties 값을 매핑해주는 기능을 제공한다.

#### **설정 클래스의 사용: MyDataSourceConfigV1**

```
import hello.datasource.MyDataSource;
import hello.datasource.MyDataSourcePropertiesV1;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;


@Slf4j
@EnableConfigurationProperties(MyDataSourcePropertiesV1.class)
public class MyDataSourceConfigV1 {
    private final MyDataSourcePropertiesV1 properties;

    public MyDataSourceConfigV1(MyDataSourcePropertiesV1 properties) {
        this.properties = properties;
    }

    @Bean
    public MyDataSource dataSource() {
        return new MyDataSource(
                properties.getUrl(),
                properties.getUsername(),
                properties.getPassword(),
                properties.getEtc().getMaxConnection(),
                properties.getEtc().getTimeout(),
                properties.getEtc().getOptions());
    }
}
```

위 클래스는 **@EnableConfigurationProperties**를 통해 외부 설정 객체를 스프링 컨테이너에 빈으로 등록하고, 이를 주입받아 실제 사용할 객체 MyDataSource에 값을 전달한다. 설정값은 불변 객체를 생성하는 데 사용되며, 이후에는 변경되지 않도록 관리된다

#### **설정 오류 방지: 타입 안전성 확보**

**@ConfigurationProperties**의 가장 큰 장점은 **타입 안전성**이다. 예를 들어 maxConnection은 정수형이어야 하는데, 실수로 문자열 "abc"를 입력하게 되면 스프링은 애플리케이션 로딩 시점에 다음과 같은 오류를 발생시킨다.

[##_Image|kage@nWLcQ/btsNLpLat7J/L2LYmG03S7veRxCFClxKY0/img.png|CDM|1.3|{"originWidth":1084,"originHeight":878,"style":"alignCenter"}_##]

이러한 방식은 개발자가 타입 오류를 조기에 발견할 수 있도록 돕고, 애플리케이션 실행 후 발생할 수 있는 예외를 사전에 방지해준다.

**자동 등록을 위한 대안: @ConfigurationPropertiesScan**

여러 설정 클래스를 사용할 경우, 매번 @EnableConfigurationProperties로 등록하는 대신, @ConfigurationPropertiesScan을 사용하여 지정된 패키지 내 모든 설정 객체를 자동으로 인식시킬 수 있다.

```
@Import(MyDataSourceConfigV1.class) // 설정 클래스 등록
@ConfigurationPropertiesScan // 설정 클래스 스캔
// @ConfigurationPropertiesScan("com.example.config") // 패키지 지정도 가능
@SpringBootApplication(scanBasePackages = "hello.datasource")
public class ExternalReadApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExternalReadApplication.class, args);
    }

}
```

## 4\. 생성자 기반 바인딩 방식

위에서 외부 설정 값을 주입하기 위한 방법으로 @ConfigurationProperties를 사용해왔으며, 일반적으로는 JavaBean 스타일의 **Getter/Setter 기반 방식**을 따랐다. 하지만 이 방식은 객체 생성 후에도 값이 수정될 수 있다는 단점이 있으며, 이로 인해 실수로 설정값이 변경되는 위험이 존재한다. 이러한 문제를 방지하기 위한 더욱 안전한 방법으로 **생성자 기반 바인딩 방식**을 사용할 수 있다.

#### **설정 객체 구성 – 생성자 바인딩 방식**

MyDataSourcePropertiesV2 클래스는 다음과 같이 생성자 기반으로 외부 설정을 받아들이는 구조로 정의된다.

```
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;


@Getter
@ConfigurationProperties("my.datasource")
public class MyDataSourcePropertiesV2 {

    private String url;
    private String username;
    private String password;
    private Etc etc;

    public MyDataSourcePropertiesV2(String url, String username, String password, @DefaultValue Etc etc) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.etc = etc;
    }

    @Getter
    public static class Etc {
        private int maxConnection;
        private Duration timeout;
        private List<String> options = new ArrayList<>();


        public Etc(int maxConnection, Duration timeout,  @DefaultValue("DEFAULT") List<String> options) {
            this.maxConnection = maxConnection;
            this.timeout = timeout;
            this.options = options;
        }
    }

}
```

이 구조는 **불변성(immutability)** 을 보장하며, 애플리케이션 실행 이후에는 설정 값이 변경되지 않도록 설계되어 있다. 이는 설정 값의 안정성을 극대화하며, 예측 가능한 동작을 유도하는 데 효과적이다.

-   @DefaultValue 어노테이션은 해당 설정 키가 존재하지 않을 경우 기본값을 지정하는 역할을 하며,
-   스프링 부트 3.0 이상에서는 생성자가 하나만 존재한다면 **@ConstructorBinding 어노테이션 없이도 동작**한다.

#### **설정 객체 사용 – 설정 클래스 등록**

```
import hello.datasource.MyDataSource;
import hello.datasource.MyDataSourcePropertiesV2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;


@Slf4j
@EnableConfigurationProperties(MyDataSourcePropertiesV2.class)
public class MyDataSourceConfigV2 {
    private final MyDataSourcePropertiesV2 properties;

    public MyDataSourceConfigV2(MyDataSourcePropertiesV2 properties) {
        this.properties = properties;
    }

    @Bean
    public MyDataSource dataSource() {
        return new MyDataSource(
                properties.getUrl(),
                properties.getUsername(),
                properties.getPassword(),
                properties.getEtc().getMaxConnection(),
                properties.getEtc().getTimeout(),
                properties.getEtc().getOptions());
    }
}
```

**장점**

생성자 기반의 @ConfigurationProperties 방식은 다음과 같은 장점을 갖는다.

-   설정 객체를 불변(immutable)하게 유지하여 중간에 값 변경을 차단할 수 있다.
-   타입뿐만 아니라 객체의 구조에 맞는 안전한 설정 주입을 지원한다.
-   잘못된 타입 입력 시 애플리케이션 초기 로딩에서 오류를 발생시켜 조기에 문제를 인지할 수 있다.
-   기본값 설정을 통해 필수 설정 누락 시에도 유연하게 대응할 수 있다.

## 5\. 설정 값 검증과 Bean Validation 적용

타입 안정성은 확보되었지만, 설정값의 유효성은 별도로 검증해주어야 한다. 예를 들어, 최대 커넥션 수는 1 이상이어야 하고, 타임아웃은 1초 이상이어야 한다. 이를 위해 @Validated 애노테이션과 함께 Bean Validation 어노테이션을 사용한다.

#### **의존성 추가**

스프링 부트에서 자바 빈 검증기를 사용하기 위해서는 spring-boot-starter-validation 의존성을 build.gradle 파일에 추가해야 한다.

```
implementation 'org.springframework.boot:spring-boot-starter-validation'
```

#### **설정 클래스 등록**

```
import hello.datasource.MyDataSource;
import hello.datasource.MyDataSourcePropertiesV3;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@Slf4j
@EnableConfigurationProperties(MyDataSourcePropertiesV3.class)
public class MyDataSourceConfigV3 {
    private final MyDataSourcePropertiesV3 properties;

    public MyDataSourceConfigV3(MyDataSourcePropertiesV3 properties) {
        this.properties = properties;
    }

    @Bean
    public MyDataSource dataSource() {
        return new MyDataSource(
                properties.getUrl(),
                properties.getUsername(),
                properties.getPassword(),
                properties.getEtc().getMaxConnection(),
                properties.getEtc().getTimeout(),
                properties.getEtc().getOptions());
    }
}
```

```
@Import(MyDataSourceConfigV3.class)
@ConfigurationPropertiesScan
@SpringBootApplication(scanBasePackages = "hello.datasource")
public class ExternalReadApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExternalReadApplication.class, args);
    }

}
```

#### **설정 속성 클래스에서의 검증 적용**

검증이 필요한 설정 클래스에는 @Validated 애노테이션을 적용하고, 필드에 검증용 제약 조건 애노테이션을 선언한다.

예제 클래스인 MyDataSourcePropertiesV3는 다음과 같은 검증 규칙을 포함하고 있다.

-   @NotEmpty : 문자열이 비어 있지 않아야 함 (필수 입력)
-   @Min(1) / @Max(999) : 숫자 값의 최소 및 최대 범위 제한
-   @DurationMin, @DurationMax : 시간 값의 최소/최대 제한

```
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import org.hibernate.validator.constraints.time.DurationMax;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;


@Getter
@ConfigurationProperties("my.datasource")
@Validated
public class MyDataSourcePropertiesV3 {

    @NotEmpty
    private String url;
    @NotEmpty
    private String username;
    @NotEmpty
    private String password;
    private Etc etc;

    public MyDataSourcePropertiesV3(String url, String username, String password, Etc etc) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.etc = etc;
    }

    @Getter
    public static class Etc {
        @Min(1)
        @Max(999)
        private int maxConnection;

        @DurationMin(seconds = 1)
        @DurationMax(seconds = 60)
        private Duration timeout;
        private List<String> options = new ArrayList<>();


        public Etc(int maxConnection, Duration timeout, List<String> options) {
            this.maxConnection = maxConnection;
            this.timeout = timeout;
            this.options = options;
        }
    }

}
```

이처럼 설정값이 정해진 범위를 벗어날 경우, 애플리케이션은 실행 시점에서 예외를 발생시켜 문제를 조기에 감지할 수 있도록 돕는다. 

또한, @DurationMin과 @DurationMax는 스프링이 기본적으로 사용하는 Hibernate Validator에서 제공하는 기능으로, 표준 자바 검증기에서 지원하지 않는 타입에 대한 유효성 검사를 수행할 수 있다.

아래는 설정값이 정해진 범위를 벗어난 경우의 예시이다.

[##_Image|kage@zKgL1/btsNKzgs6EG/JqXB08CxzWKcEEOtCmbv5K/img.png|CDM|1.3|{"originWidth":880,"originHeight":778,"style":"alignCenter"}_##]

## 7\. 마무리 정리

스프링에서 외부 설정값을 주입하는 방식은 상황에 따라 다양한 선택지를 제공하며, 각 방식의 특징은 다음과 같다.

| **방식** | **장점** | **단점** |
| --- | --- | --- |
| **@Value** | 간단하고 빠르게 사용 가능 | 키 직접 입력, 구조화 어려움 |
| **@ConfigurationProperties** | 계층적 구조 표현 가능, 가독성 우수 | 등록 과정 필요 |
| **생성자 바인딩** | 불변성, 안정성 확보 | 스프링 부트 2.2 이상 필요 |
| **Bean Validation** | 설정 유효성 검증 가능 | 의존성 추가 필요 |

실무에서는 설정 항목이 많고 복잡할수록 @ConfigurationProperties와 생성자 기반 바인딩, 그리고 Bean Validation을 함께 사용하는 것이 가장 안전하고 유지보수에 유리한 전략이라 할 수 있다.