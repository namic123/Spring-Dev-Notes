package security.springsecuritybasic.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Callable;

/**
 * 필터 동작 테스트를 위한 컨트롤러.
 * <p>
 * /testfilterbefore → forward → /testfilterafter로 내부 이동하여 필터 실행 여부 확인 가능.
 * </p>
 *
 * @author 박재성
 * @version 1.0
 * @since 2025-04-10
 */
@RestController
public class MainController {

    /**
     * /testfilterbefore 경로로 들어오면 내부적으로 /testfilterafter로 forward한다.
     *
     * @return forward 명령 문자열
     */
    @GetMapping("/testfilterbefore")
    public String before(){
        return "forward:/testfilterafter";
    }


    /**
     * /testfilterafter 요청에 대해 문자열 응답을 반환한다.
     *
     * @return 단순 문자열 응답
     */
    @GetMapping("testfilterafter")
    public String after(){
        return "hello world";
    }


    @GetMapping("/async")
    @ResponseBody
    public Callable<String> asyncPage() {
        // 현재 쓰레드에서 인증 정보 확인
        System.out.println("start " + SecurityContextHolder.getContext().getAuthentication().getName());

        // 비동기 쓰레드에서 수행될 로직
        return () -> {
            Thread.sleep(4000);
            // 새로운 쓰레드에서도 동일한 SecurityContext 접근 가능
            System.out.println("end " + SecurityContextHolder.getContext().getAuthentication().getName());

            return "async";
        };
    }
    @GetMapping
    public void headerTest() {

    }
}
