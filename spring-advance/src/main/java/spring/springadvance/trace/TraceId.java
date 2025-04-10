package spring.springadvance.trace;


import java.util.UUID;

/**
 * 트랜잭션 ID와 실행 깊이(level)를 관리하는 클래스.
 * <p>
 * 트랜잭션 ID는 UUID의 앞 8자리를 사용하고, 호출 깊이에 따라 level이 증가/감소한다.
 * 트랜잭션 흐름을 시각적으로 표현하기 위한 기반 데이터 역할을 한다.
 * </p>
 */
public class TraceId {

    private String id;
    private int level;

    /**
     * 새로운 TraceId 생성자 (최초 호출 시 사용).
     * UUID 앞 8자리로 트랜잭션 ID를 생성하고, level은 0으로 설정한다.
     */
    public TraceId() {
        this.id = createId();
        this.level = 0;
    }

    private TraceId(String id, int level) {
        this.id = id;
        this.level = level;
    }

    private String createId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 현재 ID를 유지한 채, 레벨을 1 증가시킨 TraceId를 반환한다.
     */
    public TraceId createNextId() {
        return new TraceId(id, level + 1);
    }

    /**
     * 현재 ID를 유지한 채, 레벨을 1 감소시킨 TraceId를 반환한다.
     */
    public TraceId createPreviousId() {
        return new TraceId(id, level - 1);
    }

    /**
     * 현재 레벨이 최상위인지 여부를 반환한다.
     */
    public boolean isFirstLevel() {
        return level == 0;
    }

    public String getId() {
        return id;
    }

    public int getLevel() {
        return level;
    }
}