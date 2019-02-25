package com.didichuxing.tools.droidassist.ex;

public class DroidAssistNotFoundException extends DroidAssistException {

    public DroidAssistNotFoundException(String msg) {
        super(msg);
    }

    public DroidAssistNotFoundException(Throwable e) {
        super(e);
    }

    public DroidAssistNotFoundException(String msg, Throwable e) {
        super(msg, e);
    }
}
