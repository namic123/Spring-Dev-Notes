package study.quartzschedulertest.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

/**
 * 실제 Quartz Job 로직을 수행하는 클래스.
 * {@link TestSimpleJob}에 의해 주기적으로 실행되며,
 * 콘솔 메시지를 출력하는 단순 테스트 용도.
 *
 * @author 박재성
 * @version 1.0
 * @since 2025-03-25
 */
@Component
@Slf4j
public class TestSimpleProcessor implements Job {

    /**
     * Quartz 스케줄러에 의해 실행될 작업 로직.
     *
     * @param jobExecutionContext 실행 컨텍스트 정보
     * @throws JobExecutionException 실행 중 예외 발생 시 던짐
     */
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        System.out.println("TestSimpleProcessor 실행중! 테스트 잘되감");
    }
}
