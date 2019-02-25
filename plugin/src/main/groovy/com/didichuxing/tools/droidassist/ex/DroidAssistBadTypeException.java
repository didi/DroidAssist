package com.didichuxing.tools.droidassist.ex;

public class DroidAssistBadTypeException extends DroidAssistException {

    public DroidAssistBadTypeException(String msg) {
        super(msg);
    }

    public DroidAssistBadTypeException(Throwable e) {
        super(e);
    }

    public DroidAssistBadTypeException(String msg, Throwable e) {
        super(msg, e);
    }
}
