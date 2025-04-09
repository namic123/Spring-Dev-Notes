package study.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.expression.spel.ast.Projection;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.QMemberDto;
import study.querydsl.dto.UserDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.*;

@SpringBootTest
@Transactional
public class QuerydslDeependingTests {

    @Autowired
    EntityManager em;

    JPAQueryFactory jpaQueryFactory;



    @BeforeEach
    public void setUp(){
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
    * 프로젝션
    * DB의 필요한 속성만을 조회하는 것을 프로젝션이라고 함
        즉 select 절의 지정 대상이라고 볼 수 있음
    */

    // 프로젝션 대상이 하나인 경우
    @Test
    public void oneProjection(){

        // 프로젝션 대상이 하나면 타입을 명확하게 지정할 수 있음
        List<String> result = jpaQueryFactory
                .select(member.username)
                .from(member)
                .fetch();
    }

    // 프로젝션 대상이 둘인 경우
    // 프로젝션 대상이 둘 이상이면 튜플이나 DTO로 조회
    // 튜플 조회
    // 튜플은 가급적이면, repository에서만 활용
    @Test
    public void tupleProjection(){

        List<Tuple> tupleList = jpaQueryFactory
                .select(member.username, member.age)
                .from(member)
                .fetch();
    }

    /* Entity to DTO 변환 예제 */
    /* Setter 방식 */
    @Test
    public void entityToDtoBySetter(){
        List<MemberDto> result = jpaQueryFactory
                .select(Projections.bean(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for(MemberDto memberDto : result){
            System.out.println("memberDto.toString() = " + memberDto.toString());
        }
    }


    // 필드 직접 접근
    @Test
    public void entityToDtoByField(){
        List<MemberDto> result =jpaQueryFactory
                .select(Projections.fields(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for(MemberDto memberDto : result){
            System.out.println("memberDto.toString() = " + memberDto.toString());
        }
    }


    /* 엔티티 속성과 필드의 별칭이 다를 때 */
    @Test
    public void entityToDtoByAlias(){
        List<UserDto> fetch = jpaQueryFactory
                .select(Projections.fields(UserDto.class,
                        member.username.as("name"),
                        ExpressionUtils.as(
                                JPAExpressions
                                        .select(member.age.max())
                                        .from(member), "age"
                        )))
                .from(member)
                .fetch();

        for(UserDto userDto : fetch){
            System.out.println("userDto.toString() = " + userDto.toString());
        }
    }

    // 생성자 사용
    @Test
    public void entityToDtoByConstructor(){
        List<MemberDto> result = jpaQueryFactory
                .select(Projections.constructor(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for(MemberDto memberDto : result){
            System.out.println("memberDto.toString() = " + memberDto.toString());
        }
    }

    // QueryProjection 활용

    @Test
    public void entityToDtoByQueryProjection(){
        List<MemberDto> result = jpaQueryFactory
                .select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch();

        System.out.println("result.toString() = " + result.toString());
    }


    // 동적 쿼리 - BooleanBuilder
    @Test
    public void dynamicQuery_BooleanBuilder(){
        String usernameParam = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMember1(usernameParam, ageParam);
        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember1(String usernameCond, Integer ageCond) {
        BooleanBuilder builder = new BooleanBuilder();

        if(usernameCond != null){
            builder.and(member.username.eq(usernameCond));
        }

        if(ageCond != null){
            builder.and(member.age.eq(ageCond));
        }

        return jpaQueryFactory
                .selectFrom(member)
                .where(builder)
                .fetch();
    }


    // 동적 쿼리 - Where 다중 파라미터 사용
    @Test
    public void dynamicQuery_WhereParam() throws Exception{
        String usernameParam = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMember2(usernameParam, ageParam);

        assertThat(result.size()).isEqualTo(1);
    }


    // Sql Function 예제

    @Test
    public void sqlFunction(){
        List<String> fetch = jpaQueryFactory
                .select(Expressions.stringTemplate("function('replace',{0}, {1}, {2})", member.username, "member", "M"))
                .from(member)
                .fetch();

        for (String string : fetch) {
            System.out.println(string);
        }
    }

    // 소문자 변경 비교
    @Test
    public void sqlFunction2(){
        jpaQueryFactory
                .select(member.username)
                .from(member)
//                .where(member.username.eq(Expressions.stringTemplate("function('lower', {0})", member.username)))
                .where(member.username.eq(member.username.lower()))
                .fetch();

    }

    private List<Member> searchMember2(String usernameParam, Integer ageParam) {
        return jpaQueryFactory
                .selectFrom(member)
                .where(usernameEq(usernameParam), ageEq(ageParam))
                .fetch();
    }

    private BooleanExpression usernameEq(String usernameCond){
        return usernameCond != null ? member.username.eq(usernameCond) : null;
    }

    private BooleanExpression ageEq(Integer ageCond){
        return ageCond != null ? member.age.eq(ageCond) : null;
    }

    // usernameEq, ageEq 조합해서 활용
    private BooleanExpression allEq(String usernameCond, Integer ageCond){
        return usernameEq(usernameCond).and(ageEq(ageCond));
    }


    // 벌크 연산시, 초기화 필수(영속성 컨텍스트의 내용과 DB의 내용이 다를 수 있기 때문이다.)

}
