package hello;

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
