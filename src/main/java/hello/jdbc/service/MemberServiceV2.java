package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * # 트랜잭션 - 파라미터 연동, 풀을 고려한 종료
 * - 반복되는 (커넥션 생성 / 트랜잭션 시작 / 예외처리 / 롤백 / 커넨션 풀 반납)은 충분히 문제가 될 요소이기 때문에
 *   다음 MemberService 버전에서 해결방법이 제시 된다.
 */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV2 {
    private final DataSource dataSource;
    private final MemberRepositoryV2 memberRepository;

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        Connection connection = dataSource.getConnection();
        try {
            /**
             * <트랜잭션 시작>
             * - 트랜잭션을 시작하려면 자동 커밋 모드를 꺼야한다.
             * - 커넥션을 통해 세션에 set autocommit false 가 전달되어 수동 커밋모드로 동작한다.
             */
            connection.setAutoCommit(false);
            Member fromMember = memberRepository.findById(connection, fromId);
            Member toMember = memberRepository.findById(connection, toId);

            int fromMemberMoney = fromMember.getMoney();
            int toMemberMoney = toMember.getMoney();

            memberRepository.update(connection, fromId, fromMemberMoney - money);
            validation(toMember);
            memberRepository.update(connection, toId, toMemberMoney + money);

            connection.commit(); // 성공시 커밋
        } catch (Exception e) {
            connection.rollback(); // 실패시 롤백
            throw new IllegalStateException(e);
        } finally {
            release(connection);
        }
    }

    private void validation(Member toMember) {
        if (toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체중 예외 발생");
        }
    }

    private void release(Connection connection) {
        if (connection != null) {
            try {
                /**
                 * 해당 커넥션은 종료되는 것이 아니라 풀에 반납되기 때문에 autocommit 을 true 로 전환 하지 않으면
                 * 다른 비즈니스 로직에서도 트랜잭션이 시작하게 된다.
                 */
                connection.setAutoCommit(true);
                connection.close();
            } catch (Exception e) {
                log.info("error", e);
            }
        }
    }
}
