package com.didichuxing.tools.droidassist.transform.around;

import com.didichuxing.tools.droidassist.util.ClassUtils;
import com.didichuxing.tools.droidassist.util.Logger;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.NotFoundException;

/**
 * Transform that adds code before and after the initializer call simultaneously.
 */
public class InitializerExecutionAroundTransformer extends AroundTransformer {
    @Override
    public String getName() {
        return "InitializerExecutionAroundTransformer";
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

        String before = getTargetBefore();
        String after = getTargetAfter();

        ClassUtils.newInitializerDelegate(
                classPool,
                inputClass,
                initializer,
                (source, result) -> {
                    String body = "{" + before + source + after + "}";
                    return getReplaceStatement(inputClassName, initializer, true, body);
                }
        );
        Logger.warning(getPrettyName() + " by: " + before + " $proceed($$) " + after
                + " at " + inputClassName + ".java" + ":" + initializer.getName());

        return true;
    }
}
