package study.quartzschedulertest;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import study.quartzschedulertest.job.TestSimpleJob;

/**
 * Spring Boot 애플리케이션이 시작될 때 실행되는 Runner 클래스.
 * {@link TestSimpleJob}의 scheduleJobExecution()을 호출하여 Quartz Job 등록 및 실행을 트리거함.
 *
 * @author 박재성
 * @version 1.0
 * @since 2025-03-25
 */
@Component
@AllArgsConstructor
@Slf4j
public class QuartzSchedulerTestApplicationRunner implements ApplicationRunner {
    /** Quartz Job 등록을 위한 테스트용 Job 클래스 의존성 주입 */
    TestSimpleJob testSimpleJob;

    /**
     * Spring Boot 애플리케이션이 실행되면 자동 호출됨.
     * 해당 메서드에서 Quartz Job을 스케줄링.
     *
     * @param args 애플리케이션 실행 시 전달된 인자
     * @throws Exception 예외 발생 시 호출자에게 전파
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        testSimpleJob.scheduleJobExecution();
    }
}
