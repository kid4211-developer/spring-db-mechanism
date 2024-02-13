package hello.jdbc.exception.translator;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class SpringExceptionTranslatorTest {
    DataSource dataSource;

    @BeforeEach
    void init() {
        dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
    }

    @Test
    void sqlExceptionErrorCode() {
        String sql = "select bad grammar";
        try {
            Connection con = dataSource.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.executeQuery();
        } catch (SQLException e) {
            /**
             * 이전에 SQL ErrorCode 를 직접 확인하는 방법으로 직접 예외를 확인하고 하나하나 스프링이 만들어준 예외로 변환하는 것은 효율적이지 못하다.
             * 그리고 데이터베이스마다 오류 코드가 다르다는 점도 문제가 된다.
             */
            assertThat(e.getErrorCode()).isEqualTo(42122);
            int errorCode = e.getErrorCode();
            log.info("errorCode={}", errorCode);
            // org.h2.jdbc.JdbcSQLSyntaxErrorException
            log.info("error", e);
        }
    }

    @Test
    void exceptionTranslator() {
        String sql = "select bad grammar";
        try {
            Connection con = dataSource.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.executeQuery();
        } catch (SQLException e) {
            assertThat(e.getErrorCode()).isEqualTo(42122);
            /**
             * @SQLExceptionTranslator - 스프링이 제공하는 SQL 예외 변환기
             * - translate() 를 통해 적절한 스프링 데이터 접근 계층의 예외로 변환해서 반환해준다.
             *
             * @org.springframework.jdbc.support.sql-error-codes.xml
             * - 사용 하는 대부분의 관계형 데이터베이스에 맞춰 SQL ErrorCode 가 제크되도록 정의 되어 있다.
             */
            SQLExceptionTranslator sqlExceptionTranslator = new SQLErrorCodeSQLExceptionTranslator(dataSource);
            DataAccessException dataAccessException = sqlExceptionTranslator.translate("select", sql, e);
            log.info("dataAccessException : ", dataAccessException);
            assertThat(dataAccessException.getClass()).isEqualTo(BadSqlGrammarException.class);
        }
    }
}