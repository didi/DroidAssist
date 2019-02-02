package com.didichuxing.tools.droidassist.transform.replace;

import com.didichuxing.tools.droidassist.util.Logger;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

/**
 * Transform that replaces method-execute with new code.
 */
public class MethodExecutionReplaceTransformer extends ReplaceTransformer {

    @Override
    public String getName() {
        return "MethodExecutionReplaceTransformer";
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
        method.setBody(target);
        Logger.warning(getPrettyName() + " by: " + target
                + " at " + inputClassName + ".java" + ":" + name);
        return true;
    }
}
