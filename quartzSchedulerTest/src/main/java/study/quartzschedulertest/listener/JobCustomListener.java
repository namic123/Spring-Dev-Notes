package study.quartzschedulertest.listener;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.springframework.stereotype.Component;

/**
 * Quartz Job 실행 시의 다양한 상태를 감지하기 위한 글로벌 JobListener 구현 클래스.
 *
 * 이 Listener는 Job 실행 전, 실행 거부, 실행 완료의 세 가지 타이밍에서 로그를 남김.
 *
 * SchedulerFactoryBean에 전역 리스너로 등록되어 모든 Job에 적용.
 *
 * @author 박재성
 * @version 1.0
 * @since 2025-03-25
 */
@Slf4j
@Component
public class JobCustomListener implements JobListener {

    /**
     * 리스너의 이름을 반환.
     *
     * @return "globalJob" 문자열
     */
    @Override
    public String getName() {
        return "globalJob";
    }

    /**
     * Job이 실행되기 직전에 호출됨.
     * JobKey를 로그에 출력.
     *
     * @param context 현재 실행될 Job의 실행 컨텍스트
     */
    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        JobKey jobKey = context.getJobDetail().getKey();
        log.info("jobToBeExecuted :: jobkey : {}", jobKey);
    }

    /**
     * Job 실행이 취소되었을 때 호출.
     * (TriggerListener의 vetoJobExecution이 true를 반환한 경우)
     *
     * @param context 취소된 Job의 실행 컨텍스트
     */
    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        JobKey jobKey = context.getJobDetail().getKey();
        log.info("jobExecutionVetoed :: jobkey : {}", jobKey);
    }

    /**
     * Job이 정상 실행을 마쳤을 때 호출.
     * 예외 발생 여부와 관계없이 실행 완료 시 호출.
     *
     * @param context       실행된 Job의 컨텍스트
     * @param jobException  Job 실행 중 발생한 예외 (없을 경우 null)
     */
    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        JobKey jobKey = context.getJobDetail().getKey();
        log.info("jobWasExecuted :: jobkey : {}", jobKey);
    }
}
