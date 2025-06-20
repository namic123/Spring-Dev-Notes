package spring.jdbc.repository;

import org.junit.jupiter.api.Test;
import spring.jdbc.domain.Member;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class MemberRepositoryV0Test {

    MemberRepositoryV0 repository = new MemberRepositoryV0();

    @Test
    void crud() throws SQLException {
        Member member = new Member("memberV0", 10000);

        repository.save(member);
    }
}