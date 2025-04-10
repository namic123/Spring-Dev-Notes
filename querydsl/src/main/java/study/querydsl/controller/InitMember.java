package study.querydsl.controller;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;


/**
 * 애플리케이션 실행 시 로컬 프로파일에서만 샘플 데이터를 초기화하는 컴포넌트 클래스입니다.
 *
 * @author 박재성
 * @version 1.0
 * @since 2025-04-10
 */
@Profile("local")
@Component
@RequiredArgsConstructor
public class InitMember {

    private final InitMemberService initMemberService;

    /**
     * 스프링 컨테이너가 초기화된 후 자동 실행되어 샘플 데이터를 등록합니다.
     */
    @PostConstruct
    public void init() {
        initMemberService.init();
    }

    /**
     * 샘플 데이터를 실제로 초기화하는 내부 서비스 클래스입니다.
     * 로컬 프로파일에서만 사용되며, 트랜잭션 안에서 실행됩니다.
     */
    @Component
    static class InitMemberService {

        @PersistenceContext
        EntityManager em;

        /**
         * 팀 2개(teamA, teamB)와 회원 100명을 생성 및 저장합니다.
         * 짝수 인덱스는 teamA, 홀수 인덱스는 teamB에 배정됩니다.
         */
        @Transactional
        public void init() {
            Team teamA = new Team("teamA");
            Team teamB = new Team("teamB");

            em.persist(teamA);
            em.persist(teamB);

            for (int i = 0; i < 100; i++) {
                Team selectedTeam = (i % 2 == 0) ? teamA : teamB;
                em.persist(new Member("member" + i, i, selectedTeam));
            }
        }
    }
}