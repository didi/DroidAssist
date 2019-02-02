package com.didichuxing.tools.droidassist.transform.insert;

import com.didichuxing.tools.droidassist.util.Logger;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

/**
 * Transform that inserts custom code at the pointcut where method executes.
 */
public class MethodExecutionInsertTransformer extends InsertTransformer {

    @Override
    public String getName() {
        return "MethodExecutionInsertTransformer";
    }

    @Override
    protected String getTransformType() {
        return TRANSFORM_EXEC;
    }

    @Override
    protected String getExecuteType() {
        return METHOD;
    }

    @Override
    protected boolean execute(
            CtClass inputClass,
            String inputClassName,
            CtMethod method)
            throws CannotCompileException, NotFoundException {

        String name = method.getName();
        String signature = method.getSignature();

        if (!isMatchSourceMethod(inputClass, false, name, signature)) {
            return false;
        }

        String target = getTarget();
        target = getReplaceStatement(method, target);
        if (isAsBefore()) {
            method.insertBefore(target);
            Logger.warning(getPrettyName() + " by before: " + target
                    + " at " + inputClassName + ".java" + ":" + name);
        }
        if (isAsAfter()) {
            method.insertAfter(target);
            Logger.warning(getPrettyName() + " by after: " + target
                    + " at " + inputClassName + ".java" + ":" + name);
        }
        return true;
    }
}
