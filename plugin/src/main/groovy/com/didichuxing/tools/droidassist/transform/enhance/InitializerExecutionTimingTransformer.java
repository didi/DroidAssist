package com.didichuxing.tools.droidassist.transform.enhance;

import com.didichuxing.tools.droidassist.util.ClassUtils;
import com.didichuxing.tools.droidassist.util.Logger;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.NotFoundException;

/**
 * Transform that adds initializer-execute time-consuming statistics code.
 */
public class InitializerExecutionTimingTransformer extends TimingTransformer {

    @Override
    public String getName() {
        return "InitializerExecutionTimingTransformer";
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

        ClassUtils.newInitializerDelegate(
                classPool,
                inputClass,
                initializer,
                (source, result) -> {
                    String statement = "{" + getTimingStatement(source, target) + "}";
                    statement = getReplaceStatement(inputClassName, initializer, true, statement);
                    return statement;
                });

        Logger.warning(getPrettyName() + " by: " + target
                + " at " + inputClassName + ".java" + ":" + initializer.getName());

        return true;
    }
}

