package antrpc.server.exception;

public class HasManyBeansException extends Exception {
    private static final long serialVersionUID = 6743694002413957206L;

    public HasManyBeansException(String msg) {
        super(msg);
    }

    public HasManyBeansException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
