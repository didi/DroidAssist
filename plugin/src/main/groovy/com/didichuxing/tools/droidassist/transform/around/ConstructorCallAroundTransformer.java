package com.didichuxing.tools.droidassist.transform.around;

import com.didichuxing.tools.droidassist.util.Logger;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.expr.NewExpr;

/**
 * Transform that adds code before and after the constructor method call simultaneously.
 */
public class ConstructorCallAroundTransformer extends AroundTransformer {

    @Override
    public String getName() {
        return "ConstructorCallAroundTransformer";
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
            NewExpr expr)
            throws CannotCompileException, NotFoundException {

        String insnClassName = expr.getClassName();
        String insnSignature = expr.getSignature();

        if (!isMatchConstructorSource(insnClassName, insnSignature)) {
            return false;
        }

        String before = getTargetBefore();
        String after = getTargetAfter();
        // "$_=$proceed($$);" represents the original method body
        String statement = "{" + before + "$_=$proceed($$);" + after + "}";
        String replacement = replaceInstrument(inputClassName, expr, statement);

        Logger.warning(getPrettyName() + " by: " + replacement
                + " at " + inputClassName + ".java" + ":" + expr.getLineNumber());
        return true;
    }
}
