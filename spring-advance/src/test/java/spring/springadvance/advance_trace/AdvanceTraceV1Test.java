package spring.springadvance.advance_trace;

import org.junit.jupiter.api.Test;
import spring.springadvance.trace.TraceStatus;
import spring.springadvance.trace.advance_trace.AdvanceTraceV1;

/**
 * AdvanceTraceV1 기능 테스트.
 * 로그 시작-종료 및 예외 발생 시 로그 출력 포맷을 확인한다.
 */
public class AdvanceTraceV1Test {

    @Test
    void begin_end() {
        AdvanceTraceV1 trace = new AdvanceTraceV1();
        TraceStatus status = trace.begin("hello");
        trace.end(status); // 정상 종료 로그 출력
    }

    @Test
    void begin_exception() {
        AdvanceTraceV1 trace = new AdvanceTraceV1();
        TraceStatus status = trace.begin("hello");
        trace.exception(status, new IllegalStateException()); // 예외 종료 로그 출력
    }
}
