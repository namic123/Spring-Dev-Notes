## 로그 추적기 V1의 개발 배경과 구조

### 🎯 목적

-   요청이 어디서 시작되고 어디서 끝났는지, 얼마나 걸렸는지를 **트랜잭션 단위로 추적**
-   \*\*깊이(Level)\*\*를 기반으로 **호출 계층 구조를 시각적으로 표현**
-   **예외 발생 시점**도 로그에 함께 출력

## 📦 1. TraceId 클래스

### 🧠 핵심 역할

-   하나의 트랜잭션을 식별하는 **고유한 ID(UUID)** 생성
-   현재 호출의 **깊이(level)** 를 추적하여, 요청의 흐름 구조를 계층적으로 표시

### 💡 주요 메서드 설명

| 메서드명 | 설명 |
| --- | --- |
| createId() | UUID를 생성하고 앞 8자리만 사용 |
| createNextId() | 동일 ID, level + 1 로 다음 단계 생성 |
| createPreviousId() | 동일 ID, level - 1 로 이전 단계로 이동 |
| isFirstLevel() | 현재 호출이 최상위(루트)인지 확인 |

#### **🧾 로그 출력 예시**

```
[796bccd9] OrderController.request()
[796bccd9] |-->OrderService.orderItem()
[796bccd9] | |-->OrderRepository.save()
```

## 📦 2. TraceStatus 클래스

### 🧠 핵심 역할

-   로그가 **시작된 시점의 상태**를 기록 (추후 종료할 때 비교 기준)
-   로그를 식별할 수 있는 TraceId, 시작 시간, 메시지를 보관

### ✨ 활용

-   AdvanceTraceV1에서 로그 시작 → TraceStatus 생성 → 종료 시 다시 사용

---

## 🔍 3. AdvanceTraceV1 클래스

### 🧠 핵심 역할

-   로그의 **시작, 정상 종료, 예외 종료**를 처리
-   로그 메시지를 계층적으로 정렬하여 출력
-   실행 시간도 함께 기록

### 🔧 주요 공개 메서드



| 메서드 | 설명 |
| --- | --- |
| begin(String message) | 로그 시작. TraceId 생성, 시작 시점 기록 |
| end(TraceStatus status) | 정상 흐름 로그 종료 처리 |
| exception(TraceStatus status, Exception e) | 예외 상황 로그 종료 처리 |

### 🔧 내부 메서드



| 메서드 | 설명 |
| --- | --- |
| complete(TraceStatus, Exception) | 종료 처리 공통 로직 (정상/예외 모두) |
| addSpace(String prefix, int level) | 호출 깊이에 따라 \` |

### 🧾 로그 출력 예시

정상 종료:

```
[796bccd9] |-->OrderService.orderItem()
[796bccd9] |<--OrderService.orderItem() time=13ms
```

예외 종료:

```
[796bccd9] |-->OrderService.orderItem()
[796bccd9] |<X-OrderService.orderItem() time=15ms ex=IllegalStateException
```

## ✅ 4. 테스트: AdvanceTraceV1Test



| 테스트 메서드 | 설명 |
| --- | --- |
| begin\_end() | 로그 시작 → 정상 종료 흐름 확인 |
| begin\_exception() | 로그 시작 → 예외 종료 흐름 확인 |

> 이 테스트는 **실행 흐름을 눈으로 확인**하는 용도입니다. 자동 검증은 포함되어 있지 않기 때문에 콘솔 로그를 직접 확인해야 합니다.

---

## 📌 전체 흐름 요약

1.  클라이언트 요청 발생
2.  AdvanceTraceV1.begin("method명") 호출 → TraceId와 시작 시간 기록
3.  비즈니스 로직 수행
4.  AdvanceTraceV1.end(status) 또는 exception(status, e) 호출로 로그 종료
5.  TraceId의 level로 들여쓰기 표현 → **호출 계층 추적 가능**

---

## 🔁 한계점 (V1 기준)

-   TraceId가 매 호출마다 새로 생성됨 → **동일 트랜잭션 흐름을 연결할 수 없음**
-   상태를 공유하지 않음 → 이후 V2에서 개선될 예정