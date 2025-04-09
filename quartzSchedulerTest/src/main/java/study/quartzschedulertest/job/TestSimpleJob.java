package study.quartzschedulertest.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Service;
import study.quartzschedulertest.config.QuartzConfig;

/**
 * Test용 Quartz Job 정의 클래스.
 * {@link AbstractSchedulerJob}을 상속하여 주기적으로 실행될 Job을 구성.
 * 해당 Job은 {@link TestSimpleProcessor}를 실제 작업으로 수행하며, cron 주기로 스케줄링됨.
 *
 * @author 박재성
 * @version 1.0
 * @since 2025-03-25
 */
@Service
@Slf4j
public class TestSimpleJob extends AbstractSchedulerJob{

    /**
     * QuartzConfig를 주입받아 상위 클래스에 전달.
     *
     * @param quartzConfig Quartz 스케줄러 공통 설정
     */
    public TestSimpleJob(QuartzConfig quartzConfig) {
        super(quartzConfig);
    }

    /**
     * 실제 실행될 Job 클래스를 지정.
     *
     * @return Job 구현체 클래스
     */
    @Override
    protected Class<? extends Job> defineJobClass() {
        return TestSimpleProcessor.class;
    }

    /**
     * Job을 구분하기 위한 고유 식별자 설정.
     *
     * @return JobKey (job name과 group)
     */
    @Override
    protected JobKey defineJobKey() {
        return new JobKey("job-test-simple-batch", "simpleJob");
    }

    /**
     * Trigger를 구분하기 위한 고유 식별자 설정.
     *
     * @return TriggerKey (trigger name과 group)
     */
    @Override
    protected TriggerKey defineTriggerKey() {
        return new TriggerKey("trigger-test-simple-batch", "simpleJob");
    }

    /**
     * Cron 형식의 실행 주기 정의 (예: 매 분 10초에 실행).
     *
     * @return Cron 표현식
     */
    @Override
    protected String defineCronExpression() {
        return "10 * * * * ?";
    }

    /**
     * 상위 클래스에 정의된 scheduleJobExecution()을 호출하여 Job을 등록 및 실행.
     */
    @Override
    public void scheduleJobExecution() {
        super.scheduleJobExecution();
    }
}
