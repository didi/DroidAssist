package com.didichuxing.tools.droidassist.transform.enhance;

import com.didichuxing.tools.droidassist.util.ClassUtils;
import com.didichuxing.tools.droidassist.util.Logger;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

/**
 * Transform that adds method-execute time-consuming statistics code.
 */
public class MethodExecutionTimingTransformer extends TimingTransformer {

    @Override
    public String getName() {
        return "MethodExecutionTimingTransformer";
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

        if (!isMatchSourceMethod(inputClass, false, name, signature, method, true)) {
            return false;
        }
        String target = getTarget();

        ClassUtils.newMethodDelegate(
                inputClass,
                method,
                (source, result) -> {
                    String body = "{" + getTimingStatement(source, target) + "}";
                    return getReplaceStatement(inputClassName, (CtMethod) result.getSource(), body);
                });

        Logger.warning(getPrettyName() + " by: " + target
                + " at " + inputClassName + ".java" + ":" + name);

        return true;
    }
}

