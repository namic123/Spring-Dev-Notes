package spring.jdbc.connection;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static spring.jdbc.connection.ConnectionConst.*;

@Slf4j
public class ConnectionTest {

@Test
    void driverManager() throws SQLException {
    Connection con1 = DriverManager.getConnection(URL, USERNAME, PASSWORD);

    Connection con2 = DriverManager.getConnection(URL, USERNAME, PASSWORD);

    log.info("connection={}, class={}", con1, con1.getClass());
    log.info("connection={}, class={}", con2, con2.getClass());
}
}
