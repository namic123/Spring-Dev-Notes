package spring.jdbc.repository;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import spring.jdbc.domain.Member;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class MemberRepositoryV0Test {

    MemberRepositoryV0 repository = new MemberRepositoryV0();

    @Test
    void crud() throws SQLException {
        // save
        Member member = new Member("memberV2", 10000);

        repository.save(member);

        // find by Id
        Member findMember = repository.findById(member.getMemberId());
        log.info("findMember={}", findMember);
        assertEquals(findMember, member);

        repository.update(member.getMemberId(), 20000);
        Member updateMember = repository.findById(member.getMemberId());
        assertEquals(updateMember.getMoney(), 20000);

        repository.delete(member.getMemberId());
        assertThrows(NoSuchElementException.class, () -> repository.findById(member.getMemberId()));
    }
}