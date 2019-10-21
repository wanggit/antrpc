package antrpc.client.rate;

public class RateLimitingException extends RuntimeException {

    private static final long serialVersionUID = -4643251205289732991L;

    public RateLimitingException() {}

    public RateLimitingException(String message) {
        super(message);
    }

    public RateLimitingException(String message, Throwable cause) {
        super(message, cause);
    }

    public RateLimitingException(Throwable cause) {
        super(cause);
    }
}
