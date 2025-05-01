## SpringBoot 외부 설정(3) - 커맨드 라인 옵션 인수 활용 방법

블로그: https://pjs-world.tistory.com/entry/SpringBoot-%EC%99%B8%EB%B6%80-%EC%84%A4%EC%A0%953-%EC%BB%A4%EB%A7%A8%EB%93%9C-%EB%9D%BC%EC%9D%B8-%EC%98%B5%EC%85%98-%EC%9D%B8%EC%88%98-%ED%99%9C%EC%9A%A9-%EB%B0%A9%EB%B2%95

## 1\. 커맨드 라인 인수란 무엇인가?

Java에서의 커맨드 라인 인수(Command Line Arguments)는 main(String\[\] args) 메서드로 전달되는 외부 입력값을 의미한다. 일반적으로 프로그램을 실행할 때 아래와 같이 인수를 넘겨줄 수 있다.

**사용 개념**

자바 애플리케이션을 실행할 때 다음과 같이 인수를 전달한다.

```
java -jar app.jar dataA dataB
```

-   위 명령에서 dataA, dataB는 각각 args\[0\], args\[1\]로 전달된다.
-   중요한 점은 이 값들이 **문자열로만 전달**되며, 구조화되지 않는다. 즉, key=value 형태로 입력해도 단지 하나의 문자열로 간주된다.

아래는 IDE를 통해 인수를 전달하고 조회하는 방법이다.

**IDE를 통해 인수 전달**

**상단 우측 > 메뉴 > Edit > Run Debug Configuration**  

