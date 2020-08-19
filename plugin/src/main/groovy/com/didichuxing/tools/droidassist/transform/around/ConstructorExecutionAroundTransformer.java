package com.didichuxing.tools.droidassist.transform.around;

import com.didichuxing.tools.droidassist.util.ClassUtils;
import com.didichuxing.tools.droidassist.util.Logger;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.NotFoundException;

/**
 * Transform that adds code before and after the constructor execute simultaneously.
 */
public class ConstructorExecutionAroundTransformer extends AroundTransformer {

    @Override
    public String getName() {
        return "ConstructorExecutionAroundTransformer";
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
        String before = getTargetBefore();
        String after = getTargetAfter();

        ClassUtils.newConstructorDelegate(
                classPool,
                inputClass,
                constructor,
                (source, result) -> {
                    String body = "{" + before + source + after + "}";
                    return getReplaceStatement(inputClassName, (CtConstructor) result.getSource(), body);
                }
        );

        Logger.warning(getPrettyName() + " by: " + before + " $proceed($$) "
                + after + " at " + inputClassName + ".java" + ":" + constructor.getName());
        return true;
    }
}
