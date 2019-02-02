package com.didichuxing.tools.droidassist.transform.replace;

import com.didichuxing.tools.droidassist.util.Logger;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.expr.NewExpr;

/**
 * Transform that replaces constructor-call with new code.
 */
public class ConstructorCallReplaceTransformer extends ReplaceTransformer {

    @Override
    public String getName() {
        return "ConstructorCallReplaceTransformer";
    }

    @Override
    protected String getTransformType() {
        return TRANSFORM_EXPR;
    }

    @Override
    protected String getExecuteType() {
        return NEW_EXPR;
    }

    @Override
    protected boolean filterClass(CtClass inputClass, String inputClassName) {
        return !isMatchSourceClassName(inputClassName);
    }

    @Override
    protected boolean execute(
            CtClass inputClass,
            String inputClassName,
            NewExpr newExpr)
            throws CannotCompileException, NotFoundException {

        String insnClassName = newExpr.getClassName();
        String insnSignature = newExpr.getSignature();

        if (!isMatchConstructorSource(insnClassName, insnSignature)) {
            return false;
        }

        String target = getTarget();

        if (!target.startsWith("$_=") || !target.startsWith("$_ =")) {
            if (target.startsWith("{")) {
                target = "{" + "$_=" + target.substring(1);
            } else {
                target = "$_=" + target;
            }
        }

        String replacement = replaceInstrument(newExpr, target);

        Logger.warning(getPrettyName() + "by: " + replacement
                + " at " + inputClassName + ".java" + ":" + newExpr.getLineNumber());
        return true;
    }
}
