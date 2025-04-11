## Querydsl - 사용자 정의 JPA 리포지토리 설계 및 구현
블로그 : https://pjs-world.tistory.com/entry/Querydsl-%EC%82%AC%EC%9A%A9%EC%9E%90-%EC%A0%95%EC%9D%98-JPA-%EB%A6%AC%ED%8F%AC%EC%A7%80%ED%86%A0%EB%A6%AC-%EC%84%A4%EA%B3%84-%EB%B0%8F-%EA%B5%AC%ED%98%84

### 📌 목차

[1\. 기본 JPA Repository의 한계](#basic-jpa-repo) [2\. 사용자 정의 리포지토리 설계 및 구현](#custom-repository-design) [3\. Querydsl 기반 동적 검색 로직 구현](#querydsl-search-logic) [4\. 테스트 코드를 통한 검증](#test-implementation) [5\. 전체 구조 요약](#architecture-summary) [6\. 추가 팁 및 주의사항](#additional-tips)

## 1\. 기본 JPA Repository의 한계

Spring Data JPA는 JpaRepository 인터페이스를 통해 CRUD 기능 및 간단한 쿼리 메서드를 손쉽게 제공한다. 예컨대, 다음과 같이 사용자 이름을 기준으로 데이터를 조회할 수 있다.

```
public interface MemberRepository extends JpaRepository<Member, Long> {
    List<Member> findByUsername(String username);
}
```

그러나 이러한 방식은 단일 조건 또는 간단한 범위 조건에만 적합하며, 다음과 같은 **복잡한 조건을 포함하는 검색 기능은 지원하지 못한다.**

-   **팀 이름이 "teamB"**
-   **나이가 35세 이상 40세 이하**
-   **사용자 이름이 "member4"**

이러한 복합 조건 처리를 위해서는 Querydsl을 기반으로 사용자 정의 리포지토리를 도입해야 한다.

## 2\. 사용자 정의 리포지토리 설계 및 구현

**사용자 정의 리포지토리 구성 모식도**

[##_Image|kage@dCAkLW/btsNhdx3i86/SMR5KZEtturkfXrn1LylCK/img.png|CDM|1.3|{"originWidth":611,"originHeight":541,"style":"alignCenter","width":447,"height":396,"caption":"사용자 정의 리포지토리 구성 모식도"}_##]

**연결 흐름**

1.  JpaRepository는 JPA 기본 기능을 제공
2.  MemberRepository는 이를 상속하면서, MemberRepositoryCustom도 함께 상속
3.  MemberRepositoryImpl은 MemberRepositoryCustom을 구현하며 실제 search() 기능을 담당
4.  **Spring Data JPA가 자동으로** MemberRepository와 MemberRepositoryImpl을 연결해 동작

**왜 이렇게 나누는가?**

| **이유** | **설명** |
| --- | --- |
| **관심사 분리** | 기본 CRUD와 커스텀 Querydsl 로직 분리 |
| **유지보수** | 커스텀 쿼리가 많아질 경우, 별도 구현체에 집중 |
| **확장성** | 다양한 검색 조건 추가 시 BooleanExpression 조합 등 유연한 확장 가능 |
| **스프링 구조 호환** | Spring Data JPA의 자동 빈 등록/합성과 호환됨 |

**요약**

| **구성 요소** | **역할** | **주의사항** |
| --- | --- | --- |
| **JpaRepository** | 기본 CRUD 제공 | \- |
| **MemberRepository** | JPA + 사용자 정의 조합 | MemberRepositoryImpl과 이름 일치 필수 |
| **MemberRepositoryCustom** | 사용자 정의 기능 선언 | 쿼리 메서드 정의만 |
| **MemberRepositoryImpl** | 사용자 정의 기능 구현 | Impl 접미어 필수, Querydsl 사용 |

**예제 코드**

먼저, 사용자가 정의한 검색 기능을 선언할 인터페이스를 작성한다.

```
public interface MemberRepositoryCustom {
    List<MemberTeamDto> search(MemberSearchCondition condition);
}
```

이후, 실제 Querydsl 쿼리를 구현할 클래스를 작성하는데, **반드시 MemberRepositoryImpl**이라는 명명 규칙을 따라야 한다. 그렇지 않으면 **Spring Data JPA가 해당 구현체를 인식하지 못한다**.

```
public class MemberRepositoryImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public MemberRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<MemberTeamDto> search(MemberSearchCondition condition) {
        return queryFactory
            .select(new QMemberTeamDto(
                member.id, member.username, member.age,
                team.id, team.name
            ))
            .from(member)
            .leftJoin(member.team, team)
            .where(
                usernameEq(condition.getUsername()),
                teamNameEq(condition.getTeamName()),
                ageGoe(condition.getAgeGoe()),
                ageLoe(condition.getAgeLoe())
            )
            .fetch();
    }

    // Querydsl 동적 조건 메서드
    private BooleanExpression usernameEq(String username) {
        return isEmpty(username) ? null : member.username.eq(username);
    }
    private BooleanExpression teamNameEq(String teamName) {
        return isEmpty(teamName) ? null : team.name.eq(teamName);
    }
    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe == null ? null : member.age.goe(ageGoe);
    }
    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe == null ? null : member.age.loe(ageLoe);
    }
}
```

MemberRepository에서 JpaRepository와 커스텀 리포지토리를 상속 받음.

```
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {
    List<Member> findByUsername(String username);
}
```

## 3\. Querydsl 기반 동적 검색 로직 구현

Querydsl은 BooleanExpression을 통해 null-safe한 where 조건을 생성할 수 있다. null인 조건은 무시되므로 사용자가 일부 조건만 설정해도 문제없이 동작한다.

DTO 매핑에는 QMemberTeamDto와 같이 @QueryProjection 기반 생성자가 있는 DTO가 필요하다.

| \* **@QueryProjection?** @QueryProjection은 **Querydsl에서 제공하는 애너테이션**으로, **쿼리 결과를 DTO에 직접 매핑할 때 사용하는 컴파일 타임 기반의 타입 안전한 방식**입니다. 이 애너테이션을 활용하면 Querydsl이 자동으로 해당 DTO의 Q타입 클래스를 생성해주며, **생성자 기반으로 select 절에서 DTO를 직접 생성**할 수 있게 도와줍니다. |
| --- |

```
@Data
public class MemberTeamDto {

    private Long memberId;
    private String username;
    private int age;
    private Long teamId;
    private String teamName;

    @QueryProjection
    public MemberTeamDto(Long memberId, String username, int age, Long teamId, String teamName) {
        this.memberId = memberId;
        this.username = username;
        this.age = age;
        this.teamId = teamId;
        this.teamName = teamName;
    }
}
```

## 4\. 테스트 코드를 통한 검증

```
@Test
public void searchTest() {
    // 팀과 멤버 생성
    Team teamA = new Team("teamA");
    Team teamB = new Team("teamB");
    em.persist(teamA);
    em.persist(teamB);
    em.persist(new Member("member1", 10, teamA));
    em.persist(new Member("member2", 20, teamA));
    em.persist(new Member("member3", 30, teamB));
    em.persist(new Member("member4", 40, teamB));

    // 검색 조건
    MemberSearchCondition condition = new MemberSearchCondition();
    condition.setAgeGoe(35);
    condition.setAgeLoe(40);
    condition.setTeamName("teamB");

    // 결과 검증
    List<MemberTeamDto> result = memberRepository.search(condition);
    assertThat(result).extracting("username").containsExactly("member4");
}
```

위 코드에서 확인되는 바와 같이, memberRepository로 커스텀 repository의 search메서드를 사용할 수 있는 것을 볼 수 있다.

## 5\. 전체 구조 요약

| **역할** | **클래스 및 인터페이스** | **설명** |
| --- | --- | --- |
| **기본 Repository** | MemberRepository | JPA 기본 기능 + 커스텀 상속 |
| **사용자 정의 인터페이스** | MemberRepositoryCustom | search 메서드 선언 |
| **구현체** | MemberRepositoryImpl | Querydsl로 로직 구현 |
| **검색 조건 DTO** | MemberSearchCondition | 사용자 입력 조건 캡슐화 |
| **결과 DTO** | MemberTeamDto, QMemberTeamDto | 결과값 매핑 |

## 6\. 추가 팁 및 주의사항

-   @QueryProjection을 DTO 생성자에 붙이면 컴파일 시 자동으로 Q타입이 생성된다.
-   @Repository 애너테이션은 생략 가능하다. Spring Data JPA가 자동으로 인식한다.
-   구현 클래스의 이름은 반드시 Repository + Impl 형태여야 한다.
-   Querydsl을 사용하면 JPQL보다 타입 안정성이 뛰어나며, 유지보수가 용이하다.