package com.didichuxing.tools.droidassist.transform.enhance;

import com.didichuxing.tools.droidassist.util.Logger;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.expr.NewExpr;

/**
 * Transform that adds constructor-call time-consuming statistics code.
 */
public class ConstructorCallTimingTransformer extends TimingTransformer {

    @Override
    public String getName() {
        return "ConstructorCallTimingTransformer";
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

        String statement = getDefaultTimingStatement(false, getTarget());
        String replacement = replaceInstrument(expr, statement);

        Logger.warning(getPrettyName() + " by: " + replacement
                + " at " + inputClassName + ".java" + ":" + expr.getLineNumber());
        return true;
    }
}

