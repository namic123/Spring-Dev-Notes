package spring.springadvance.app.v1;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import spring.springadvance.trace.TraceStatus;
import spring.springadvance.trace.advance_trace.AdvanceTraceV1;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryV1 {
    private final AdvanceTraceV1 trace;
    public void save(String itemId) {
        TraceStatus status = null;
        try {
            status = trace.begin("OrderRepository.save()");
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
