## 개요: 왜 V2가 필요한가?

V1 버전에서는 각 계층(Controller → Service → Repository)에서 HelloTrace.begin()을 호출할 때마다 새로운 TraceId가 생성됩니다. 이 방식은 각 로그가 **독립적인 트랜잭션 ID**를 가지므로, 전체 요청 흐름에서 어떤 메서드가 어떤 요청의 하위 호출인지를 파악하기 어렵습니다.  
예를 들어, 아래와 같은 로그가 찍히더라도 요청의 연속성이나 계층 구조는 보이지 않습니다.

```
[a1b2c3d4] OrderController.request()
[e5f6g7h8] OrderService.orderItem()
[z9y8x7w6] OrderRepository.save()
```

→ 이 문제를 해결하기 위해 V2에서는 TraceId를 각 계층 간 **파라미터로 전달**하는 방식으로 **동기화**합니다.

---

## 🔧 핵심 개선: TraceId 파라미터 전달

### 🎯 목표

-   하나의 HTTP 요청 내에서는 **하나의 트랜잭션 ID** 사용
-   계층 깊이를 추적할 수 있도록 level 값을 **자동 증가**
-   **정상 흐름 / 예외 흐름 / 실행 시간**을 모두 추적

---

## 📁 구성 클래스 상세 설명

---

### 1️⃣ TraceId

```
public class TraceId {
    private String id;    // 트랜잭션 ID (UUID 앞 8자리)
    private int level;    // 현재 호출의 깊이
}
```

-   createNextId(): 같은 트랜잭션 ID를 유지하면서 level을 +1 증가시킴
-   createPreviousId(): 반대로 level을 -1 감소시킴
-   isFirstLevel(): 현재 트랜잭션이 최상위 호출인지 판단

---

### 2️⃣ TraceStatus

```
public class TraceStatus {
    private TraceId traceId;        // 현재 트랜잭션의 ID 및 깊이 정보
    private Long startTimeMs;       // 시작 시간 (소요 시간 측정용)
    private String message;         // 로그 메시지
}
```

로그 시작 시 생성되며, 종료(end) 또는 예외 발생 시(exception) 활용됩니다.

---

### 3️⃣ AdvanceTraceV2

```
public TraceStatus begin(String message);
public TraceStatus beginSync(TraceId beforeTraceId, String message);
```

#### 🔹 begin()

-   최상위 호출 시 사용
-   새로운 트랜잭션 ID와 level=0 으로 시작

#### 🔹 beginSync()

-   하위 호출(중첩 호출) 시 사용
-   전달받은 TraceId를 기반으로 같은 ID, level + 1 적용

#### 🔹 로그 출력 형태

-   시작:


```
[796bccd9] |-->OrderService.orderItem()
```

-   종료:

```
[796bccd9] |<--OrderService.orderItem() time=10ms
```

-   예외:

```
[796bccd9] |<X-OrderService.orderItem() time=10ms ex=...
```

---

## ⚙️ 애플리케이션 적용 흐름

### 💡 핵심 아이디어: TraceId를 넘기고, 그걸로 다음 TraceStatus를 만들자

---

### 1\. OrderControllerV2

```
TraceStatus status = trace.begin("Controller 호출");
orderService.orderItem(status.getTraceId(), itemId);
trace.end(status);
```

-   TraceStatus에서 traceId를 추출해서 서비스 계층으로 넘김

---

### 2\. OrderServiceV2

```
TraceStatus status = trace.beginSync(traceId, "Service 호출");
orderRepository.save(status.getTraceId(), itemId);
trace.end(status);
```

-   전달받은 traceId를 기반으로 level + 1 상태의 새로운 TraceId 생성

---

### 3\. OrderRepositoryV2

```
TraceStatus status = trace.beginSync(traceId, "Repository 저장");
trace.end(status);
```

-   동일하게 TraceId를 이어받아 호출 깊이만 증가시킨 채 로그 남김

---

## 🧪 실행 결과 예시

### ✅ 정상 실행 시

```
[c80f5dbb] OrderController.request()
[c80f5dbb] |-->OrderService.orderItem()
[c80f5dbb] | |-->OrderRepository.save()
[c80f5dbb] | |<--OrderRepository.save() time=1005ms
[c80f5dbb] |<--OrderService.orderItem() time=1014ms
[c80f5dbb] OrderController.request() time=1017ms
```

-   트랜잭션 ID는 계속 \[c80f5dbb\]
-   호출 깊이에 따라 |--, | |-- 와 같이 들여쓰기 적용

---

### ❌ 예외 실행 시

```
[ca867d59] OrderController.request()
[ca867d59] |-->OrderService.orderItem()
[ca867d59] | |-->OrderRepository.save()
[ca867d59] | |<X-OrderRepository.save() time=0ms ex=예외 발생!
[ca867d59] |<X-OrderService.orderItem() time=7ms ex=예외 발생!
[ca867d59] OrderController.request() time=7ms ex=예외 발생!
```

-   |<X- 접두어로 예외 여부 시각적으로 표현
-   예외 발생한 메서드에서 예외 로그 → 상위 메서드로 전파되며 예외 로그 반복

---

## ✅ V2의 결과 요약

| 기능 | 구현 여부 | 설명 |
| --- | --- | --- |
| 트랜잭션 ID | ✅ 유지됨 | 같은 요청 흐름에 동일 ID 사용 |
| 메서드 호출 깊이 | ✅ 표현됨 | 들여쓰기 \` |
| 예외 구분 | ✅ | <X- 접두어로 표기됨 |
| 실행 시간 | ✅ | 로그 종료 시 time 출력 |
| 요청 구분 | ✅ | 요청마다 ID가 다름 (UUID 생성) |

---

## ⚠️ V2 방식의 한계

| 문제점 | 설명 |
| --- | --- |
| **모든 계층에 파라미터 추가** | TraceId를 계속 넘겨야 해서 메서드 시그니처가 오염됨 |
| **인터페이스 전파 문제** | 인터페이스가 있다면 전부 수정해야 함 |
| **초기 호출 판단 필요** | 처음인지 중첩 호출인지 외부에서 판단해야 함 |
| **외부 시스템에서 호출 시 복잡** | 외부에서 Service를 직접 호출하면 TraceId를 만들기 어려움 |