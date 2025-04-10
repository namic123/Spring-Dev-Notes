package spring.springadvance.app.v2;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import spring.springadvance.trace.TraceId;
import spring.springadvance.trace.TraceStatus;
import spring.springadvance.trace.advance_trace.AdvanceTraceV1;
import spring.springadvance.trace.advance_trace.AdvanceTraceV2;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryV2 {
    private final AdvanceTraceV2 trace;
    public void save(TraceId traceId, String itemId) {
        TraceStatus status = null;
        try {
            status = trace.beginSync(traceId, "OrderRepository.save()");
            //저장 로직
            if (itemId.equals("ex")) {
                throw new IllegalStateException("예외 발생!");
            }
            sleep(1000);
            trace.end(status);
        } catch (Exception e) {
            trace.exception(status, e);
            throw e;
        }
    }
    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}