package com.didichuxing.tools.droidassist.util;

import java.util.List;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMember;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.ExceptionTable;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;

@SuppressWarnings("RedundantThrows")
public class ClassUtils {
    public interface BodyProducer {
        String createBody(String source, DelegateResult result);
    }

    public static class DelegateResult {
        private final CtMember source;
        private final CtMember delegate;
        private final String statement;

        private DelegateResult(CtMember source, CtMember delegate, String statement) {
            this.source = source;
            this.delegate = delegate;
            this.statement = statement;
        }

        @SuppressWarnings("unchecked")
        public <T extends CtMember> T getSource() {
            return (T) source;
        }

        @SuppressWarnings("unchecked")
        public <T extends CtMember> T getDelegate() {
            return (T) delegate;
        }

        public String getDelegateStatement() {
            return statement;
        }
    }

    public static boolean subclassOf(
            CtClass superClass,
            CtClass subClass)
            throws NotFoundException {
        if (subClass.subclassOf(superClass)) {
            return true;
        }

        CtClass[] interfaces = subClass.getInterfaces();
        if (interfaces != null) {
            for (CtClass itf : interfaces) {
                if (itf == superClass) {
                    return true;
                }
            }
        }
        return false;
    }

    public static CtMethod getDeclaredMethod(
            CtClass ctClass,
            String name,
            String signature)
            throws NotFoundException {

        for (CtMethod method : ctClass.getDeclaredMethods()) {
            if (method.getName().equals(name)
                    && method.getSignature().equals(signature)) {
                return method;
            }
        }
        throw new NotFoundException("Method " + name + signature +
                " not found in class " + ctClass.getName());
    }


    public static DelegateResult newMethodDelegate(
            CtClass clazz,
            CtMethod method,
            BodyProducer bodyProducer)
            throws NotFoundException, CannotCompileException {

        String methodName = method.getName();
        String delegateMethodName = clazz.makeUniqueName(methodName + "$delegate");
        method.setName(delegateMethodName);

        CtMethod newMethod = CtNewMethod.copy(method, methodName, clazz, null);
        int oldModifiers = method.getModifiers();
        int mod = Modifier.setPrivate(oldModifiers);

        if ((mod & AccessFlag.BRIDGE) != 0) {
            mod = mod & ~AccessFlag.BRIDGE;
        }
        if ((mod & AccessFlag.SYNTHETIC) != 0) {
            mod = mod & ~AccessFlag.SYNTHETIC;
        }

        method.setModifiers(mod);

        String type = method.getReturnType().getName();
        StringBuilder body = new StringBuilder();
        body.append("{");

        boolean isVoid = "void".equals(type);
        if (!isVoid) {
            body.append(type).append(" result = ");
        }
        String statement = delegateMethodName + "($$);";
        body.append(statement);
        if (!isVoid) {
            body.append("return result;");
        }
        body.append("}");

        DelegateResult result = new DelegateResult(newMethod, method, statement);
        String bodyString =
                bodyProducer == null ?
                        body.toString() : bodyProducer.createBody(body.toString(), result);
        try {
            String s = bodyString.replaceAll("\n", "");
            newMethod.setBody(s);
        } catch (CannotCompileException e) {
            Logger.error("Create method delegate error with body \n" + bodyString + "\n", e);
            throw e;
        }
        clazz.addMethod(newMethod);
        return result;
    }

    public static DelegateResult newConstructorDelegate(
            ClassPool classPool,
            CtClass clazz,
            CtConstructor constructor,
            BodyProducer bodyProducer)
            throws NotFoundException, CannotCompileException {

        String delegateMethodName = clazz.makeUniqueName("_init_$delegate");
        CtMethod delegateMethod = constructor.toMethod(delegateMethodName, clazz);
        delegateMethod.setModifiers(Modifier.PRIVATE);

        MethodInfo methodInfo = constructor.getMethodInfo();
        CodeAttribute sourceCodeAttr = methodInfo.getCodeAttribute();

        DelegateResult result = null;
        try {
            byte[] code = sourceCodeAttr.getCode();
            CodeIterator iterator = sourceCodeAttr.iterator();
            int pos = iterator.skipConstructor();
            if (pos >= 0) {
                int len = pos + 3;
                byte[] codeCopy = new byte[len + 1];
                codeCopy[len] = (byte) Opcode.RETURN;
                System.arraycopy(code, 0, codeCopy, 0, len);

                CtConstructor constructorCopy =
                        new CtConstructor(
                                constructor.getParameterTypes(),
                                clazz);

                constructorCopy.setExceptionTypes(constructor.getExceptionTypes());
                constructorCopy.setModifiers(constructor.getModifiers());
                MethodInfo methodInfoCopy = constructorCopy.getMethodInfo();
                ConstPool cp = methodInfo.getConstPool();

                CodeAttribute codeAttribute =
                        new CodeAttribute(
                                cp,
                                0,
                                0,
                                codeCopy,
                                new ExceptionTable(cp));

                methodInfoCopy.setCodeAttribute(codeAttribute);
                methodInfoCopy.rebuildStackMap(classPool);

                //Copy the original annotation
                List<AttributeInfo> attributes = methodInfo.getAttributes();
                for (AttributeInfo attribute : attributes) {
                    if (attribute instanceof AnnotationsAttribute) {
                        methodInfoCopy.addAttribute(attribute);
                    }
                }

                clazz.removeConstructor(constructor);
                clazz.addConstructor(constructorCopy);

                clazz.addMethod(delegateMethod);
                String statement = delegateMethodName + "($$);";

                result = new DelegateResult(constructor, delegateMethod, statement);
                String body =
                        bodyProducer == null ? statement :
                                bodyProducer.createBody(statement, result);
                try {
                    String s = body.replaceAll("\n", "");
                    constructorCopy.insertBeforeBody(s);
                } catch (CannotCompileException e) {
                    Logger.error("Create constructor delegate error with body \n" +
                            body + "\n", e);
                    throw e;
                }

            }
        } catch (BadBytecode e) {
            throw new CannotCompileException(e);
        }
        return result;
    }

    public static DelegateResult newInitializerDelegate(
            ClassPool classPool,
            CtClass clazz,
            CtConstructor constructor,
            BodyProducer bodyProducer)
            throws NotFoundException, CannotCompileException {

        String delegateMethodName = clazz.makeUniqueName("_clinit_$delegate");
        CtMethod delegateMethod = constructor.toMethod(delegateMethodName, clazz);
        delegateMethod.setModifiers(Modifier.PRIVATE | Modifier.STATIC);
        clazz.addMethod(delegateMethod);
        String statement = delegateMethodName + "();";

        DelegateResult result = new DelegateResult(constructor, delegateMethod, statement);
        String body =
                bodyProducer == null ? "{" + statement + "}" :
                        bodyProducer.createBody(statement, result);

        try {
            String s = body.replaceAll("\n", "");
            constructor.setBody(s);
        } catch (CannotCompileException e) {
            Logger.error("Create initializer delegate error with body \n" + body + "\n", e);
            throw e;
        }
        return result;
    }
}
