package study.querydsl.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 본 테스트 클래스는 {@link MemberJpaRepository}에 대한 기본적인 CRUD 동작과
 * Querydsl을 이용한 쿼리 메서드가 정상적으로 동작하는지 검증하기 위한 테스트를 포함하고 있다.
 *
 * 테스트는 스프링 부트 테스트 환경에서 수행되며, 각 테스트는 트랜잭션 범위 내에서 실행되며
 * 테스트 종료 시 롤백된다.
 *
 * @author 박재성
 * @version 1.0
 * @since 2025-04-09
 */
@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    // JPA의 EntityManager를 직접 주입받아 사용할 수 있도록 설정함
    @Autowired
    EntityManager em;

    // 테스트 대상이 되는 사용자 정의 JPA 리포지토리
    @Autowired
    MemberJpaRepository memberJpaRepository;

    /**
     * 본 테스트 메서드는 순수 JPA 기반의 {@link MemberJpaRepository} 기능을 검증하기 위한 것이다.
     * - 회원 저장
     * - ID 기반 단건 조회
     * - 전체 회원 조회
     * - 사용자명(username)으로 회원 조회
     *
     * 각 기능이 정상 동작하는지 {@code assertThat()}을 통해 확인한다.
     */
    @Test
    public void basicTest() {
        // 테스트를 위한 회원 객체 생성 및 저장
        Member member = new Member("member1", 10);
        memberJpaRepository.save(member);

        // 저장된 회원을 ID로 조회하고, 원본 객체와 동일한지 검증
        Member findMember = memberJpaRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        // 전체 회원 목록 조회 후 저장한 회원이 포함되어 있는지 확인
        List<Member> result1 = memberJpaRepository.findAll();
        assertThat(result1).containsExactly(member);

        // 사용자명으로 회원을 조회한 결과가 예상과 일치하는지 검증
        List<Member> result2 = memberJpaRepository.findByUsername("member1");
        assertThat(result2).containsExactly(member);
    }

    /**
     * 본 테스트는 Querydsl을 활용한 쿼리 메서드의 동작을 검증하기 위한 것이다.
     * - Querydsl 기반 전체 회원 조회
     * - Querydsl 기반 사용자명으로 회원 조회
     *
     * JPA 방식과 동일한 방식으로 테스트를 구성하되,
     * 내부 쿼리 실행은 Querydsl로 수행된다.
     */
    @Test
    public void basicQuerydslTest() {
        // 테스트용 회원 등록
        Member member = new Member("member1", 10);
        memberJpaRepository.save(member);

        // 저장된 회원이 정상적으로 조회되는지 확인
        Member findMember = memberJpaRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        // Querydsl을 사용한 전체 조회 테스트
        List<Member> result1 = memberJpaRepository.findAll_Querydsl();
        assertThat(result1).containsExactly(member);

        // Querydsl을 사용한 사용자명 조건 조회 테스트
        List<Member> result2 = memberJpaRepository.findByUsername_Querydsl("member1");
        assertThat(result2).containsExactly(member);
    }


    /**
     * {@code searchTest}는 {@link MemberJpaRepository#searchByBuilder(MemberSearchCondition)}
     * 메서드의 동적 검색 기능을 테스트합니다.
     *
     * 이 테스트는 팀 이름과 나이 범위 조건에 따라 {@code member4}만 검색되는지를 검증합니다.
     *
     * 조건:
     *  팀 이름: "teamB"
     *  나이: 35 이상, 40 이하
     *
     *
     *기대 결과: {@code username = "member4"}인 한 명의 결과만 반환되어야 함</p>
     */
    @Test
    public void searchTest() {
        // 팀 생성 및 영속화
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        // 회원 4명 생성 및 팀에 배정
        Member member1 = new Member("member1", 10, teamA); // age 10, teamA
        Member member2 = new Member("member2", 20, teamA); // age 20, teamA
        Member member3 = new Member("member3", 30, teamB); // age 30, teamB
        Member member4 = new Member("member4", 40, teamB); // age 40, teamB
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        // 검색 조건 설정: 팀 이름 "teamB", 나이 35세 이상 40세 이하
        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(35);
        condition.setAgeLoe(40);
        condition.setTeamName("teamB");

        // 동적 조건 기반 검색 실행
        List<MemberTeamDto> result = memberJpaRepository.searchByBuilder(condition);

        // 결과 검증: "member4"만 포함되어야 함
        assertThat(result).extracting("username").containsExactly("member4");
    }



}
