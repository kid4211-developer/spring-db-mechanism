package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import java.sql.SQLException;

/**
 * 트랜잭션 - @Transactional AOP */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV3_3 {
    private final MemberRepositoryV3 memberRepository;

    /**
     * @Transactional 어노테이션을 통해 순수 비즈니스 로직만 남기고 트랜잭션 코드는 모두 생략 가능 해진다.
     * - 클래스에 붙이면 외부에서 호출 가능한 public 메서드가 AOP 적용 대상이 된다
     */
    @Transactional
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);

        int fromMemberMoney = fromMember.getMoney() - money;
        int toMemberMoney = toMember.getMoney() + money;

        memberRepository.update(fromId, fromMemberMoney);
        validation(toMember);
        memberRepository.update(toId, toMemberMoney);
    }

    private void validation(Member toMember) {
        if (toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체중 예외 발생"); }
    }
}
