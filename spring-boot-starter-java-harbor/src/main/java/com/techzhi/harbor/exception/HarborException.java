package com.techzhi.harbor.exception;

/**
 * Harbor操作异常
 * 
 * @author techzhi
 */
public class HarborException extends RuntimeException {

    private final int code;

    public HarborException(String message) {
        super(message);
        this.code = -1;
    }

    public HarborException(String message, Throwable cause) {
        super(message, cause);
        this.code = -1;
    }

    public HarborException(int code, String message) {
        super(message);
        this.code = code;
    }

    public HarborException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
} 