package com.didichuxing.tools.droidassist.transform.enhance;

import com.didichuxing.tools.droidassist.transform.ExprExecTransformer;
import com.didichuxing.tools.droidassist.util.Logger;
import com.google.common.collect.Sets;

import java.util.Set;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.Descriptor;
import javassist.expr.ConstructorCall;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;

/**
 * An transform that support for class parent reset
 *
 * <p> See {@link ConstructorCallTryCatchTransformer}, {@link ConstructorExecutionTryCatchTransformer},
 * {@link InitializerExecutionTryCatchTransformer}, {@link MethodCallTryCatchTransformer},
 * {@link MethodCallTryCatchTransformer}
 */
public class ReparentClassTransformer extends ExprExecTransformer {

    private CtClass targetClass;

    @Override
    public String getName() {
        return "ReparentClassTransformer";
    }

    public String getCategoryName() {
        return "Reparent";
    }

    @Override
    protected String getTransformType() {
        return TRANSFORM_EXPR;
    }

    @Override
    protected String getExecuteType() {
        return METHOD_CALL;
    }

    @Override
    protected Set<String> getExtraExecuteTypes() {
        return Sets.newHashSet(CONSTRUCTOR_CALL, FIELD_ACCESS);
    }

    @Override
    protected boolean filterClass(CtClass inputClass, String inputClassName)
            throws NotFoundException {
        return inputClass.getSuperclass().getName().equals(getSourceDeclaringClassName())
                && !inputClassName.equals(getTargetClass().getName());
    }

    private CtClass getTargetClass() throws NotFoundException {
        if (targetClass == null) {
            targetClass = classPool.get(getTarget());
        }
        return targetClass;
    }

    @Override
    protected boolean execute(CtClass inputClass, String inputClassName)
            throws CannotCompileException, NotFoundException {

        Logger.warning(getCategoryName() + " reset parent for class " + inputClassName + " from "
                + inputClass.getSuperclass().getName() + " to " + getTarget());

        inputClass.setSuperclass(getTargetClass());
        return true;
    }

    @Override
    protected boolean execute(CtClass inputClass, String inputClassName, MethodCall methodCall)
            throws CannotCompileException, NotFoundException {
        if (methodCall.isSuper()) {
            String signature = methodCall.getSignature();
            boolean voidType =
                    Descriptor.getReturnType(signature, classPool) == CtClass.voidType;
            String methodName = methodCall.getMethodName();

            Logger.warning(getCategoryName() + " reset parent for class " + inputClassName
                    + " adapt super method call" +
                    " at " + inputClassName + ".java" + ":" + methodCall.getLineNumber());

            methodCall.replace((!voidType ? "$_=" : "") + "super." + methodName + "($$);");
            return true;
        }
        return false;
    }

    @Override
    protected boolean execute(CtClass inputClass, String inputClassName, ConstructorCall constructorCall)
            throws CannotCompileException, NotFoundException {
        if (constructorCall.isSuper()) {

            Logger.warning(getCategoryName() + " reset parent for class " + inputClassName
                    + " adapt super constructor call" +
                    " at " + inputClassName + ".java" + ":" + constructorCall.getLineNumber());


            constructorCall.replace("super($$);");
            return true;
        }
        return super.execute(inputClass, inputClassName, constructorCall);
    }

    @Override
    protected boolean execute(CtClass inputClass, String inputClassName, FieldAccess fieldAccess)
            throws CannotCompileException, NotFoundException {
        if (!fieldAccess.isStatic()
                && fieldAccess.getClassName().equals(getSourceDeclaringClassName())) {

            Logger.warning(getCategoryName() + " reset parent for class " + inputClassName
                    + " adapt super field access" +
                    " at " + inputClassName + ".java" + ":" + fieldAccess.getLineNumber());

            String fieldName = fieldAccess.getFieldName();
            if (fieldAccess.isReader()) {
                fieldAccess.replace("$_=super." + fieldName + ";");
            } else if (fieldAccess.isWriter()) {
                fieldAccess.replace("super." + fieldName + "=$1;");
            }
            return true;
        }
        return false;
    }

}
