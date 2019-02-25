package com.didichuxing.tools.droidassist.ex;


public class DroidAssistException extends RuntimeException {

    private Throwable myCause;

    public Throwable getCause() {
        return (myCause == this ? null : myCause);
    }

    public synchronized Throwable initCause(Throwable cause) {
        myCause = cause;
        return this;
    }

    private String message;

    public String getReason() {
        if (message != null) {
            return message;
        } else {
            return this.toString();
        }
    }

    public DroidAssistException(String msg) {
        super(msg);
        message = msg;
        initCause(null);
    }


    public DroidAssistException(Throwable e) {
        super(e);
        message = null;
        initCause(e);
    }


    public DroidAssistException(String msg, Throwable e) {
        this(msg);
        initCause(e);
    }
}
