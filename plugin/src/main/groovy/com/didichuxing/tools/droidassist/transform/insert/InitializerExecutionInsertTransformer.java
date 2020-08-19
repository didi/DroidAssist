package com.didichuxing.tools.droidassist.transform.insert;

import com.didichuxing.tools.droidassist.util.Logger;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.NotFoundException;

/**
 * Transform that inserts custom code at the pointcut where initializer executes.
 */
public class InitializerExecutionInsertTransformer extends InsertTransformer {
    @Override
    public String getName() {
        return "InitializerExecutionInsertTransformer";
    }

    @Override
    protected String getTransformType() {
        return TRANSFORM_EXEC;
    }

    @Override
    protected String getExecuteType() {
        return INITIALIZER;
    }

    @Override
    protected boolean execute(
            CtClass inputClass,
            String inputClassName,
            CtConstructor initializer)
            throws CannotCompileException, NotFoundException {

        String target = getTarget();

        target = getReplaceStatement(inputClassName, initializer, true, target);
        if (isAsBefore()) {
            initializer.insertBefore(target);
            Logger.warning(getPrettyName() + " insert after execution by: " + target
                    + " at " + inputClassName + ".java" + ":" + initializer.getName());
        }
        if (isAsAfter()) {
            initializer.insertAfter(target);
            Logger.warning(getPrettyName() + " insert after execution by: " + target
                    + " at " + inputClassName + ".java" + ":" + initializer.getName());
        }
        return true;
    }
}
