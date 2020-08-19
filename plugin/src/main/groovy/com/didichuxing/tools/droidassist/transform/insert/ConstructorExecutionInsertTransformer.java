package com.didichuxing.tools.droidassist.transform.insert;

import com.didichuxing.tools.droidassist.util.Logger;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.NotFoundException;

/**
 * Transform that inserts custom code at the pointcut where constructor executes.
 */
public class ConstructorExecutionInsertTransformer extends InsertTransformer {

    @Override
    public String getName() {
        return "ConstructorExecutionInsertTransformer";
    }

    @Override
    protected String getTransformType() {
        return TRANSFORM_EXEC;
    }

    @Override
    protected String getExecuteType() {
        return CONSTRUCTOR;
    }

    @Override
    protected boolean execute(
            CtClass inputClass,
            String inputClassName,
            CtConstructor constructor)
            throws CannotCompileException, NotFoundException {

        if (!isMatchConstructorSource(inputClassName, constructor)) {
            return false;
        }
        String target = getTarget();
        String name = constructor.getName();

        target = getReplaceStatement(inputClassName, constructor, target);

        if (isAsBefore()) {
            constructor.insertBeforeBody(target);
            Logger.warning(getPrettyName() + " by before: " + target
                    + " at " + inputClassName + ".java" + ":" + name);
        }
        if (isAsAfter()) {
            constructor.insertAfter(target);
            Logger.warning(getPrettyName() + " by after: " + target
                    + " at " + inputClassName + ".java" + ":" + name);
        }
        return false;
    }
}
