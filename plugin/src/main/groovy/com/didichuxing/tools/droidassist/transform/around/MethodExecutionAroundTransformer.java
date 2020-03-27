package com.didichuxing.tools.droidassist.transform.around;

import com.didichuxing.tools.droidassist.util.ClassUtils;
import com.didichuxing.tools.droidassist.util.Logger;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

/**
 * Transform that adds code before and after the method execute simultaneously.
 */
public class MethodExecutionAroundTransformer extends AroundTransformer {

    @Override
    public String getName() {
        return "MethodExecutionAroundTransformer";
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

        if (!isMatchSourceMethod(inputClass, false, name, signature, method)) {
            return false;
        }
        String before = getTargetBefore();
        String after = getTargetAfter();

        ClassUtils.newMethodDelegate(
                inputClass,
                method,
                (source, result) -> {
                    String body = "{" + before + source + after + "}";
                    return getReplaceStatement((CtMethod) result.getSource(), body);
                });

        Logger.warning(getPrettyName() + " by: " + before + " $proceed($$) "
                + after + " at " + inputClassName + ".java" + ":" + name);
        return true;
    }
}
