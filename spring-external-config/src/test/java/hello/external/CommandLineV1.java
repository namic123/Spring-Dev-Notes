package hello.external;

import lombok.extern.slf4j.Slf4j;

/**
 * CommandLine 인수는 스페이스로 구분
 * java -jar app.jar dataA dataB -> [dataA, dataB] 2개
 * java -jar app.jar url=devdb -> [url=devdb] 1개
 * url=devdb 이라는 단어를 개발자가 직접 파싱해야 함
 **/
@Slf4j
public class CommandLineV1 {
    public static void main(String[] args) {
        for (String arg : args) {
            log.info("arg {}", arg);
        }
    }
}