package com.didichuxing.tools.droidassist.transform.enhance;

import com.didichuxing.tools.droidassist.util.ClassUtils;
import com.didichuxing.tools.droidassist.util.Logger;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.NotFoundException;

/**
 * Transform that wraps initializer-execute with try-catch.
 */
public class InitializerExecutionTryCatchTransformer extends TryCatchTransformer {

    @Override
    public String getName() {
        return "InitializerExecutionTryCatchTransformer";
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
                    String statement = "try{"
                            + source +
                            "} catch (" + getException() + " e) {"
                            + target.replace("$e", "e")
                            + "}";
                    statement = getReplaceStatement(initializer, true, statement);
                    return statement;
                }
        );

        Logger.warning(getPrettyName() + " add catch for constructor exec by: " + target
                + " at " + inputClassName + ".java" + ":" + initializer.getName());
        return true;
    }
}

