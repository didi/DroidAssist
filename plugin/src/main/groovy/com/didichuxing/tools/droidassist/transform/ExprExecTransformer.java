package com.didichuxing.tools.droidassist.transform;


import java.util.concurrent.atomic.AtomicBoolean;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;
import javassist.expr.NewExpr;

@SuppressWarnings("RedundantThrows")
public abstract class ExprExecTransformer extends SourceTargetTransformer {

    protected static final String METHOD_CALL = "MethodCall";
    protected static final String FIELD_ACCESS = "FieldAccess";
    protected static final String NEW_EXPR = "NewExpr";

    protected static final String METHOD = "method";
    protected static final String INITIALIZER = "initializer";
    protected static final String CONSTRUCTOR = "constructor";

    public static final String TRANSFORM_EXEC = "exec";
    public static final String TRANSFORM_EXPR = "expr";


    class Editor extends ExprEditor {
        CtBehavior behavior;
        AtomicBoolean modified;
    }

    protected abstract String getExecuteType();

    protected abstract String getTransformType();

    @Override
    public String getPrettyName() {
        StringBuilder name = new StringBuilder(getCategoryName() + " [");

        String disposeType = getExecuteType();
        if (FIELD_ACCESS.equals(disposeType)) {
            name.append("field");
        }
        if (METHOD_CALL.equals(disposeType)) {
            name.append("method");
        }

        if (NEW_EXPR.equals(disposeType)) {
            name.append("constructor");
        }

        if (METHOD.equals(disposeType)) {
            name.append("method");
        }

        if (CONSTRUCTOR.equals(disposeType)) {
            name.append("constructor");
        }

        if (INITIALIZER.equals(disposeType)) {
            name.append("initializer");
        }

        //expr
        String transformType = getTransformType();
        if (TRANSFORM_EXPR.equals(transformType)) {
            name.append(" call");
        }
        //exec
        if (TRANSFORM_EXEC.equals(getTransformType())) {
            name.append(" exec");
        }
        name.append("]");
        return name.toString();
    }

    @Override
    protected final boolean onTransform(
            CtClass inputClass,
            String inputClassName)
            throws NotFoundException, CannotCompileException {
        //expr
        if (TRANSFORM_EXPR.equals(getTransformType())) {
            return onTransformExpr(inputClass, inputClassName);
        }
        //exec
        if (TRANSFORM_EXEC.equals(getTransformType())) {
            return onTransformExec(inputClass, inputClassName);
        }
        return false;
    }

    private boolean onTransformExec(
            CtClass inputClass,
            String inputClassName)
            throws NotFoundException, CannotCompileException {

        if (!filterClass(inputClass, inputClassName)) {
            return false;
        }
        if (!isMatchSourceClass(inputClass)) {
            return false;
        }

        boolean modified = false;
        if (METHOD.equals(getExecuteType())) {
            CtMethod[] declaredMethods = tryGetDeclaredMethods(inputClass);
            for (CtMethod method : declaredMethods) {
                if (Modifier.isAbstract(method.getModifiers())) {
                    continue;
                }
                if (execute(inputClass, inputClassName, method)) {
                    modified = true;
                }
            }
        }

        if (CONSTRUCTOR.equals(getExecuteType())) {
            CtConstructor[] declaredConstructors = tryGetDeclaredConstructors(inputClass);
            for (CtConstructor constructor : declaredConstructors) {
                if (execute(inputClass, inputClassName, constructor)) {
                    modified = true;
                }
            }
        }

        if (INITIALIZER.equals(getExecuteType())) {
            CtConstructor initializer = tryGetClassInitializer(inputClass);
            if (initializer != null) {
                if (execute(inputClass, inputClassName, initializer)) {
                    modified = true;
                }
            }
        }
        return modified;
    }

    private boolean onTransformExpr(
            CtClass inputClass,
            String inputClassName)
            throws NotFoundException, CannotCompileException {

        if (!filterClass(inputClass, inputClassName)) {
            return false;
        }

        final AtomicBoolean modified = new AtomicBoolean(false);
        Editor editor = new Editor() {

            @Override
            public void edit(MethodCall call) throws CannotCompileException {
                if (METHOD_CALL.equals(getExecuteType())) {
                    boolean disposed;
                    try {
                        disposed = execute(inputClass, inputClassName, call);
                    } catch (NotFoundException e) {
                        String msg = e.getMessage() + " for input class " + inputClassName;
                        throw new CannotCompileException(msg, e);
                    }
                    modified.set(modified.get() | disposed);
                }
            }

            @Override
            public void edit(FieldAccess fieldAccess) throws CannotCompileException {
                if (FIELD_ACCESS.equals(getExecuteType())) {
                    boolean disposed;
                    try {
                        disposed = execute(inputClass, inputClassName, fieldAccess);
                    } catch (NotFoundException e) {
                        String msg = e.getMessage() + " for input class " + inputClassName;
                        throw new CannotCompileException(msg, e);
                    }
                    modified.set(modified.get() | disposed);
                }
            }

            @Override
            public void edit(NewExpr newExpr) throws CannotCompileException {
                if (NEW_EXPR.equals(getExecuteType())) {
                    boolean disposed;
                    try {
                        disposed = execute(inputClass, inputClassName, newExpr);
                    } catch (NotFoundException e) {
                        String msg = e.getMessage() + " for input class " + inputClassName;
                        throw new CannotCompileException(msg, e);
                    }
                    modified.set(modified.get() | disposed);
                }
            }
        };

        CtConstructor initializer = tryGetClassInitializer(inputClass);
        if (initializer != null) {
            if (instrument(initializer, editor)) {
                modified.set(true);
            }
        }
        CtMethod[] declaredMethods = tryGetDeclaredMethods(inputClass);
        for (CtMethod method : declaredMethods) {
            if (instrument(method, editor)) {
                modified.set(true);
            }
        }
        CtConstructor[] declaredConstructors = tryGetDeclaredConstructors(inputClass);
        for (CtConstructor constructor : declaredConstructors) {
            if (instrument(constructor, editor)) {
                modified.set(true);
            }
        }
        return modified.get();
    }

    private boolean instrument(CtBehavior behavior, Editor editor) throws CannotCompileException {
        editor.modified = new AtomicBoolean(false);
        editor.behavior = behavior;
        behavior.instrument(editor);
        return editor.modified.get();
    }

    protected boolean filterClass(
            CtClass inputClass,
            String inputClassName)
            throws NotFoundException, CannotCompileException {
        return true;
    }

    protected boolean execute(
            CtClass inputClass,
            String inputClassName,
            CtMethod method)
            throws CannotCompileException, NotFoundException {
        return false;
    }

    protected boolean execute(
            CtClass inputClass,
            String inputClassName,
            CtConstructor constructor)
            throws CannotCompileException, NotFoundException {
        return false;
    }

    protected boolean execute(
            CtClass inputClass,
            String inputClassName,
            MethodCall methodCall)
            throws CannotCompileException, NotFoundException {
        return false;
    }

    protected boolean execute(
            CtClass inputClass,
            String inputClassName,
            FieldAccess fieldAccess)
            throws CannotCompileException, NotFoundException {
        return false;
    }

    protected boolean execute(
            CtClass inputClass,
            String inputClassName,
            NewExpr newExpr)
            throws CannotCompileException, NotFoundException {
        return false;
    }
}
