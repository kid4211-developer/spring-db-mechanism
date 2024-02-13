package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.SQLException;

/**
 * 트랜잭션 - 트랜잭션 템플릿
 */
@Slf4j
public class MemberServiceV3_2 {
    private final TransactionTemplate transactionTemplate;
    private final MemberRepositoryV3 memberRepository;

    public MemberServiceV3_2(PlatformTransactionManager transactionManager, MemberRepositoryV3 memberRepository) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.memberRepository = memberRepository;
    }

    public void accountTransfer(String fromId, String toId, int money) throws IllegalStateException {
        /**
         * # 트랜잭션 템플릿
         * - 템플릿 형태를 적용하므로써 (트랜잭션 시작 / 성공 커밋 / 실패 롤백) 과정을 생략 처리 할 수 있다.
         * - 하지만 여전히 서비스 계층임에도 불구하고 트랜잭션 처리 로직은 남아있다.
         * - AOP 의 Proxy 를 도입한다면 서비스 클래스를 순수한 비즈니스 로직으로만 구성이 가능해 질 것이다.
         */
        transactionTemplate.executeWithoutResult((status) -> {
            try {
                // 비즈니스 로직
                Member fromMember = memberRepository.findById(fromId);
                Member toMember = memberRepository.findById(toId);

                int fromMemberMoney = fromMember.getMoney() - money;
                int toMemberMoney = toMember.getMoney() + money;

                memberRepository.update(fromId, fromMemberMoney);
                validation(toMember);
                memberRepository.update(toId, toMemberMoney);
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    private void validation(Member toMember) {
        if (toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체중 예외 발생");
        }
    }
}
