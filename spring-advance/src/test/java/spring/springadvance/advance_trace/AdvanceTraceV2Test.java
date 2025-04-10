package spring.springadvance.advance_trace;

import org.junit.jupiter.api.Test;
import spring.springadvance.trace.TraceStatus;
import spring.springadvance.trace.advance_trace.AdvanceTraceV1;
import spring.springadvance.trace.advance_trace.AdvanceTraceV2;

/**
 * AdvanceTraceV1 기능 테스트.
 * 로그 시작-종료 및 예외 발생 시 로그 출력 포맷을 확인한다.
 */
public class AdvanceTraceV2Test {

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

    @Test
    void begin_end_level2() {
        AdvanceTraceV2 trace = new AdvanceTraceV2();
        TraceStatus status1 = trace.begin("hello1");
        TraceStatus status2 = trace.beginSync(status1.getTraceId(), "hello2");
        trace.end(status2);
        trace.end(status1);
    }
    @Test
    void begin_exception_level2() {
        AdvanceTraceV2 trace = new AdvanceTraceV2();
        TraceStatus status1 = trace.begin("hello");
        TraceStatus status2 = trace.beginSync(status1.getTraceId(), "hello2");
        trace.exception(status2, new IllegalStateException());
        trace.exception(status1, new IllegalStateException());
    }
}
