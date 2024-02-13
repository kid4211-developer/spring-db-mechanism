package hello.jdbc.repository;

import com.zaxxer.hikari.HikariDataSource;
import hello.jdbc.domain.Member;
import static hello.jdbc.connection.ConnectionConst.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@Slf4j
class MemberRepositoryV1Test {
    MemberRepositoryV1 memberRepositoryV1;

    @BeforeEach
    void beforeEach() throws Exception {
        // 기본 DriverManager - 항상 새로운 커넥션 획득 //DriverManagerDataSource dataSource =
        // new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        // 커넥션 풀링: HikariProxyConnection -> JdbcConnection
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setJdbcUrl(URL);
        hikariDataSource.setUsername(USERNAME);
        hikariDataSource.setPassword(PASSWORD);
        memberRepositoryV1 = new MemberRepositoryV1(hikariDataSource);
    }

    @Test
    void crud() throws SQLException {
        // save
        Member member = new Member("memberV0", 10000);
        memberRepositoryV1.save(member);

        // findById
        Member findMember = memberRepositoryV1.findById(member.getMemberId());
        log.info("findMember={}", findMember);
        assertThat(findMember).isEqualTo(member);

        // update: money: 10000 -> 20000
        memberRepositoryV1.update(member.getMemberId(), 20000);
        Member updatedMember = memberRepositoryV1.findById(member.getMemberId());
        assertThat(updatedMember.getMoney()).isEqualTo(20000);

        // delete
        memberRepositoryV1.delete(member.getMemberId());
        /**
         * 회원을 삭제한 다음 findById()` 를 통해서 조회한다.
         * 회원이 없기 때문에 NoSuchElementException 이 발생하는데 assertThatThrownBy 는 해당 예외가 발생해야 검증에 성공한다.
         */
        assertThatThrownBy(() -> memberRepositoryV1.findById(member.getMemberId()))
                .isInstanceOf(NoSuchElementException.class);
    }
}
