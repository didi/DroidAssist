package com.didichuxing.tools.droidassist.transform.around;

import com.didichuxing.tools.droidassist.util.Logger;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.expr.MethodCall;

/**
 * Transform that adds code before and after the method call simultaneously.
 */
public class MethodCallAroundTransformer extends AroundTransformer {

    @Override
    public String getName() {
        return "MethodCallAroundTransformer";
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
        String before = getTargetBefore();
        String after = getTargetAfter();

        Logger.warning(getPrettyName() + " by: " + before + " $proceed($$) " + after
                + " at " + inputClassName + ".java" + ":" + methodCall.getLineNumber());

        String proceed = isVoidSourceReturnType() ? "$proceed($$);" : "$_ =$proceed($$);";
        String statement = "{" + before + proceed + after + "}";

        replaceInstrument(inputClassName, methodCall, statement);

        return true;
    }
}
