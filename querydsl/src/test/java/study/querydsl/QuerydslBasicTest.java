package study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;
import study.querydsl.entity.Team;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static study.querydsl.entity.QMember.*;
import static study.querydsl.entity.QTeam.*;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory jpaQueryFactory;

    @PersistenceUnit
    EntityManagerFactory emf;

    @BeforeEach
    public void before() {
        jpaQueryFactory = new JPAQueryFactory(em);

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);
        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    /*
    * JPQL
        ✅ 장점
        JPA 표준 쿼리 언어로, JPA를 사용하는 프로젝트에서 기본적으로 제공됨.
        SQL과 유사한 문법으로 직관적.
        *
        ❌ 단점
        문자열 기반이라 컴파일 타임에 오류를 잡을 수 없음.
        파라미터 바인딩이 번거로우며, 복잡한 동적 쿼리를 만들기 어려움.
        자동 완성이 안 되므로 오타 발생 가능.
    *
    * */

    /* JPQL 문법 */
    @Test
    public void startJPQL() {
        String qlString = "select m from Member m where m.username = :username";

        Member findMember = em.createQuery(qlString, Member.class)
                .setParameter("username", "member1")
                .getSingleResult();
    }


/*
        * QueryDSL

        * ✅ 장점
        Type-safe: 컴파일 타임에 문법 오류를 잡을 수 있음.
        자동 완성 지원: IDE의 도움을 받아 개발 속도 향상.
        동적 쿼리 작성이 용이: BooleanBuilder 또는 Expressions 등의 기능을 활용해 복잡한 조건을 쉽게 조립 가능.
        가독성 및 유지보수성 우수: 코드 기반으로 작성되므로 유지보수 시 안정적.

        * ❌ 단점
        별도의 Q 클래스 생성 필요 (gradle build 또는 annotationProcessor 설정 필요).
        JPQL보다 러닝 커브가 존재함.
        프로젝트 규모가 작다면 굳이 사용할 필요가 없을 수도 있음.
* */

    /* QueryDsl 문법 */
    @Test
    public void startQuerydsl() {
        Member findMember = jpaQueryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1")) // 파라미터 바인딩
                .fetchOne();

        Assertions.assertThat(findMember.getUsername()).isEqualTo("member1");
    }


    // 검색 조건 쿼리 기본 예제
    @Test
    public void search() {
        Member findMember = jpaQueryFactory
                .selectFrom(member)
                .where(member.username.eq("member1")
                        .and(member.age.eq(10)))
                .fetchOne();
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void searchAndParam() {
        List<Member> result1 = jpaQueryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"), // and 조건은 , 쉼표로 간단하게 작성 가능
                        member.age.eq(10))
                .fetch();

        assertThat(result1.size()).isEqualTo(1);
    }

    /*
    JPQL이 제공하는 모든 검색 조건 제공
    * member.username.eq("member1") // username = 'member1'
    member.username.ne("member1") //username != 'member1'
    member.username.eq("member1").not() // username != 'member1'
    member.username.isNotNull() //이름이 is not null
    member.age.in(10, 20) // age in (10,20)
    member.age.notIn(10, 20) // age not in (10, 20)
    member.age.between(10,30) //between 10, 30
    member.age.goe(30) // age >= 30
    member.age.gt(30) // age > 30
    member.age.loe(30) // age <= 30
    member.age.lt(30) // age < 30
    member.username.like("member%") //like 검색
    member.username.contains("member") // like ‘%member%’ 검색
    member.username.startsWith("member") //like ‘member%’ 검색
    * */

    /* 결과 조회 fetch~ */
    /*
    * fetch() : 리스트 조회, 데이터 없으면 빈 리스트 반환
        fetchOne() : 단 건 조회
        결과가 없으면 : null
        결과가 둘 이상이면 : com.querydsl.core.NonUniqueResultException
        fetchFirst() : limit(1).fetchOne()
        fetchResults() : 페이징 정보 포함, total count 쿼리 추가 실행
        fetchCount() : count 쿼리로 변경해서 count 수 조회
    * */

    @Test
    public void fetchResult() {

        List<Member> fetch = jpaQueryFactory.selectFrom(member).fetch(); // 리스트 조회, 없으면 빈 리스트

        Member findMember1 = jpaQueryFactory.selectFrom(member).fetchOne(); // 단건 조회, 결과가 둘 이상이면 Exception

        Member findMember2 = jpaQueryFactory.selectFrom(member).fetchFirst(); // 처음 한건 조회

        QueryResults<Member> results = jpaQueryFactory.selectFrom(member).fetchResults(); // 페이징할 때 사용

        long count = jpaQueryFactory.selectFrom(member).fetchCount(); // count 쿼리로 변경
    }

    /**
     * 회원 정렬 순서
     * 1. 회원 나이 내림차순(desc)
     * 2. 회원 이름 올림차순(asc)
     * 단 2에서 회원 이름이 없으면 마지막에 출력(nulls last)
     */
    @Test
    public void sort() {
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> result = jpaQueryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);
        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();
    }

    /*
    *   desc() , asc() : 일반 정렬
        nullsLast() , nullsFirst() : null 데이터 순서 부여
    * */


    /* 페이징 */
    // 조회 건수 제한
    @Test
    public void paging1() {
        List<Member> result = jpaQueryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1) //0부터 시작(zero index)
                .limit(2) //최대 2건 조회
                .fetch();
        assertThat(result.size()).isEqualTo(2);
    }

    // 전체 조회 수가 필요하면?
    @Test
    public void paging2() {
        QueryResults<Member> queryResults = jpaQueryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();

        assertThat(queryResults.getTotal()).isEqualTo(4);
        assertThat(queryResults.getLimit()).isEqualTo(2);
        assertThat(queryResults.getOffset()).isEqualTo(1);
        assertThat(queryResults.getResults().size()).isEqualTo(2);
    }

    /**
     * JPQL
     * select
     * COUNT(m), //회원수
     * SUM(m.age), //나이 합
     * AVG(m.age), //평균 나이
     * MAX(m.age), //최대 나이
     * MIN(m.age) //최소 나이
     * from Member m
     */
    @Test
    public void aggregation() throws Exception {
        List<Tuple> result = jpaQueryFactory
                .select(member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                ).from(member)
                .fetch();

        Tuple tuple = result.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    /**
     * 팀의 이름과 각 팀의 평균 연령을 구해라.
     */
    @Test
    public void group() throws Exception {
        List<Tuple> result = jpaQueryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();
        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);
        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);
        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);
    }

    /*
     * 팀 A에 소속된 모든 회원
     * */
    // 조인 기본 예제
    @Test
    public void join() throws Exception {
        List<Member> findMemberByTeam = jpaQueryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();


            for (Member member : findMemberByTeam) {
                System.out.println("member = " + member);
            }
        
    }

    /**
     * 세타 조인(연관관계가 없는 필드로 조인)
     * 회원의 이름이 팀 이름과 같은 회원 조회
     */
    @Test
    public void theta_join() throws Exception {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));
        List<Member> result = jpaQueryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        System.out.println("result = " + result);
    }

    /*
    * 예) 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
    * */
    @Test
    public void join_on_filtering(){
        List<Tuple> result = jpaQueryFactory
                .select(member, team)
                .from(member)
                .leftJoin(
                        member.team, team
                ).on(team.name.eq("teamA"))
                .fetch();
        
        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }
    /**
     * 2. 연관관계 없는 엔티티 외부 조인
     * 예) 회원의 이름과 팀의 이름이 같은 대상 외부 조인
     * JPQL: SELECT m, t FROM Member m LEFT JOIN Team t on m.username = t.name
     * SQL: SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.username = t.name
     */
    @Test
    public void join_on_no_relation() throws Exception {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        List<Tuple> result = jpaQueryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team).on(member.username.eq(team.name))
                .fetch();
        for (Tuple tuple : result) {
            System.out.println("t=" + tuple);
        }
    }


    //페치 조인은 SQL에서 제공하는 기능은 아니다. SQL조인을 활용해서 연관된 엔티티를 SQL 한번에 조회하는 기능이
    //다. 주로 성능 최적화에 사용하는 방법이다.

    /*
    * 페치 조인 미적용
        지연로딩으로 Member, Team SQL 쿼리 각각 실행
    * */
    @Test
    public void fetchJoinNo() throws Exception {
        em.flush();
        em.clear();
        Member findMember = jpaQueryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();
        boolean loaded =
                emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치 조인 미적용").isFalse();
    }

