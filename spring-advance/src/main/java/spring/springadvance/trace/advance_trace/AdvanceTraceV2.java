package spring.springadvance.trace.advance_trace;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import spring.springadvance.trace.TraceId;
import spring.springadvance.trace.TraceStatus;

/**
 * 로그 추적기 V1 버전 (프로토타입).
 * <p>
 * 트랜잭션 ID와 호출 깊이를 기반으로 실행 흐름을 시각화하고, 실행 시간 및 예외 정보까지 로그로 출력한다.
 * </p>
 */
@Slf4j
@Component
public class AdvanceTraceV2 {
    private static final String START_PREFIX = "-->";
    private static final String COMPLETE_PREFIX = "<--";
    private static final String EX_PREFIX = "<X-";

    /**
     * 로그 시작: 트랜잭션 ID 및 깊이 정보를 기반으로 시작 로그를 출력한다.
     *
     * @param message 로그 메시지
     * @return TraceStatus (로그 상태 정보)
     */
    public TraceStatus begin(String message) {
        TraceId traceId = new TraceId(); // 최초 트랜잭션 ID 생성
        Long startTimeMs = System.currentTimeMillis();
        log.info("[{}] {}{}", traceId.getId(), addSpace(START_PREFIX, traceId.getLevel()), message);
        return new TraceStatus(traceId, startTimeMs, message);
    }
    //V2에서 추가
    public TraceStatus beginSync(TraceId beforeTraceId, String message) {
        TraceId nextId = beforeTraceId.createNextId();
        Long startTimeMs = System.currentTimeMillis();
        log.info("[" + nextId.getId() + "] " + addSpace(START_PREFIX,
                nextId.getLevel()) + message);
        return new TraceStatus(nextId, startTimeMs, message);
    }
    /**
     * 정상 종료 로그 처리.
     *
     * @param status 시작 시점의 TraceStatus
     */
    public void end(TraceStatus status) {
        complete(status, null);
    }

    /**
     * 예외 종료 로그 처리.
     *
     * @param status 시작 시점의 TraceStatus
     * @param e      발생한 예외 정보
     */
    public void exception(TraceStatus status, Exception e) {
        complete(status, e);
    }

    /**
     * 로그 종료를 공통으로 처리. 실행 시간 측정 및 예외 여부 판단.
     */
    private void complete(TraceStatus status, Exception e) {
        Long stopTimeMs = System.currentTimeMillis();
        long resultTimeMs = stopTimeMs - status.getStartTimeMs();
        TraceId traceId = status.getTraceId();

        if (e == null) {
            log.info("[{}] {}{} time={}ms", traceId.getId(),
                    addSpace(COMPLETE_PREFIX, traceId.getLevel()), status.getMessage(), resultTimeMs);
        } else {
            log.info("[{}] {}{} time={}ms ex={}", traceId.getId(),
                    addSpace(EX_PREFIX, traceId.getLevel()), status.getMessage(), resultTimeMs, e.toString());
        }
    }

    /**
     * 호출 깊이에 따라 로그 접두사를 계층적으로 정렬.
     *
     * @param prefix 접두사 (예: -->, <--)
     * @param level  현재 호출 깊이
     * @return 들여쓰기된 문자열
     */
    private static String addSpace(String prefix, int level) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            sb.append((i == level - 1) ? "|" + prefix : "| ");
        }
        return sb.toString();
    }
}
