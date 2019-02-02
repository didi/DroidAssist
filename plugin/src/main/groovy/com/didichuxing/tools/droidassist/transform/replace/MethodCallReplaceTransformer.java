package com.didichuxing.tools.droidassist.transform.replace;

import com.didichuxing.tools.droidassist.util.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.expr.MethodCall;

/**
 * Transform that replaces method-call with new code.
 */
public class MethodCallReplaceTransformer extends ReplaceTransformer {

    @Override
    public String getName() {
        return "MethodCallReplaceTransformer";
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

        if (!isMatchSourceMethod(insnClass, insnName, insnSignature)) {
            return false;
        }

        String target = getTarget();
        if (!isVoidSourceReturnType()) {
            if ((!target.startsWith("$_=") && !target.startsWith("$_ ="))) {
                if (target.startsWith("{")) {
                    target = "{" + "$_=" + target.substring(1);
                } else {
                    target = "$_=" + target;
                }
            }
        } else {
            target = target.
                    replaceFirst(
                            Pattern.quote("$_="), Matcher.quoteReplacement(""))
                    .replaceFirst(
                            Pattern.quote("$_ ="), Matcher.quoteReplacement(""));
        }

        String replacement = replaceInstrument(methodCall, target);
        Logger.warning(getPrettyName() + " by: " + replacement
                + " at " + inputClassName + ".java" + ":" + methodCall.getLineNumber());
        return true;
    }
}
