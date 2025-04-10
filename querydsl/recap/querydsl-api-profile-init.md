## Querydsl 기반 조회 API 개발 + 프로파일 분리 샘플 데이터 초기화

### 📌 목차

1.  [프로파일 분리란?](#%ED%94%84%EB%A1%9C%ED%8C%8C%EC%9D%BC-%EB%B6%84%EB%A6%AC%EB%9E%80)
2.  [application.yml 구성과 프로파일 활성화](#applicationyml-%EA%B5%AC%EC%84%B1%EA%B3%BC-%ED%94%84%EB%A1%9C%ED%8C%8C%EC%9D%BC-%ED%99%9C%EC%84%B1%ED%99%94)
3.  [샘플 데이터 추가 코드 분석](#%EC%83%98%ED%94%8C-%EB%8D%B0%EC%9D%B4%ED%84%B0-%EC%B6%94%EA%B0%80-%EC%BD%94%EB%93%9C-%EB%B6%84%EC%84%9D)
4.  [조회 컨트롤러 구성](#%EC%A1%B0%ED%9A%8C-%EC%BB%A8%ED%8A%B8%EB%A1%A4%EB%9F%AC-%EA%B5%AC%EC%84%B1)
5.  [Postman 테스트 예제](#postman-%ED%85%8C%EC%8A%A4%ED%8A%B8-%EC%98%88%EC%A0%9C)
6.  [✅ 핵심 요약](#%E2%9C%85-%ED%95%B5%EC%8B%AC-%EC%9A%94%EC%95%BD)

## 프로파일 분리란?

-   **Spring Profile**은 실행 환경에 따라 다른 설정을 적용하기 위해 사용하는 기능입니다.
-   local, test, prod 등의 이름으로 나누고, 해당 환경에 맞는 Bean, 설정, 초기화 등을 선택적으로 활성화할 수 있습니다.

### 왜 필요한가?

-   **로컬 개발용 샘플 데이터**는 실제 테스트나 운영에 포함되면 안 됩니다.
-   테스트에서는 **데이터 초기화가 없어야** 하며, 서로 독립적인 설정이 필요합니다.

## application.yml 구성과 프로파일 활성화

### ✅ src/main/resources/application.yml

```
spring:
  profiles:
    active: local
```

-   로컬 개발 시 자동으로 local 프로파일이 활성화됩니다.
-   @Profile("local")이 적용된 Bean만 등록됩니다.

### ✅ src/test/resources/application.yml

```
spring:
  profiles:
    active: test
```

-   테스트 실행 시, 테스트 전용 설정이 적용됩니다.
-   @Profile("local")이 붙은 초기화 코드는 **실행되지 않음** → 테스트 결과에 영향을 주지 않음.

## 샘플 데이터 추가 코드 분석

### 클래스 위치: study.querydsl.InitMember

```
@Profile("local")
@Component
@RequiredArgsConstructor
public class InitMember {
    private final InitMemberService initMemberService;

    @PostConstruct
    public void init() {
        initMemberService.init();
    }

    @Component
    static class InitMemberService {
        @PersistenceContext
        EntityManager em;

        @Transactional
        public void init() {
            Team teamA = new Team("teamA");
            Team teamB = new Team("teamB");
            em.persist(teamA);
            em.persist(teamB);

            for (int i = 0; i < 100; i++) {
                Team selectedTeam = i % 2 == 0 ? teamA : teamB;
                em.persist(new Member("member" + i, i, selectedTeam));
            }
        }
    }
}
```

### 🔍 핵심 설명

| 항목 | 설명 |
| --- | --- |
| @Profile("local") | 해당 클래스는 local 프로파일일 때만 실행됨 |
| @PostConstruct | Spring Bean 초기화 후 자동으로 init() 실행 |
| EntityManager | JPA 직접 사용하여 팀과 멤버 100명 데이터 생성 |
| @Transactional | 트랜잭션 보장으로 em.persist() 처리 완료 |

## 조회 컨트롤러 구성

### 클래스 위치: study.querydsl.controller.MemberController

```
@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberJpaRepository memberJpaRepository;

    @GetMapping("/v1/members")
    public List<MemberTeamDto> searchMemberV1(MemberSearchCondition condition) {
        return memberJpaRepository.search(condition);
    }
}
```

### 🔍 핵심 설명

| 항목 | 설명 |
| --- | --- |
| @RestController | JSON 응답용 컨트롤러 |
| @GetMapping("/v1/members") | 멤버 검색 API GET 엔드포인트 |
| MemberSearchCondition | 검색 조건 (teamName, ageGoe 등) |
| MemberTeamDto | 검색 결과 DTO 리스트 |

## Postman 테스트 예제

-   호출 URL: http://localhost:8080/v1/members?teamName=teamB&ageGoe=31&ageLoe=35

-   요청 파라미터:
    -   teamName=teamB
    -   ageGoe=31 (31세 이상)
    -   ageLoe=35 (35세 이하)
-   이 쿼리는 member31, member33, member35 등 teamB에 속하고 나이가 조건에 부합하는 멤버만 필터링해서 응답합니다.

## ✅ 핵심 요약



| 구성 요소 | 역할 |
| --- | --- |
| application.yml | 실행 환경(local/test) 구분 설정 |
| @Profile("local") | 로컬 환경에서만 샘플 데이터 등록 |
| InitMember | 팀 및 멤버 샘플 데이터 생성 클래스 |
| MemberController | Querydsl을 활용한 조회 API 제공 |
| /v1/members | 조건 기반 동적 쿼리 실행 엔드포인트 |