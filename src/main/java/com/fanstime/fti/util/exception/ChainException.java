package com.fanstime.fti.util.exception;

/**
 * Created by Bynum Williams on 29.07.18.
 */
public class ChainException extends RuntimeException {

    /**
     * Might be useful on client side to understand exact cause of exception.
     */
    private int errorCode;

    public ChainException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
}
