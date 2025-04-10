## ✅ 로그 추적기 V1 - 실제 적용 흐름

### 📌 목표

-   앞서 만든 HelloTraceV1 로그 추적기를 **애플리케이션 코드에 직접 적용**
-   HTTP 요청 → 컨트롤러 → 서비스 → 리포지토리의 호출 흐름을 로그로 남기고, **예외 발생 여부와 실행 시간**까지 기록

---

## 🔁 적용 방식

### 🏷 패키지 분리

-   기존 코드를 v0에 보존하고, v1 패키지를 새로 만들어 비교 가능하게 함
-   OrderControllerV0 → V1, OrderServiceV0 → V1, OrderRepositoryV0 → V1 으로 **복사**
-   내부 클래스 참조도 V1으로 변경

```
hello.advanced.app.v1
 ├── OrderControllerV1
 ├── OrderServiceV1
 └── OrderRepositoryV1
```

---

## 🧩 HelloTraceV1 사용법 요약

```
TraceStatus status = trace.begin("메시지");
try {
    // 비즈니스 로직
    trace.end(status);       // 정상 종료
} catch (Exception e) {
    trace.exception(status, e); // 예외 발생 시
    throw e;                 // 예외 다시 던지기 필수!
}
```

### ❗ 중요한 포인트

-   TraceStatus는 try 밖에서 선언해야 catch에서도 접근 가능
-   예외는 throw e;로 다시 던져야 실제 예외 흐름이 유지됨
-   로그 자체는 **부수 효과**만 있어야 하며, **비즈니스 흐름을 변경하면 안 됨**

---

## ✨ 각 계층별 적용 예

### 📍 Controller: OrderControllerV1

```
@GetMapping("/v1/request")
public String request(String itemId) {
    TraceStatus status = null;
    try {
        status = trace.begin("OrderController.request()");
        orderService.orderItem(itemId);
        trace.end(status);
        return "ok";
    } catch (Exception e) {
        trace.exception(status, e);
        throw e;
    }
}
```

### 📍 Service: OrderServiceV1

```
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
```

### 📍 Repository: OrderRepositoryV1

```
public void save(String itemId) {
    TraceStatus status = null;
    try {
        status = trace.begin("OrderRepository.save()");
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
```

---

## 🧪 실행 결과 예시

### ✔ 정상 요청

```
GET /v1/request?itemId=hello

[11111111] OrderController.request()
[22222222] |-->OrderService.orderItem()
[33333333] | |-->OrderRepository.save()
[33333333] | |<--OrderRepository.save() time=1000ms
[22222222] |<--OrderService.orderItem() time=1001ms
[11111111] OrderController.request() time=1001ms
```

### ❌ 예외 요청

```
GET /v1/request?itemId=ex

[aaa] OrderController.request()
[bbb] |-->OrderService.orderItem()
[ccc] | |-->OrderRepository.save()
[ccc] | |<X-OrderRepository.save() time=0ms ex=IllegalStateException
[bbb] |<X-OrderService.orderItem() time=6ms ex=IllegalStateException
[aaa] OrderController.request() time=7ms ex=IllegalStateException
```

---

## ⚠️ 남은 문제점과 한계

### 아직 해결하지 못한 요구사항

요구사항해결 여부

| 모든 public 메서드 로그 출력 | ✅ 수동 적용 |
| --- | --- |
| 비즈니스 흐름에 영향 없음 | ✅ 예외 재전파로 보장 |
| 실행 시간 기록 | ✅ System.currentTimeMillis() 활용 |
| 예외 로그 포함 | ✅ trace.exception() 처리 |
| **메서드 호출 깊이 표현** | ❌ 아직 구현 안됨 |
| **HTTP 요청 단위 트랜잭션 ID 유지** | ❌ TraceId가 요청마다 새로 생성됨 |

### 👉 핵심 미완성 기능

-   모든 계층의 로그가 **같은 TraceId를 사용**하지 않음 → 각 계층에서 new TraceId() 호출
-   level은 항상 0 → 호출 관계가 계층적으로 보이지 않음
-   로그 문맥(Context)이 공유되지 않음

---

## 📚 다음 단계에서 개선할 점 (예고)

개선 목표이유

| **동일 HTTP 요청에서 동일한 TraceId 사용** | 전체 호출 흐름을 트랜잭션 단위로 묶기 위함 |
| --- | --- |
| **메서드 깊이(level) 증가** | 로그 계층 구조 시각화 목적 |
| **TraceId 상태를 공통 저장소에서 관리** | AOP 또는 ThreadLocal로 공유 필요 |

---

## ✅ 정리

-   로그 추적기 V1은 수동 방식으로 추적기(HelloTraceV1)를 각 계층에 직접 삽입
-   실행 시간 및 예외 여부를 포함해 로그를 남김으로써 기본적인 추적 기능 확보
-   그러나 TraceId와 level이 공유되지 않아 **호출 트리 구조**를 완성하지 못함