package study.querydsl.dto;

import lombok.Data;

/**
 * {@code MemberSearchCondition} 클래스는 회원 및 팀 정보를 조건에 따라 검색하기 위한
 * 필터 조건들을 담는 DTO입니다.
 *
 * <p>이 객체는 사용자의 입력에 따라 동적으로 쿼리 조건을 구성할 수 있도록 도와주며,
 * Querydsl의 동적 쿼리 기능과 함께 사용됩니다.</p>
 *
 * <p>주로 이름, 팀명, 나이 범위(ageGoe ~ ageLoe) 등을 조건으로 검색할 때 사용됩니다.</p>
 *
 * <ul>
 *   <li>{@code username} - 회원 이름 (일치 조건)</li>
 *   <li>{@code teamName} - 팀 이름 (일치 조건)</li>
 *   <li>{@code ageGoe} - 나이의 최소값 (greater or equal)</li>
 *   <li>{@code ageLoe} - 나이의 최대값 (less or equal)</li>
 * </ul>
 *
 * @author 박재성
 * @version 1.0
 * @since 2025-04-09
 */
@Data
public class MemberSearchCondition {

    /** 회원 이름 (username equals 조건) */
    private String username;

    /** 팀 이름 (team name equals 조건) */
    private String teamName;

    /** 나이 조건 - 이상 (greater or equal) */
    private Integer ageGoe;

    /** 나이 조건 - 이하 (less or equal) */
    private Integer ageLoe;
}
