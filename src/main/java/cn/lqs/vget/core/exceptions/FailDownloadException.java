package cn.lqs.vget.core.exceptions;

public class FailDownloadException extends Exception{

    public FailDownloadException(String message) {
        super(message);
    }

    public FailDownloadException(String message, Throwable cause) {
        super(message, cause);
    }

    public FailDownloadException(Throwable cause) {
        super(cause);
    }

    public FailDownloadException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
