package com.didichuxing.tools.droidassist.ex;

public class DroidAssistNotFoundException extends RuntimeException {
    private Throwable myCause;

    public DroidAssistNotFoundException(String msg) {
        super(msg);
        message = msg;
        initCause(null);
    }

    public DroidAssistNotFoundException(Throwable e) {
        super("Class " + e.getMessage()+" not found");
        message = null;
        initCause(e);
    }

    public DroidAssistNotFoundException(String msg, Throwable e) {
        this(msg);
        initCause(e);
    }

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
}
