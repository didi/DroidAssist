package com.didichuxing.tools.droidassist.transform.enhance;

import com.didichuxing.tools.droidassist.util.Logger;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.expr.NewExpr;

/**
 * Transform that wraps constructor-call with try-catch.
 */
public class ConstructorCallTryCatchTransformer extends TryCatchTransformer {

    @Override
    public String getName() {
        return "ConstructorCallTryCatchTransformer";
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
    protected boolean execute(
            CtClass inputClass,
            String inputClassName,
            NewExpr expr)
            throws CannotCompileException, NotFoundException {

        if (isMatchSourceClassName(inputClassName)) {
            return false;
        }

        String insnClassName = expr.getClassName();
        String insnSignature = expr.getSignature();

        if (!isMatchConstructorSource(insnClassName, insnSignature)) {
            return false;
        }

        String proceed = "$_=$proceed($$);";

        String statement = "try{" + proceed + "} catch (" + getException() + " e) {"
                + getTarget().replace("$e", "e") + "}";

        String replacement = replaceInstrument(inputClassName, expr, statement);

        Logger.warning(getPrettyName() + " by: " + replacement
                + " at " + inputClassName + ".java" + ":" + expr.getLineNumber());
        return true;
    }
}

