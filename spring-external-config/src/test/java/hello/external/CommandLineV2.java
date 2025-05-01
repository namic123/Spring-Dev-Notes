package hello.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;

import java.util.List;
import java.util.Set;

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