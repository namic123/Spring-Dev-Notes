package study.querydsl.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;

import java.util.List;
import java.util.Optional;

import static study.querydsl.entity.QMember.*;
import static study.querydsl.entity.QTeam.*;

/**
 * {@code MemberJpaRepository}는 순수 JPA 및 Querydsl을 활용하여
 * {@link Member} 엔티티에 대한 데이터 접근 기능을 제공하는 리포지토리 클래스입니다.
 *
 * <p>JPA의 기본적인 CRUD 기능 외에도, Querydsl을 활용한 동적 쿼리 메서드를 함께 제공합니다.</p>
 *
 * @author 박재성
 * @version 1.0
 * @since 2025-04-09
 */
@Repository
public class MemberJpaRepository {

    private final EntityManager em;
    private final JPAQueryFactory jpaQueryFactory;

    /**
     * {@code MemberJpaRepository} 생성자.
     *
     * @param em JPA {@link EntityManager} 인스턴스
     */
    public MemberJpaRepository(EntityManager em) {
        this.em = em;
        this.jpaQueryFactory = new JPAQueryFactory(em);
    }

    /**
     * {@link Member} 엔티티를 데이터베이스에 저장합니다.
     *
     * @param member 저장할 {@link Member} 객체
     */
    public void save(Member member) {
        em.persist(member);
    }

    /**
     * ID를 기반으로 {@link Member} 엔티티를 조회합니다.
     *
     * @param id 조회할 회원의 ID
     * @return 조회된 {@link Member} 객체를 담은 {@link Optional}
     */
    public Optional<Member> findById(Long id) {
        Member findMember = em.find(Member.class, id);
        return Optional.ofNullable(findMember);
    }

    /**
     * 모든 {@link Member} 엔티티를 조회합니다.
     *
     * @return 전체 회원 목록
     */
    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class).getResultList();
    }

    /**
     * 사용자 이름을 기준으로 {@link Member} 엔티티 목록을 조회합니다.
     *
     * @param username 조회할 사용자 이름
     * @return 사용자 이름에 해당하는 회원 목록
     */
    public List<Member> findByUsername(String username) {
        return em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", username)
                .getResultList();
    }

    /**
     * Querydsl을 사용하여 모든 {@link Member} 엔티티를 조회합니다.
     *
     * @return 전체 회원 목록
     */
    public List<Member> findAll_Querydsl() {
        return jpaQueryFactory
                .selectFrom(member)
                .fetch();
    }

    /**
     * Querydsl을 사용하여 사용자 이름으로 {@link Member} 엔티티를 조회합니다.
     *
     * @param username 조회할 사용자 이름
     * @return 사용자 이름에 해당하는 회원 목록
     */
    public List<Member> findByUsername_Querydsl(String username) {
        return jpaQueryFactory
                .selectFrom(member)
                .where(member.username.eq(username))
                .fetch();
    }
    /**
     * 사용자가 입력한 검색 조건에 따라 동적으로 WHERE 절을 구성하여,
     * 회원(Member)과 팀(Team)의 정보를 조회하고 DTO로 반환하는 메서드입니다.
     *
     * <p>Querydsl의 BooleanBuilder를 사용하여 조건이 존재하는 경우에만 해당 조건을 추가함으로써
     * 효율적인 동적 쿼리를 생성합니다.</p>
     *
     * @param memberSearchCondition 사용자로부터 전달받은 검색 조건 객체
     *                              (username, teamName, ageGoe, ageLoe)
     * @return 검색 조건에 해당하는 결과를 담은 {@link MemberTeamDto} 리스트
     */
    public List<MemberTeamDto> searchByBuilder(MemberSearchCondition memberSearchCondition) {

        // BooleanBuilder를 이용해 조건을 동적으로 조립
        BooleanBuilder builder = new BooleanBuilder();

        // 회원 이름(username)이 입력된 경우 조건에 추가
        if (StringUtils.hasText(memberSearchCondition.getUsername())) {
            builder.and(member.username.eq(memberSearchCondition.getUsername()));
        }

        // 팀 이름(teamName)이 입력된 경우 조건에 추가
        if (StringUtils.hasText(memberSearchCondition.getTeamName())) {
            builder.and(team.name.eq(memberSearchCondition.getTeamName()));
        }

        // 최소 나이 조건(ageGoe)이 입력된 경우 조건에 추가
        if (memberSearchCondition.getAgeGoe() != null) {
            builder.and(member.age.goe(memberSearchCondition.getAgeGoe()));
        }

        // 최대 나이 조건(ageLoe)이 입력된 경우 조건에 추가
        if (memberSearchCondition.getAgeLoe() != null) {
            builder.and(member.age.loe(memberSearchCondition.getAgeLoe()));
        }

        // 조건이 조립된 BooleanBuilder를 where 절에 적용하여 조회 실행
        return jpaQueryFactory
                .select(new QMemberTeamDto(
                        member.id,           // 회원 ID
                        member.username,     // 회원 이름
                        member.age,          // 회원 나이
                        team.id,             // 팀 ID
                        team.name))          // 팀 이름
                .from(member)
                .leftJoin(member.team, team) // 회원과 팀을 left join
                .where(builder)              // 동적 조건 적용
                .fetch();                    // 결과 조회 및 반환
    }

    public List<MemberTeamDto> search(MemberSearchCondition memberSearchCondition) {
        return jpaQueryFactory
                .select(new QMemberTeamDto(
                        member.id,
                        member.username,
                        member.age,
                        team.id,
                        team.name
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(memberSearchCondition.getUsername()),
                        teamNameEq(memberSearchCondition.getTeamName()),
                        ageGoe(memberSearchCondition.getAgeGoe()),
                        ageLoe(memberSearchCondition.getAgeLoe()))
                .fetch();
    }

    private BooleanExpression usernameEq(String username) {
        return StringUtils.isEmpty(username) ? null : member.username.eq(username);
    }
    private BooleanExpression teamNameEq(String teamName) {
        return StringUtils.isEmpty(teamName) ? null : team.name.eq(teamName);
    }
    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe == null ? null : member.age.goe(ageGoe);
    }
    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe == null ? null : member.age.loe(ageLoe);
    }
}
