package com.didichuxing.tools.droidassist.transform.enhance;

import com.didichuxing.tools.droidassist.util.ClassUtils;
import com.didichuxing.tools.droidassist.util.Logger;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

/**
 * Transform that wraps method-execute with try-catch.
 */
public class MethodExecutionTryCatchTransformer extends TryCatchTransformer {

    @Override
    public String getName() {
        return "MethodExecutionTryCatchTransformer";
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

        addCatchForMethod(inputClass, method, getExceptionClass(), target);

        Logger.warning(getPrettyName() + "by: " + target
                + " at " + inputClassName + ".java" + ":" + name);

        return true;
    }


    private void addCatchForMethod(
            CtClass clazz,
            CtMethod method,
            CtClass exceptionType,
            String target)
            throws NotFoundException, CannotCompileException {

        ClassUtils.DelegateResult result =
                ClassUtils.newMethodDelegate(clazz, method, null);

        CtMethod srcMethod = result.getSource();
        String type = method.getReturnType().getName();
        boolean isVoid = "void".equals(type);

        StringBuilder builder = new StringBuilder(target);
        target = target.replace("\\s+", " ");
        boolean returnStatement =
                target.contains(" return ")
                        || target.contains("return ")
                        || target.contains(" return;")
                        || target.contains(";return;")
                        || target.contains(";return ")
                        || target.contains(" throw ")
                        || target.contains("throw ")
                        || target.contains(";throw ");
        if (isVoid) {
            if (!returnStatement) {
                builder.append("return;");
            }
        } else {
            if (!returnStatement) {
                throw new CannotCompileException(
                        "Catch block code fragment must end with a throw or return statement, " +
                                "but found: " + target);
            }
        }
        target = getReplaceStatement(clazz.getName(), method, builder.toString());
        srcMethod.addCatch(target, exceptionType);
    }
}

