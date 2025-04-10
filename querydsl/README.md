# 🧩 Querydsl 학습 정리

Querydsl은 타입 안전한 쿼리 작성을 지원하는 ORM 도구로, JPA와 함께 사용하면 유지보수성과 성능 모두에서 강력한 장점을 제공합니다. 아래는 각 섹션별 핵심 개념 요약입니다.

---

## 📦 섹션 2. 프로젝트 환경설정

- **프로젝트 생성**  
  Spring Boot 기반 Querydsl 프로젝트를 Gradle로 구성하는 초기 설정

- **Querydsl 설정과 검증**  
  Querydsl 라이브러리 의존성 추가 및 Q 클래스 생성 여부 확인

- **라이브러리 살펴보기**  
  Querydsl 관련 주요 의존성과 역할 간단히 파악

- **H2 데이터베이스 설치**  
  로컬 테스트용 인메모리 H2 데이터베이스 설치

- **스프링 부트 설정 - JPA, DB**  
  JPA 설정, 데이터베이스 연결 및 Hibernate 콘솔 로그 확인

---

## 🧱 섹션 3. 예제 도메인 모델

- **예제 도메인 모델과 동작확인**  
  Member, Team 엔티티 구성 및 초기 데이터 설정, 연관관계 테스트

---

## 🧪 섹션 4. 기본 문법

- **시작 - JPQL vs Querydsl**  
  Querydsl의 장점과 JPQL의 한계 비교

- **기본 Q-Type 활용**  
  Q타입 객체를 사용하여 타입 안전한 쿼리 작성

- **검색 조건 쿼리**  
  where 절 조건을 이용한 단순 검색 구현

- **결과 조회**  
  fetch(), fetchOne(), fetchFirst() 등 결과 조회 방법 정리

- **정렬**  
  orderBy()를 통한 정렬 처리

- **페이징**  
  offset(), limit()을 이용한 페이징 구현

- **집합**  
  count, sum, avg 등 집계 함수 사용법

- **조인 - 기본 조인**  
  inner join을 통한 관계 조인 쿼리 작성

- **조인 - on절**  
  join on을 활용한 필터링 조인 조건 작성

- **조인 - 페치 조인**  
  fetchJoin으로 성능 개선 (N+1 문제 해결)

- **서브 쿼리**  
  where절 또는 select절에 서브쿼리 적용하는 방법

- **Case 문**  
  단순/복잡 case 조건문을 이용한 값 처리

- **상수, 문자 더하기**  
  상수 값 추가 및 문자열 concat 예제

---

## ⚙️ 섹션 5. 중급 문법

- **프로젝션과 결과 반환 - 기본**  
  단순 값 반환 방식: 튜플, Object[] 등

- **프로젝션과 결과 반환 - DTO 조회**  
  Setter, 생성자, @QueryProjection 방식으로 DTO 매핑

- **프로젝션과 결과 반환 - @QueryProjection**  
  컴파일 타임 타입 체크가 가능한 안전한 DTO 매핑 방식

- **동적 쿼리 - BooleanBuilder 사용**  
  조건에 따라 쿼리 동적으로 조립 (AND/OR 조합)

- **동적 쿼리 - Where 다중 파라미터 사용**  
  BooleanExpression을 메서드로 분리하여 where절에 적용

- **수정, 삭제 벌크 연산**  
  update(), delete()를 통한 대량 데이터 처리

- **SQL function 호출하기**  
  stringTemplate로 SQL 함수 직접 호출

---

## 🧩 섹션 6. 실무 활용 - 순수 JPA와 Querydsl

- **순수 JPA 리포지토리와 Querydsl**  
  기존 JPA 코드와 Querydsl을 병행하여 사용하는 리포지토리 구조

- **동적 쿼리와 성능 최적화 조회 - Builder 사용**  
  BooleanBuilder를 활용한 조건 조합 + fetchJoin으로 최적화

- **동적 쿼리와 성능 최적화 조회 - Where절 파라미터 사용**  
  BooleanExpression 메서드 조합으로 가독성 높은 조건 처리

- **조회 API 컨트롤러 개발**  
  Querydsl 기반 조건 검색 API 구성

---

## 🧩 섹션 7. 실무 활용 - 스프링 데이터 JPA와 Querydsl

- **스프링 데이터 JPA 리포지토리로 변경**  
  JpaRepository를 사용하여 리포지토리 기본 구조 변경

- **사용자 정의 리포지토리**  
  CustomRepository + Impl 클래스에서 Querydsl 작성

- **스프링 데이터 페이징 활용1 - Querydsl 페이징 연동**  
  Querydsl의 fetchResults() 또는 fetch+count 방식 적용

- **스프링 데이터 페이징 활용2 - CountQuery 최적화**  
  count 쿼리 최적화를 통해 불필요한 조인 제거

- **스프링 데이터 페이징 활용3 - 컨트롤러 개발**  
  실제 검색 조건과 페이징 파라미터로 API 구성

---

## 🛠️ 섹션 8. 스프링 데이터 JPA가 제공하는 Querydsl 기능

- **인터페이스 지원 - QuerydslPredicateExecutor**  
  Spring Data가 제공하는 Predicate 기반 검색 기능

- **Querydsl Web 지원**  
  컨트롤러에서 Querydsl Predicate를 파라미터로 자동 매핑

- **리포지토리 지원 - QuerydslRepositorySupport**  
  Spring Data의 Querydsl 지원 클래스 활용 (페이징 등)

- **Querydsl 지원 클래스 직접 만들기**  
  직접 지원 클래스를 만들어 공통 Querydsl 기능 재사용

