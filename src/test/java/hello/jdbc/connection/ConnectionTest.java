package hello.jdbc.connection;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;

@Slf4j
public class ConnectionTest {
    @Test
    void driverManager() throws SQLException {
        Connection con1 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        Connection con2 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        log.info("connection={}, class={}", con1, con1.getClass());
        log.info("connection={}, class={}", con2, con2.getClass());
    }

    @Test
    /**
     * - DriverManager 는 DataSource 인터페이스를 사용하지 않기 때문에 DriverManager 를 사용하다가
     *   DataSource 기반의 커넥션 풀을 사용하도록 변경하면 관련 코드를 다 고쳐야 한다.
     *   이런 문제를 해결하기 위해 스프링은 DriverManager 도 DataSource 를 통해서 사 용할 수 있도록
     *   DriverManagerDataSource 라는 DataSource 를 구현한 클래스를 제공한다.
     */
    void dataSourceDriverManager() throws SQLException {
        // DriverManagerDataSource - 항상 새로운 커넥션 획득
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        useDataSource(dataSource);
    }

    @Test
    /**
     * - HikariCP 커넥션 풀을 사용한다. HikariDataSource 는 DataSource 인터페이스를 구현하고 있다.
     * - 커넥션 풀 최대 사이즈를 10으로 지정하고, 풀의 이름을 `MyPool` 이라고 지정
     * - 커넥션 풀에서 커넥션을 생성하는 작업은 애플리케이션 실행 속도에 영향을 주지 않기 위해 별도의 쓰레드에서 작동한다.
     *  (별도의 쓰레드에서 동작하기 때문에 테스트가 먼저 종료되어 버리기 때문에 Thread.sleep() 을 통해
     *   대기 시간을 주어야 쓰레드 풀에 커넥션이 생성되는 로그를 확인할 수 있다)
     */
    void dataSourceConnectionPool() throws SQLException, InterruptedException {
        // 커넥션 풀링: HikariProxyConnection(Proxy) -> JdbcConnection(Target)
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setJdbcUrl(URL);
        hikariDataSource.setUsername(USERNAME);
        hikariDataSource.setPassword(PASSWORD);
        hikariDataSource.setMaximumPoolSize(10);
        hikariDataSource.setPoolName("MyPool");

        useDataSource(hikariDataSource);
        Thread.sleep(5000); //커넥션 풀에서 커넥션 생성 시간 대기
    }

    private void useDataSource(DataSource dataSource) throws SQLException {
        Connection con1 = dataSource.getConnection();
        Connection con2 = dataSource.getConnection();
        log.info("connection={}, class={}", con1, con1.getClass());
        log.info("connection={}, class={}", con2, con2.getClass());
    }
}