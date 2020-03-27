package com.didichuxing.tools.droidassist.transform.enhance;

import com.didichuxing.tools.droidassist.util.ClassUtils;
import com.didichuxing.tools.droidassist.util.Logger;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.NotFoundException;

/**
 * Transform that wraps constructor-execute with try-catch.
 */
public class ConstructorExecutionTryCatchTransformer extends TryCatchTransformer {

    @Override
    public String getName() {
        return "ConstructorExecutionTryCatchTransformer";
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

        ClassUtils.newConstructorDelegate(
                classPool,
                inputClass,
                constructor,
                (source, result) -> {
                    String targetStatement = target;
                    targetStatement = targetStatement.replace("\\s+", " ");
                    String statement = "try{" + source + "} catch (" + getException() + " e) {"
                            + targetStatement.replace("$e", "e") + "}";

                    return getReplaceStatement((CtConstructor) result.getSource(), statement);
                });

        Logger.warning(getPrettyName() + " by: " + target
                + " at " + inputClassName + ".java" + ":" + constructor.getName());
        return true;
    }
}

