package spring.jdbc.repository;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import spring.jdbc.domain.Member;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class MemberRepositoryV0Test {

    MemberRepositoryV0 repository = new MemberRepositoryV0();

    @Test
    void crud() throws SQLException {
        // save
        Member member = new Member("memberV0", 10000);

        repository.save(member);

        // find by Id
        Member findMember = repository.findById(member.getMemberId());
        log.info("findMeber={}", findMember);
        assertEquals(findMember, member);
    }
}