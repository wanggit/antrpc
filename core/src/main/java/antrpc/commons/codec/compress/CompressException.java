package antrpc.commons.codec.compress;

class CompressException extends RuntimeException {
    private static final long serialVersionUID = -1310534211373858308L;

    CompressException(Exception e) {
        super(e);
    }
}
