package com.didichuxing.tools.droidassist.transform.replace;

import com.didichuxing.tools.droidassist.util.Logger;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.NotFoundException;

/**
 * Transform that replaces constructor-execute with new code.
 */
public class ConstructorExecutionReplaceTransformer extends ReplaceTransformer {

    @Override
    public String getName() {
        return "ConstructorExecutionReplaceTransformer";
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
        replaceInstrument(constructor, target);
        Logger.warning(getPrettyName() + " replaced execution by: " + target
                + " at " + inputClassName + ".java" + ":" + constructor.getName());
        return true;
    }
}
