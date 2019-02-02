package com.didichuxing.tools.droidassist.transform.insert;

import com.didichuxing.tools.droidassist.util.Logger;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.expr.NewExpr;

/**
 * Transform that inserts custom code at the pointcut where constructor is called.
 */
public class ConstructorCallInsertTransformer extends InsertTransformer {

    @Override
    public String getName() {
        return "ConstructorCallInsertTransformer";
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

        if (!target.endsWith(";")) {
            target = target + ";";
        }
        String before = isAsBefore() ? target : "";
        String after = isAsAfter() ? target : "";
        String statement = "{" + before + "$_=$proceed($$);" + after + "}";
        String replacement = replaceInstrument(newExpr, statement);

        if (isAsBefore()) {
            Logger.warning(getPrettyName() + " by before: " + replacement
                    + " at " + inputClassName + ".java" + ":" + newExpr.getLineNumber());
        }

        if (isAsAfter()) {
            Logger.warning(getPrettyName() + " by after: " + replacement
                    + " at " + inputClassName + ".java" + ":" + newExpr.getLineNumber());
        }
        return true;
    }
}
