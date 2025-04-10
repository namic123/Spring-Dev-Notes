package spring.springadvance.trace;


/**
 * 트랜잭션의 로그 상태 정보를 담는 클래스.
 * <p>
 * 로그의 시작 시간, 메시지, TraceId 정보를 저장하여 종료 로그 시 사용된다.
 * </p>
 */
public class TraceStatus {

    private final TraceId traceId;
    private final Long startTimeMs;
    private final String message;

    /**
     * 로그 상태 정보를 생성한다.
     *
     * @param traceId     트랜잭션 ID와 level 정보
     * @param startTimeMs 시작 시간(ms)
     * @param message     로그 메시지
     */
    public TraceStatus(TraceId traceId, Long startTimeMs, String message) {
        this.traceId = traceId;
        this.startTimeMs = startTimeMs;
        this.message = message;
    }

    public TraceId getTraceId() {
        return traceId;
    }

    public Long getStartTimeMs() {
        return startTimeMs;
    }

    public String getMessage() {
        return message;
    }
}
