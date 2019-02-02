package com.didichuxing.tools.droidassist.ex;

@SuppressWarnings("WeakerAccess")
public class DroidAssistBadStatementException extends RuntimeException {

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

    public DroidAssistBadStatementException(String msg) {
        super(msg);
        message = msg;
        initCause(null);
    }


    public DroidAssistBadStatementException(Throwable e) {
        super("by " + e.toString());
        message = null;
        initCause(e);
    }


    public DroidAssistBadStatementException(String msg, Throwable e) {
        this(msg);
        initCause(e);
    }
}
