package com.didichuxing.tools.droidassist.transform.replace;

import com.didichuxing.tools.droidassist.util.Logger;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.NotFoundException;

/**
 * Transform that replaces initializer-execute with new code.
 */
public class InitializerExecutionReplaceTransformer extends ReplaceTransformer {
    @Override
    public String getName() {
        return "InitializerExecutionReplaceTransformer";
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
            CtConstructor constructor)
            throws CannotCompileException, NotFoundException {

        String target = getTarget();
        target = getReplaceStatement(constructor, true, target);
        constructor.setBody(target);
        Logger.warning(getPrettyName() + " replaced execution by: " + target
                + " at " + inputClassName + ".java" + ":" + constructor.getName());
        return true;
    }
}
