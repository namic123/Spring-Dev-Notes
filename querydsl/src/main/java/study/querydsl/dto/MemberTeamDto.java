package study.querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

/**
 * {@code MemberTeamDto}는 회원(Member)과 팀(Team)의 정보를 함께 담기 위한 DTO 클래스입니다.
 *
 * <p>Querydsl의 {@link com.querydsl.core.annotations.QueryProjection}을 활용하여,
 * Querydsl 쿼리에서 생성자 기반으로 직접 DTO에 데이터를 매핑할 수 있도록 설계되었습니다.</p>
 *
 * <p>주로 성능 최적화를 위해 엔티티 전체를 조회하지 않고 필요한 필드만 가져오는 조회 쿼리에 사용됩니다.</p>
 *
 * @author 박재성
 * @version 1.0
 * @since 2025-04-09
 */
@Data
public class MemberTeamDto {

    /** 회원 ID */
    private Long memberId;

    /** 회원 이름 */
    private String username;

    /** 회원 나이 */
    private int age;

    /** 소속 팀 ID */
    private Long teamId;

    /** 소속 팀 이름 */
    private String teamName;

    /**
     * {@code MemberTeamDto} 생성자.
     *
     * <p>{@link QueryProjection}이 적용되어 있어 Querydsl의 생성자 기반 프로젝션으로 사용할 수 있습니다.</p>
     *
     * @param memberId 회원 ID
     * @param username 회원 이름
     * @param age      회원 나이
     * @param teamId   소속 팀 ID
     * @param teamName 소속 팀 이름
     */
    @QueryProjection
    public MemberTeamDto(Long memberId, String username, int age, Long teamId, String teamName) {
        this.memberId = memberId;
        this.username = username;
        this.age = age;
        this.teamId = teamId;
        this.teamName = teamName;
    }
}
