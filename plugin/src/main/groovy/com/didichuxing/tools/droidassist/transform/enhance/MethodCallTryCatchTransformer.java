package com.didichuxing.tools.droidassist.transform.enhance;

import com.didichuxing.tools.droidassist.util.Logger;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.expr.MethodCall;

/**
 * Transform that wraps method-call with try-catch.
 */
public class MethodCallTryCatchTransformer extends TryCatchTransformer {

    @Override
    public String getName() {
        return "MethodCallTryCatchTransformer";
    }

    @Override
    protected String getTransformType() {
        return TRANSFORM_EXPR;
    }

    @Override
    protected String getExecuteType() {
        return METHOD_CALL;
    }

    @Override
    protected boolean execute(
            CtClass inputClass,
            String inputClassName,
            MethodCall methodCall)
            throws CannotCompileException, NotFoundException {

        if (methodCall.isSuper()) {
            return false;
        }

        String insnClassName = methodCall.getClassName();
        String insnName = methodCall.getMethodName();
        String insnSignature = methodCall.getSignature();

        CtClass insnClass = tryGetClass(insnClassName, inputClassName);
        if (insnClass == null) {
            return false;
        }

        if (!isMatchSourceMethod(insnClass, insnName, insnSignature, false)) {
            return false;
        }
        String target = getTarget();

        String proceed = isVoidSourceReturnType() ? "$proceed($$);" : "$_ =$proceed($$);";

        String statement = "try{" +
                proceed +
                "} catch (" + getException() + " e) {" +
                target.replace("$e", "e") +
                "}";

        String replacement = replaceInstrument(inputClassName, methodCall, statement);

        Logger.warning(getPrettyName() + " by: " + replacement
                + " at " + inputClassName + ".java" + ":" + methodCall.getLineNumber());

        return true;
    }
}

