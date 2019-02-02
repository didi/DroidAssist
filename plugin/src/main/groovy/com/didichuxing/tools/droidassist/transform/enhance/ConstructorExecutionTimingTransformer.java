package com.didichuxing.tools.droidassist.transform.enhance;

import com.didichuxing.tools.droidassist.util.ClassUtils;
import com.didichuxing.tools.droidassist.util.Logger;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.NotFoundException;

/**
 * Transform that adds constructor-execute time-consuming statistics code.
 */
public class ConstructorExecutionTimingTransformer extends TimingTransformer {

    @Override
    public String getName() {
        return "ConstructorExecutionTimingTransformer";
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

        String name = constructor.getName();
        String signature = constructor.getSignature();

        if (!isMatchConstructorSource(inputClassName, signature)) {
            return false;
        }
        String target = getTarget();

        ClassUtils.newConstructorDelegate(
                classPool,
                inputClass,
                constructor,
                (source, result) -> {
                    String statement = "{" + getTimingStatement(source, target) + "}";
                    statement = getReplaceStatement((CtConstructor) result.getSource(), statement);
                    return statement;
                });

        Logger.warning(getPrettyName() + " by: " + target
                + " at " + inputClassName + ".java" + ":" + name);

        return true;
    }
}

