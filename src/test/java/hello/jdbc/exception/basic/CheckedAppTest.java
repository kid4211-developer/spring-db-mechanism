package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
public class CheckedAppTest {
    @Test
    void checked() {
        Controller controller = new Controller();
        assertThatThrownBy(() -> controller.request()).isInstanceOf(Exception.class);
    }

    static class Controller {
        Service service = new Service();

        public void request () throws SQLException, ConnectException {
            service.logic();
        }
    }

    /**
     * # Service 계층이 체크예외를 throws 하는게 왜 문제가 되는가?
     * - SQLException 의 경우 JDBC 의 예외이므로 Repository DB 계층의 문제는 Service 단에서 다루지도 못하는데
     *   Exception throws 가 강제 되서 JDBC 에 의존하게 된다.
     * - 당장은 문제가 되지 않아 보이지만 Repository 의 DB 기술을 JDBC -> JPA 와 같은 다른 기술로 변경하게 되면
     *   Service 전체를 수정해야 되는 문제가 발생한다.
     */
    static class Service {
        Repository repository = new Repository();
        NetworkClient networkClient = new NetworkClient();

        public void logic() throws SQLException, ConnectException {
            repository.call();
            networkClient.call();
        }
    }

    static class NetworkClient {
        public void call() throws ConnectException {
            throw new ConnectException("연결 실패");
        }
    }

    static class Repository {
        public void call() throws SQLException {
            throw new SQLException("ex");
        }
    }
}
