package study.querydsl.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;

import java.util.List;
import java.util.Optional;

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
    public MemberJpaRepository(EntityManager em){
        this.em = em;
        this.jpaQueryFactory = new JPAQueryFactory(em);
    }

    /**
     * {@link Member} 엔티티를 데이터베이스에 저장합니다.
     *
     * @param member 저장할 {@link Member} 객체
     */
    public void save(Member member){
        em.persist(member);
    }

    /**
     * ID를 기반으로 {@link Member} 엔티티를 조회합니다.
     *
     * @param id 조회할 회원의 ID
     * @return 조회된 {@link Member} 객체를 담은 {@link Optional}
     */
    public Optional<Member> findById(Long id){
        Member findMember = em.find(Member.class, id);
        return Optional.ofNullable(findMember);
    }

    /**
     * 모든 {@link Member} 엔티티를 조회합니다.
     *
     * @return 전체 회원 목록
     */
    public List<Member> findAll(){
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
    public List<Member> findAll_Querydsl(){
        return jpaQueryFactory
                .selectFrom(QMember.member)
                .fetch();
    }

    /**
     * Querydsl을 사용하여 사용자 이름으로 {@link Member} 엔티티를 조회합니다.
     *
     * @param username 조회할 사용자 이름
     * @return 사용자 이름에 해당하는 회원 목록
     */
    public List<Member> findByUsername_Querydsl(String username){
        return jpaQueryFactory
                .selectFrom(QMember.member)
                .where(QMember.member.username.eq(username))
                .fetch();
    }
}
