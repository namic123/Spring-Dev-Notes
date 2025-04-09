package study.quartzschedulertest.listener;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.TriggerListener;
import org.springframework.stereotype.Component;

/**
 * Quartz Trigger의 상태를 감지하기 위한 글로벌 TriggerListener 구현 클래스.
 *
 * Trigger가 실행되었을 때, misfire 되었을 때, 실행 완료 후 등을 감지하여 로그를 남김.
 *
 * SchedulerFactoryBean에 전역 리스너로 등록되어 모든 Trigger에 적용됨.
 *
 * @author 박재성
 * @version 1.0
 * @since 2025-03-25
 */
@Component
@Slf4j
public class TriggerCustomListener implements TriggerListener {
    /**
     * 리스너의 이름을 반환.
     *
     * @return "globalTrigger" 문자열
     */
    @Override
    public String getName() {
        return "globalTrigger";
    }

    /**
     * Trigger가 실제로 firing 되었을 때 호출됨.
     * (즉, 해당 트리거에 의해 Job이 실행되려는 시점)
     *
     * @param trigger 트리거 정보
     * @param context 실행될 Job의 컨텍스트
     */
    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {
        JobKey jobKey = trigger.getJobKey();
        log.info("triggerFired at {} :: jobkey : {}", trigger.getStartTime(), jobKey);
    }

    /**
     * Job 실행을 막고 싶은 경우 true를 반환하면 해당 Job은 실행되지 않음.
     * 기본적으로 false 반환 (실행 허용)
     *
     * @param trigger 트리거 정보
     * @param context 실행될 Job의 컨텍스트
     * @return true: 실행 거부 / false: 실행 허용
     */
    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
        return false;
    }

    /**
     * Trigger의 misfire가 발생했을 때 호출됨.
     * (예: 트리거가 제때 실행되지 못한 경우)
     *
     * @param trigger misfire 발생한 트리거
     */
    @Override
    public void triggerMisfired(Trigger trigger) {
        JobKey jobKey = trigger.getJobKey();
        log.info("triggerMisfired at {} :: jobkey : {}", trigger.getStartTime(), jobKey);
    }

    /**
     * Trigger가 Job 실행을 완료했을 때 호출됨.
     *
     * @param trigger                실행 완료된 트리거
     * @param context                실행된 Job의 컨텍스트
     * @param triggerInstructionCode Quartz에서 정의한 실행 완료 코드
     */
    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext context, Trigger.CompletedExecutionInstruction triggerInstructionCode) {
        JobKey jobKey = trigger.getJobKey();
        log.info("triggerComplete at {} :: jobkey : {}", trigger.getStartTime(), jobKey);
    }
}
