package spring.springadvance.app.v1;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import spring.springadvance.trace.TraceStatus;
import spring.springadvance.trace.advance_trace.AdvanceTraceV1;

@Service
@RequiredArgsConstructor
public class OrderServiceV1 {
    private final OrderRepositoryV1 orderRepository;
    private final AdvanceTraceV1 trace;

    public void orderItem(String itemId) {
        TraceStatus status = null;
        try {
            status = trace.begin("OrderService.orderItem()");
            orderRepository.save(itemId);
            trace.end(status);
        } catch (Exception e) {
            trace.exception(status, e);
            throw e;
        }
    }
}