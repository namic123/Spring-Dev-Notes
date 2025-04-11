## Spring Data JPA와 Querydsl을 활용한 페이징 처리 및 성능 최적화
블로그 : https://pjs-world.tistory.com/entry/Spring-Data-JPA%EC%99%80-Querydsl%EC%9D%84-%ED%99%9C%EC%9A%A9%ED%95%9C-%ED%8E%98%EC%9D%B4%EC%A7%95-%EC%B2%98%EB%A6%AC-%EB%B0%8F-%EC%84%B1%EB%8A%A5-%EC%B5%9C%EC%A0%81%ED%99%94

### 📌 목차

[1\. 사용자 정의 리포지토리에서 페이징 메서드 선언](#custom-interface) [2\. fetchResults()를 이용한 단순 페이징 방식](#simple-paging) [3\. content와 count 쿼리 분리 방식](#complex-paging) [4\. PageableExecutionUtils를 이용한 고급 최적화](#paging-utils) [5\. 정렬 처리: Sort → OrderSpecifier 변환](#sort-conversion) [6\. 실제 컨트롤러에서의 적용](#controller-example) [7\. 요약 정리](#summary)

## 1\. 사용자 정의 리포지토리에서 페이징 메서드 선언

페이징 기능을 지원하기 위해서는 사용자 정의 리포지토리에 다음과 같은 메서드를 선언해야 한다.

```
public interface MemberRepositoryCustom {
    Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable);
    Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable);
}
```

이처럼 Pageable 객체를 파라미터로 받아 Page<T> 결과를 반환하면, 컨트롤러에서 페이지, 크기, 정렬 등을 유연하게 처리할 수 있다.

## 2\. fetchResults()를 이용한 단순 페이징 방식

```
@Override
public Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition,
	Pageable pageable) {
 	QueryResults<MemberTeamDto> results = queryFactory
 		.select(new QMemberTeamDto(
						member.id,
 						member.username,
 						member.age,
 						team.id,
 						team.name))
 		.from(member)
 		.leftJoin(member.team, team)
 		.where(usernameEq(condition.getUsername()),
 				teamNameEq(condition.getTeamName()),
 				ageGoe(condition.getAgeGoe()),
 				ageLoe(condition.getAgeLoe()))
 		.offset(pageable.getOffset())
 		.limit(pageable.getPageSize())
 		.fetchResults(); // ✅ content + count 둘 다 처리
 
 	List<MemberTeamDto> content = results.getResults();
 
 	long total = results.getTotal();
 
 	return new PageImpl<>(content, pageable, total);
}
```

Querydsl의 fetchResults()는 content 조회와 count 조회를 한 번에 처리할 수 있는 간편한 방법이다.

이 방식은 내부적으로 **두 개의 쿼리가 실행되며(content 조회와 count 조회),** PageImpl을 통해 결과를 반환한다.

장점은 코드의 간결성이고, 단점은 항상 쿼리가 두 번 실행되며, 복잡한 조인 구조에서는 성능 저하를 초래할 수 있다는 점이다.

## 3\. content와 count 쿼리 분리 방식

복잡한 페이징 로직이 필요한 경우에는 content 쿼리와 count 쿼리를 명시적으로 분리하는 것이 성능 면에서 유리하다.

```
@Override
public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition,
Pageable pageable) {

// content 쿼리
List<MemberTeamDto> content = queryFactory
    .select(new QMemberTeamDto(...))
    .from(member)
    .leftJoin(member.team, team)
    .where( /* 조건 */ )
    .offset(pageable.getOffset())
    .limit(pageable.getPageSize())
    .fetch();

// count 쿼리
long total = queryFactory
    .select(member.count())
    .from(member)
    .leftJoin(member.team, team)
    .where( /* 조건 */ )
    .fetchOne();
    
    return new PageImpl<>(content, pageable, total)
  }
```

이 방식은 count 쿼리에서 불필요한 join을 제거하거나 단순화할 수 있기 때문에 성능 최적화에 유리하다.

## 4\. PageableExecutionUtils를 이용한 고급 최적화

Spring Data가 제공하는 PageableExecutionUtils.getPage() 메서드는 조건에 따라 count 쿼리를 생략할 수 있어 유용하다.

```
 JPAQuery<Member> countQuery = queryFactory
 		.select(member)
 		.from(member)
 		.leftJoin(member.team, team)
 		.where(usernameEq(condition.getUsername()),
 				teamNameEq(condition.getTeamName()),
				ageGoe(condition.getAgeGoe()),
				ageLoe(condition.getAgeLoe()));
			// return new PageImpl<>(content, pageable, total);
 		return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchCount);
```

다음과 같은 조건에서는 count 쿼리가 생략된다

-   첫 페이지(page = 0)이고, 결과 수 < 페이지 크기
-   마지막 페이지이고, 결과 수 < 페이지 크기

## 5\. 정렬 처리: Sort → OrderSpecifier 변환

Pageable 객체가 포함하는 정렬 조건은 Querydsl의 OrderSpecifier로 수동 변환해야 한다.

```
for (Sort.Order o : pageable.getSort()) {
    PathBuilder pathBuilder = new PathBuilder(member.getType(), member.getMetadata());
    query.orderBy(new OrderSpecifier(o.isAscending() ? Order.ASC : Order.DESC,
            pathBuilder.get(o.getProperty())));
}
```

단순 필드 정렬은 이 방식으로 처리할 수 있지만, 연관된 엔티티 필드에 대한 정렬은 커스텀 처리하는 것이 좋다.

## 6\. 실제 컨트롤러에서의 적용

다음은 페이징 검색 기능을 실제 REST 컨트롤러에 적용한 예시이다.

```
@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberRepository memberRepository;

    @GetMapping("/v2/members")
    public Page<MemberTeamDto> searchMemberV2(MemberSearchCondition condition, Pageable pageable) {
        return memberRepository.searchPageSimple(condition, pageable);
    }

    @GetMapping("/v3/members")
    public Page<MemberTeamDto> searchMemberV3(MemberSearchCondition condition, Pageable pageable) {
        return memberRepository.searchPageComplex(condition, pageable);
    }
}
```

이 방식은 /v2/members?page=0&size=5&sort=username,desc와 같은 쿼리 파라미터로 호출이 가능하다.

## 7\. 요약 정리

| **방식** | **설명** | **장점** | **단점** |
| --- | --- | --- | --- |
| **fetchResults()** | content + count 자동 처리 | 간편함 | 쿼리 2번, 성능 최적화 어려움 |
| **분리 방식** | content, count 쿼리 수동 분리 | 성능 최적화 유리 | 코드 중복 발생 |
| **PageableExecutionUtils** | count 생략 판단 로직 포함 | 성능 최적화 가능 | 사용 조건 제한 |
| **Sort 수동 변환** | 정렬 유연하게 적용 가능 | 복잡한 정렬 처리 가능 | 구현 복잡도 증가 |

결론적으로, 간단한 페이지 처리에는 fetchResults()가 편리하지만, 실무에서는 PageableExecutionUtils 또는 content/count 분리 전략을 사용하여 성능을 최적화하는 것이 바람직하다.