[##_Image|kage@wLVfW/btsNFUMvboC/l67pguL0Le0mAyGczWbmD0/img.png|CDM|1.3|{"originWidth":312,"originHeight":64,"style":"alignCenter"}_##]

**Program Arguments**에 전달할 인수를 입력 (띄워쓰기로 각 인수를 구분할 수 있다.) 

[##_Image|kage@cuZlwN/btsNFWcrq4W/zHOvXfi4e7pC5TnlKfotH1/img.png|CDM|1.3|{"originWidth":1158,"originHeight":626,"style":"alignCenter"}_##]

**예제 코드 (CommandLineV1) - test 패키지 하위 생성**

[##_Image|kage@sL07T/btsNHPvTcfH/fws5WK1XZsxjRutIIme5rk/img.png|CDM|1.3|{"originWidth":390,"originHeight":216,"style":"alignCenter"}_##]

```
@Slf4j
public class CommandLineV1 {
 public static void main(String[] args) {
   for (String arg : args) {
     log.info("arg {}", arg);
   }
 }
}
```

**테스트 결과** 

[##_Image|kage@bw77ya/btsNGZTzCsT/S4vly3PhUvQiLYC65k53s0/img.png|CDM|1.3|{"originWidth":692,"originHeight":418,"style":"alignCenter"}_##]

## 2\. 일반 커맨드 인수의 한계

단순 문자열 배열로 전달되기 때문에 구조화된 데이터가 아니며, key-value 구조도 제공하지 않는다. 예컨대 다음과 같은 인수 전달은,

```
java -jar app.jar url=devdb username=dev_user
```

개발자가 직접 =를 기준으로 문자열을 파싱하지 않는 이상, url=devdb 전체가 하나의 문자열로 처리된다. 유지보수나 확장성 측면에서는 다소 번거로울 수밖에 없다.

테스트를 해보자

**입력 인수**

```
url=devdb username=dev_user password=dev_pw
```

[##_Image|kage@G5zZR/btsNFUsaf7B/WQHzMbSJZ1ZpXGq2H3WReK/img.png|CDM|1.3|{"originWidth":1156,"originHeight":626,"style":"alignCenter"}_##]

**실행 결과**

[##_Image|kage@6Ksgq/btsNIA53nAe/xIZKzt0b673hiR15I11jBk/img.png|CDM|1.3|{"originWidth":672,"originHeight":366,"style":"alignCenter"}_##]

이 값들은 단순 문자열이다. url=devdb 자체가 문자열 "url=devdb"로 들어온다. 즉, 자동으로 파싱되거나 Map<String, String>으로 변환되지는 않기 때문에 개발자가 직접 파싱 로직을 만들어야 한다.

**정리**

-   단순 문자열 나열이기 때문에 데이터 구조가 없다.
-   \= 기호를 기준으로 직접 key와 value를 분리해야 하며,
-   반복문을 돌며 원하는 키를 찾아야 하므로 유지보수가 어렵다.

## 3\. 스프링 전용 커맨드 옵션 인수란?

Spring Boot는 이러한 단점을 극복하기 위해 --key=value 형식의 옵션 인수를 자동으로 파싱해주는 기능을 제공한다. 이를 커맨드라인 옵션 인수(**Command Line Option Arguments)**라 부른다.

즉, 스프링 프레임워크는 애플리케이션을 실행할 때 전달되는 **외부 설정값**을 여러 방법으로 받을 수 있도록 지원한다. 그 중에서도 **커맨드 라인 옵션 인수**는 --key=value 형식으로 전달하는 방식이며, 일반적인 args 배열과 달리 Spring Boot에서 **자동 파싱**을 통해 매우 편리하게 활용할 수 있다.

**일반적인 커맨드 라인 인수와 커맨드 라인 인수 옵션의 차이**

앞서 설명했듯, 기본 커맨드 라인 인수는 단순히 공백으로 구분된 문자열 배열에 불과하다. 이 값들은 어떠한 형식도 강제하지 않으며, 모든 처리는 개발자가 직접 구현해야 한다.

**일반 커맨드 라인 인수의 특징**

-   단순히 공백(space) 기준으로 문자열을 분리하여 String\[\] args 배열로 전달된다.
-   key=value 형식도 단지 문자열일 뿐, 특별한 의미가 없다.
-   설정값을 사용할 경우 개발자가 직접 파싱 로직을 작성해야 한다.

**커맨드 라인 옵션 인수의 특징 (Spring Boot 전용)**

-   \--key=value 형식으로 입력해야 한다.
-   Spring Boot가 자동으로 파싱하여 ApplicationArguments 빈으로 주입해준다.
-   동일한 키에 대해 복수의 값을 받을 수 있으며, List<String>으로 조회할 수 있다.
-   \--로 시작하지 않은 인수는 옵션 인수로 인식되지 않는다.
-   자바 표준 기능이 아니며, **Spring Boot에서만 제공하는 기능**이다.

**예시**

```
java -jar app.jar --url=devdb --username=dev_user --password=dev_pw
```

이와 같이 --로 시작하는 인수는 **스프링 내부에서 구조화된 설정 값**으로 처리된다. 

커맨드 라인 옵션 활용하여, 테스트를 해보자

**IDE - 커맨드라인 옵션 인수 전달 값** 

구분을 위해 --(대시)가 없는 mode=on 옵션도 추가

```
--url=devdb --username=dev_user --password=dev_pw mode=on
```

[##_Image|kage@0R2Ys/btsNGhHseAG/JhgVoVP8SN9FiUkf7rWECK/img.png|CDM|1.3|{"originWidth":1156,"originHeight":628,"style":"alignCenter"}_##]

**CommandLineV2 예제 - test 패키지 하위 생성**

[##_Image|kage@sQfAq/btsNGBr72Fl/QDsXFd61bdOZrkTl1od8E1/img.png|CDM|1.3|{"originWidth":390,"originHeight":216,"style":"alignCenter"}_##]

```
@Slf4j
public class CommandLineV2 {
    public static void main(String[] args) {

        // 전달받은 커맨드 라인 인수를 그대로 출력
        for (String arg : args) {
            log.info("arg {}", arg);
        }

        // ApplicationArguments 객체 생성
        // args 배열을 기반으로 옵션 인수 및 일반 인수를 구조화
        ApplicationArguments appArgs = new DefaultApplicationArguments(args);

        // 전체 원시 인수 목록 출력
        log.info("SourceArgs = {}", List.of(appArgs.getSourceArgs()));

        // '--' 없이 전달된 일반 인수 목록 출력 (옵션 아님)
        log.info("NonOptionArgs = {}", appArgs.getNonOptionArgs());

        // '--key=value' 형태로 전달된 옵션 키 목록 출력
        log.info("OptionNames = {}", appArgs.getOptionNames());

        // 모든 옵션 키에 대해 값 리스트 출력
        Set<String> optionNames = appArgs.getOptionNames();
        for (String optionName : optionNames) {
            log.info("option args {}={}", optionName, appArgs.getOptionValues(optionName));
        }

        // 자주 사용하는 옵션 키 값 개별 조회
        List<String> url = appArgs.getOptionValues("url");
        List<String> username = appArgs.getOptionValues("username");
        List<String> password = appArgs.getOptionValues("password");
        List<String> mode = appArgs.getOptionValues("mode"); // mode는 -- 없이 전달되면 null

        // 개별 키에 대한 값 출력
        log.info("url={}", url);
        log.info("username={}", username);
        log.info("password={}", password);
        log.info("mode={}", mode); // null일 수 있음
    }
}
```

**테스트 결과** 

[##_Image|kage@cxK2RK/btsNGghxcgE/LcJ3PTcYwB71fKIahNNxoK/img.png|CDM|1.3|{"originWidth":1090,"originHeight":324,"style":"alignCenter"}_##]

**주요 기능**

| **메서드** | **설명** |
| --- | --- |
| **getSourceArgs()** | 전달된 인수 전체를 배열 그대로 반환 |
| **getOptionNames()** | \--key=value 형식의 key만 추출하여 Set으로 반환 |
| **getOptionValues(String name)** | 해당 key에 대한 모든 value를 List로 반환 (중복 key 지원) |
| **getNonOptionArgs()** | \-- 없이 전달된 일반 인수 목록 반환 |

## 4\. 커맨드 라인 옵션 인수를 ApplicationArguments 빈으로 활용하는 법

Spring Boot는 커맨드 라인에서 전달된 인수를 자동으로 분석하여 **ApplicationArguments**라는 스프링 빈으로 등록해 둔다. 이 기능을 활용하면 **어떤 클래스에서든 손쉽게 커맨드 라인 옵션 인수에 접근**할 수 있다. 특히 --key=value 형태로 전달된 값들을 구조화된 형태로 사용할 수 있어 설정 관리가 매우 효율적이다.

**핵심 개념 정리**

| **항목** | **설명** |
| --- | --- |
| **ApplicationArguments** | 커맨드 라인 인수 전체를 관리하는 Spring Boot 제공 인터페이스 |
| **빈 등록** | ApplicationArguments는 스프링이 자동으로 빈으로 등록해준다 |
| **의존성 주입** | 생성자 또는 필드 주입을 통해 어디서든 사용할 수 있다 |
| 핵심 **기능** | getSourceArgs(), getOptionNames(), getOptionValues(String name), getNonOptionArgs() 등 제공 |

자 이제 테스트를 진행해보자. 전달할 커맨드 라인 옵션 인수는 앞서 전달한 인수 그대로 사용한다.

```
--url=devdb --username=dev_user --password=dev_pw mode=on
```

**참고로 스프링 빈으로 등록하여 옵션 인수에 접근하는 것이므로, src/main 하위에 클래스를 생성해야하며, main에서 호출할 필요 없음.**

[##_Image|kage@k04Io/btsNGBMqfQ2/vtePQZp0y6HsjoqGSBdsk1/img.png|CDM|1.3|{"originWidth":390,"originHeight":180,"style":"alignCenter"}_##]

**예제 코드 분석: CommandLineBean**

```
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Component // 스프링 빈으로 등록되는 클래스 (컴포넌트 스캔 대상)
public class CommandLineBean {

    private final ApplicationArguments arguments; // 커맨드 라인 옵션 인수를 담고 있는 객체

    @PostConstruct // 의존성 주입 완료 후 초기화 시점에 자동 호출되는 메서드
    public void init() {

        // 애플리케이션 실행 시 전달된 원본 커맨드 라인 인수들을 리스트 형태로 출력
        log.info("source {}", List.of(arguments.getSourceArgs()));

        // 전달된 옵션 인수의 키 목록 출력 (--key=value 형식의 key들)
        log.info("optionNames {}", arguments.getOptionNames());

        // 모든 옵션 이름을 순회하면서 key에 대한 값(value) 목록을 출력
        Set<String> optionNames = arguments.getOptionNames();
        for (String optionName : optionNames) {
            // 각 옵션 인수의 key와 그에 대한 값 리스트 출력
            log.info("option args {}={}", optionName, arguments.getOptionValues(optionName));
        }
    }
}
```

**실행 결과**

[##_Image|kage@59PV6/btsNGLaejjA/TwNHscyzeGJlcEDVyvONdK/img.png|CDM|1.3|{"originWidth":580,"originHeight":92,"style":"alignCenter"}_##]

**추가 사항**

**\--key=value는 하나의 키에 여러 값도 입력할 수 있다.**

```
--url=devdb --url=devdb2 --username=dev_user --password=dev_pw mode=on
```

**실행 결과** 

[##_Image|kage@cudGtz/btsNHsAVS8F/b1k4XsHvKudCtKM3Q0Xpy1/img.png|CDM|1.3|{"originWidth":330,"originHeight":104,"style":"alignCenter"}_##]

## 5\. 실무 활용 팁과 주의사항

| **항목** | **설명** |
| --- | --- |
| **자동 빈 등록** | ApplicationArguments는 별도 설정 없이 스프링이 자동으로 관리 |
| **유연한 파싱** | 다중 값(--url=devdb --url=devdb2)도 리스트로 파싱됨 |
| **인수 명확성** | 반드시 --key=value 형식을 준수해야 옵션으로 인식됨 |
| **혼합 사용 주의** | 일반 인수(mode=on)는 별도로 관리되며 getOptionValues로 접근 불가 |

따라서 복잡하거나 다수의 설정이 필요한 경우, 단순 args\[\]보다 Spring Boot의 커맨드 옵션 방식을 적극 활용하는 것이 바람직하다.

## 6\. 요약

Spring Boot에서는 커맨드 라인 인수를 통해 애플리케이션 설정을 외부에서 손쉽게 주입할 수 있다. 특히 --key=value 형식의 옵션 인수를 활용하면 구조화된 설정 관리가 가능하며, 운영환경에 따라 유연한 설정 주입이 가능하다.

| **구분** | **설명** |
| --- | --- |
| **일반 인수** | 단순 문자열로만 전달됨 (args\[\]) |
| **옵션 인수** | \--key=value 형식, Spring이 자동 파싱 |
| **접근 방식** | ApplicationArguments.getOptionValues("key") |
| **실무 장점** | CI/CD 자동화, 설정 분리, 환경 유연성 확보 |