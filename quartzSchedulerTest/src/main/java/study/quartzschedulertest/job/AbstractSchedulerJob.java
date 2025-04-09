package study.quartzschedulertest.job;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import study.quartzschedulertest.config.QuartzConfig;

/**
 * Quartz Job 등록을 위한 추상 클래스.
 * Job 클래스, JobKey, TriggerKey, Cron 표현식 등을 하위 클래스에서 정의하면,
 * 공통된 로직으로 스케줄 등록을 수행할 수 있도록 구성.
 *
 * @author 박재성
 * @version 1.0
 * @since 2025-03-25
 */
@Slf4j
@AllArgsConstructor
public abstract class AbstractSchedulerJob {

    // Quartz 설정 클래스 (SchedulerFactoryBean 접근용)
    QuartzConfig quartzConfig;

    /**
     * 실행할 Job 클래스를 정의하는 추상 메서드.
     *
     * @return Job 클래스 (Job 인터페이스를 구현한 클래스)
     */
    protected abstract Class<? extends Job> defineJobClass();

    /**
     * Job을 식별하기 위한 Key를 정의하는 추상 메서드.
     *
     * @return JobKey (jobName, jobGroup 으로 구성)
     */
    protected abstract JobKey defineJobKey();

    /**
     * Trigger를 식별하기 위한 Key를 정의하는 추상 메서드.
     *
     * @return TriggerKey (triggerName, triggerGroup 으로 구성)
     */
    protected abstract TriggerKey defineTriggerKey();

    /**
     * Job의 실행 주기를 정의하는 Cron 표현식을 반환하는 추상 메서드.
     *
     * @return Cron 문자열
     */
    protected abstract String defineCronExpression();

    /**
     * Job과 Trigger를 Quartz Scheduler에 등록하고 실행.
     * 기존 동일 JobKey/TriggerKey가 존재하면 삭제 후 재등록.
     */
    public void scheduleJobExecution() {
        try {
            // SchedulerFactoryBean에서 Scheduler 객체 획득
            Scheduler scheduler = quartzConfig.schedulerFactoryBean().getScheduler();

            JobKey jobKey = defineJobKey();
            TriggerKey triggerKey = defineTriggerKey();

            // 동일한 JobKey가 존재하면 이전 Job과 Trigger를 삭제
            if (scheduler.checkExists(jobKey)) {
                scheduler.unscheduleJob(triggerKey); // 트리거 제거
                scheduler.deleteJob(jobKey);         // 잡 제거
            }

            // JobDetail 및 CronTrigger 생성
            JobDetail jobDetail = buildJobDetail(defineJobClass(), jobKey);
            CronTrigger cronTrigger = configureTrigger(triggerKey, defineCronExpression());

            // Quartz에 Job 등록
            scheduler.scheduleJob(jobDetail, cronTrigger);

            // Scheduler 시작 (start가 호출되지 않으면 Job이 동작하지 않음)
            scheduler.start();
        } catch (SchedulerException e) {
            // 예외 발생 시 로그 출력
            log.error("#### AbstractSchedulerJob >> scheduleJobExecution >> error :: {} ####", e.toString());
        }
    }

    /**
     * JobDetail 생성 유틸 메서드.
     *
     * @param jobClass 실행할 Job 클래스
     * @param jobKey Job 식별자
     * @return JobDetail 객체
     */
    private JobDetail buildJobDetail(Class<? extends Job> jobClass, JobKey jobKey) {
        return JobBuilder.newJob(jobClass)
                .withIdentity(jobKey) // JobKey 설정 (이름, 그룹)
                .build();
    }

    /**
     * Cron 기반 Trigger를 생성하는 유틸 메서드.
     * 실행 실패 시 재시도 없이 넘어가도록 설정 (`withMisfireHandlingInstructionDoNothing`)
     *
     * @param triggerKey Trigger 식별자
     * @param cronExpression 실행 주기를 정의하는 Cron 문자열
     * @return CronTrigger 객체
     */
    private CronTrigger configureTrigger(TriggerKey triggerKey, String cronExpression) {
        return TriggerBuilder.newTrigger()
                .withIdentity(triggerKey) // TriggerKey 설정 (이름, 그룹)
                .withSchedule(
                        CronScheduleBuilder
                                .cronSchedule(cronExpression)
                                .withMisfireHandlingInstructionDoNothing()
                )
                .build();
    }
}
