package cn.lqs.vget.core.exceptions;

public class HttpUrlHrefException extends Exception{
    public HttpUrlHrefException() {
    }

    public HttpUrlHrefException(String message) {
        super(message);
    }

    public HttpUrlHrefException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpUrlHrefException(Throwable cause) {
        super(cause);
    }
}