/*
    *  페치 조인 적용
       즉시로딩으로 Member, Team SQL 쿼리 조인으로 한번에 조회
    *
* */
    @Test
    public void fetchJoinUse() throws Exception {
        em.flush();
        em.clear();
        Member findMember = jpaQueryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();
        boolean loaded =
                emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치 조인 적용").isTrue();
    }

    
    /*
    * 서브 쿼리
    * 나이가 가장 많은 회원 조회
    * */
    @Test
    public void subQuery(){
        QMember memberSub = new QMember("memberSub");
        
        Member findOldestMember = jpaQueryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        JPAExpressions
                                .select(member.age.max())
                                .from(member)
                )).fetchOne();

        System.out.println("findOldestMember = " + findOldestMember);
    }

    /*
    * 나이가 평균 나이 이상인 회원
    * */
    @Test
    public void subQueryGoe(){
        List<Member> findMoreOldestMemberThanAvg = jpaQueryFactory
                .selectFrom(member)
                /* member.age >= member.age.avg*/
                .where(member.age.goe(
                        JPAExpressions
                                .select(member.age.avg())
                                .from(member)
                )).fetch();

                for(Member member : findMoreOldestMemberThanAvg){
                    System.out.println("member = " + member);
                }
    }
    /**
     * 서브쿼리 여러 건 처리, in 사용
     */
    @Test
    public void subQueryIn() throws Exception {
        QMember memberSub = new QMember("memberSub");
        List<Member> result = jpaQueryFactory
                .selectFrom(member)
                .where(member.age.in(
                        JPAExpressions
                                .select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.gt(10))
                ))
                .fetch();

        for (Member member : result) {
            System.out.println("member = " + member);
        }
    }

    /*
    * Select 절에 서브 쿼리
    * */
    @Test
    public void subQueryInSelect(){
        List<Tuple> fetch = jpaQueryFactory
                .select(member.username,
                        JPAExpressions
                                .select(member.age.avg())
                                .from(member)
                ).from(member)
                .fetch();
        for (Tuple tuple : fetch) {
            System.out.println("username = " + tuple.get(member.username));
            System.out.println("age = " +
                    tuple.get(JPAExpressions.select(member.age.avg())
                            .from(member)));
        }
    }

    /*
    * from 절의 서브쿼리 한계
        JPA JPQL 서브쿼리의 한계점으로 from 절의 서브쿼리(인라인 뷰)는 지원하지 않는다. 당연히 Querydsl도 지원하지
        않는다. 하이버네이트 구현체를 사용하면 select 절의 서브쿼리는 지원한다. Querydsl도 하이버네이트 구현체를 사용
        하면 select 절의 서브쿼리를 지원한다.
        *
        from 절의 서브쿼리 해결방안
            1. 서브쿼리를 join으로 변경한다. (가능한 상황도 있고, 불가능한 상황도 있다.)
            2. 애플리케이션에서 쿼리를 2번 분리해서 실행한다.
            3. nativeSQL을 사용한다.
    * */


    /*
    * Case 문
    * */
    @Test
    public void basicCase(){
        List<String> result = jpaQueryFactory
                .select(member.age
                        .when(10).then("열살")
                        .when(20).then("스무살")
                        .otherwise("기타")
                ).from(member)
                .fetch();

        for(String s : result){
            System.out.println("s = " + s);
        }
    }

    /*
    * 복잡한 케이스
    * */
    @Test
    public void complexCase(){
        List<String> result = jpaQueryFactory
                .select(new CaseBuilder()
                        .when(member.age.between(0, 20)).then("0~20살")
                        .when(member.age.between(21, 30)).then("21~30살")
                        .otherwise("기타")
                ).from(member)
                .fetch();
        
        for(String s : result){
            System.out.println("s = " + s);
        }
    }


    /*
    * 임의의 순서로 회원을 출력하고 싶은 경우
    *   1. 0 ~ 30살이 아닌 회원을 가장 먼저 출력
        2. 0 ~ 20살 회원 출력
        3. 21 ~ 30살 회원 출력
    * */
    @Test
    public void optionalOrderCase (){
        NumberExpression<Integer> rankPath = new CaseBuilder()
                .when(member.age.between(0, 20)).then(2)
                .when(member.age.between(21, 30)).then(1)
                .otherwise(3);
        List<Tuple> result = jpaQueryFactory
                .select(member.username, member.age, rankPath)
                .from(member)
                .orderBy(rankPath.desc())
                .fetch();
        for (Tuple tuple : result) {
            String username = tuple.get(member.username);
            Integer age = tuple.get(member.age);
            Integer rank = tuple.get(rankPath);
            System.out.println("username = " + username + " age = " + age + " rank = " +
                    rank);
        }
    }

    /*
    * 상수, 문자 더하기
    * */

    /*
    * 상수가 필요하면 Expresiion.constant() 사용
    * */
    @Test
    public void constantExample(){
        Tuple result = jpaQueryFactory
                .select(member.username, Expressions.constant("A"))
                .from(member)
                .fetchOne();

        System.out.println("result = " + result);
    }


    /*
    * 문자 더하기 concat
    * */
    @Test
    public void concatStringExample () {
        String result = jpaQueryFactory
                .select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        System.out.println("result = " + result);
    }
    /*
    * member.age.stringValue() 부분이 중요한데, 문자가 아닌 다른 타입들은 stringValue() 로 문
자로 변환할 수 있다. 이 방법은 ENUM을 처리할 때도 자주 사용한다
    * */
}
