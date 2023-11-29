package cn.lqs.vget.core.exceptions;

public class DecodeFailException extends Exception{
    public DecodeFailException() {
    }

    public DecodeFailException(String message) {
        super(message);
    }

    public DecodeFailException(String message, Throwable cause) {
        super(message, cause);
    }

    public DecodeFailException(Throwable cause) {
        super(cause);
    }

    public DecodeFailException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
