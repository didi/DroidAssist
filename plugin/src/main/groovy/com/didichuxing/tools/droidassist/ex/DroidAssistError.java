package com.didichuxing.tools.droidassist.ex;

public class DroidAssistError extends Error {

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

    public DroidAssistError(String msg) {
        super(msg);
        message = msg;
        initCause(null);
    }


    public DroidAssistError(Throwable e) {
        super("by " + e.toString());
        message = null;
        initCause(e);
    }


    public DroidAssistError(String msg, Throwable e) {
        this(msg);
        initCause(e);
    }
}
