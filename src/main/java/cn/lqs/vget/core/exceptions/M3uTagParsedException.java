package cn.lqs.vget.core.exceptions;

public class M3uTagParsedException extends Exception{


    public M3uTagParsedException(String message) {
        super(message);
    }

    public M3uTagParsedException(String message, Throwable cause) {
        super(message, cause);
    }

    public M3uTagParsedException(Throwable cause) {
        super(cause);
    }

    public M3uTagParsedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
