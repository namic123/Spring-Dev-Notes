package spring.jdbc.repository;

import lombok.extern.slf4j.Slf4j;
import spring.jdbc.domain.Member;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import static spring.jdbc.connection.DBConnectionUtil.*;


@Slf4j
public class MemberRepositoryV0 {

    public Member save(Member member) throws SQLException {
        String sql = "insert into member (member_id, money) values (?,?)";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            /* 커넥션 획득 */
            conn = getConnection();
            /* sql 전달을 위한 PerparedStatment 객체 */
            pstmt = conn.prepareStatement(sql);
            /* SQL Values 와일드카드에 값 세팅 */
            pstmt.setString(1, member.getMemberId());
            pstmt.setDouble(2, member.getMoney());
            /* SQL 실행 */
            pstmt.executeUpdate();
            return member;
        }catch (SQLException e) {
            log.error("db error :{}", e.getMessage());
            throw e;
        }finally {
            close(conn, pstmt, null);
        }
    }
    private void close(Connection conn, PreparedStatement pstmt, ResultSet rs) {

        if(rs != null) {
            try {
                rs.close();
            }catch (SQLException e) {
                log.error("db error :{}", e.getMessage());
            }
        }
        if (pstmt != null) {
            try {
                pstmt.close();
            } catch (SQLException e) {
                log.info("error", e);
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                log.info("error", e);
            }
        }
    }
}
