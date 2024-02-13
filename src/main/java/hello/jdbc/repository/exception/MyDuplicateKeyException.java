package hello.jdbc.repository.exception;

/**
 * @MyDuplicateKeyException -> 이예외는 데이터 중복의 경우에만 던져져야 한다.
 * - 기존에 사요했던 MyDbException 을 상속받아서 의미있는 계층을 형성한다.
 * - 이렇게 계층을 형성하면 데이터베이스 관련 예외라는 계층을 만들수 있다.
 * - 해당 예외는 JDBC / JPA 등과 같은 특정 기술에 종속적이지 않기 때문에 이 예외를 사용하더라도 서비스 계층의 순수성을 유지할 수 있다.
 */
public class MyDuplicateKeyException extends MyDbException {
    public MyDuplicateKeyException() {
    }

    public MyDuplicateKeyException(String message) {
        super(message);
    }

    public MyDuplicateKeyException(String message, Throwable cause) {
        super(message, cause);
    }

    public MyDuplicateKeyException(Throwable cause) {
        super(cause);
    }
}