package com.didichuxing.tools.droidassist.transform.enhance;

import com.didichuxing.tools.droidassist.spec.SourceSpec;
import com.didichuxing.tools.droidassist.transform.ExprExecTransformer;

import javassist.CtClass;
import javassist.NotFoundException;

/**
 * An abstract transform that wraps target code with try-catch.
 *
 * <p> See {@link ConstructorCallTryCatchTransformer}, {@link ConstructorExecutionTryCatchTransformer},
 * {@link InitializerExecutionTryCatchTransformer}, {@link MethodCallTryCatchTransformer},
 * {@link MethodCallTryCatchTransformer}
 */
public abstract class TryCatchTransformer extends ExprExecTransformer {
    private String exception;
    private CtClass exceptionClass;

    @Override
    public String getCategoryName() {
        return "TryCatch";
    }

    protected String getException() {
        if (exception == null || exception.trim().equals("")) {
            exception = " java.lang.Exception";
        }
        return SourceSpec.Type.forName(exception).getName();
    }

    protected CtClass getExceptionClass() throws NotFoundException {
        if (exceptionClass == null) {
            exceptionClass = classPool.get(getException());
        }
        return exceptionClass;
    }

    public TryCatchTransformer setException(String exception) {
        this.exception = exception;
        return this;
    }
}
