package com.didichuxing.tools.droidassist.transform;

import com.didichuxing.tools.droidassist.ex.DroidAssistNotFoundException;
import com.didichuxing.tools.droidassist.spec.ClassFilterSpec;
import com.didichuxing.tools.droidassist.util.Logger;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.NotFoundException;

/**
 * An abstract Transform that processes class with different way.
 *
 * <p> There are five major categories of {@link com.didichuxing.tools.droidassist.transform.around.AroundTransformer},
 * {@link com.didichuxing.tools.droidassist.transform.enhance.TimingTransformer},
 * {@link com.didichuxing.tools.droidassist.transform.enhance.TryCatchTransformer},
 * {@link com.didichuxing.tools.droidassist.transform.insert.InsertTransformer},
 * {@link com.didichuxing.tools.droidassist.transform.replace.ReplaceTransformer}
 * and 28 implementation class.
 */
@SuppressWarnings("WeakerAccess")
public abstract class Transformer {
    public ClassPool classPool;
    public ClassFilterSpec classFilterSpec = new ClassFilterSpec();
    protected boolean abortOnUndefinedClass = false;

    //Transformer name
    public String getName() {
        return "Transformer";
    }

    //Category name that transformer belongs to
    public String getCategoryName() {
        return "Transformer";
    }

    public String getPrettyName() {
        return "Transformer";
    }

    protected abstract boolean onTransform(
            CtClass inputClass,
            String inputClassName)
            throws NotFoundException, CannotCompileException;

    public boolean performTransform(
            CtClass inputClass,
            String className)
            throws NotFoundException, CannotCompileException {

        inputClass.stopPruning(true);
        if (inputClass.isFrozen()) {
            inputClass.defrost();
        }
        beforeTransform();
        return onTransform(inputClass, className);
    }

    protected void beforeTransform() {
    }

    public boolean classAllowed(String className) {
        return classFilterSpec.classAllowed(className);
    }

    public Transformer setClassPool(ClassPool classPool) {
        this.classPool = classPool;
        return this;
    }

    public ClassPool getClassPool() {
        return classPool;
    }

    public void check() {
    }

    public boolean isAbortOnUndefinedClass() {
        return abortOnUndefinedClass;
    }

    public Transformer setAbortOnUndefinedClass(boolean abortOnUndefinedClass) {
        this.abortOnUndefinedClass = abortOnUndefinedClass;
        return this;
    }

    //Get class in the class pool
    protected CtClass tryGetClass(String className, String loc) {
        CtClass ctClass = classPool.getOrNull(className);
        if (ctClass == null) {
            String msg = "cannot find " + className + " in " + loc;
            if (abortOnUndefinedClass) {
                throw new DroidAssistNotFoundException(msg);
            } else {
                Logger.warning(msg);
            }
        } else {
            return ctClass;
        }
        return null;
    }

    protected Boolean isInterface(CtClass inputClass) {
        try {
            return inputClass.isInterface();
        } catch (Exception ignore) {
            return null;
        }
    }

    //Get all interfaces of the specified class
    protected CtClass[] tryGetInterfaces(CtClass inputClass) {
        try {
            return inputClass.getInterfaces();
        } catch (NotFoundException e) {
            String msg = "Cannot find interface " + e.getMessage() + " in " + inputClass.getName();
            if (abortOnUndefinedClass) {
                throw new DroidAssistNotFoundException(msg);
            } else {
                Logger.warning(msg);
            }
        }
        return new CtClass[0];
    }

    //Get all declared methods of the specified class
    protected CtMethod[] tryGetDeclaredMethods(CtClass inputClass) {
        CtMethod[] declaredMethods = new CtMethod[0];
        try {
            declaredMethods = inputClass.getDeclaredMethods();
        } catch (Exception e) {
            String msg = "Cannot get declared methods " + " in " + inputClass.getName();
            if (abortOnUndefinedClass) {
                throw new DroidAssistNotFoundException(msg);
            } else {
                Logger.warning(msg);
            }
        }
        return declaredMethods;
    }

    //Get all declared constructors of the specified class
    protected CtConstructor[] tryGetDeclaredConstructors(CtClass inputClass) {
        CtConstructor[] declaredConstructors = new CtConstructor[0];
        try {
            declaredConstructors = inputClass.getDeclaredConstructors();
        } catch (Exception e) {
            String msg = "Cannot get declared constructors " + " in " + inputClass.getName();
            if (abortOnUndefinedClass) {
                throw new DroidAssistNotFoundException(msg);
            } else {
                Logger.warning(msg);
            }
        }
        return declaredConstructors;
    }

    //Get initialization method of the specified class
    protected CtConstructor tryGetClassInitializer(CtClass inputClass) {
        CtConstructor initializer = null;
        try {
            initializer = inputClass.getClassInitializer();
        } catch (Exception e) {
            String msg = "Cannot get class initializer " + " in " + inputClass.getName();
            if (abortOnUndefinedClass) {
                throw new DroidAssistNotFoundException(msg);
            } else {
                Logger.warning(msg);
            }
        }
        return initializer;
    }


    @Override
    public String toString() {
        return getName() + "{" +
                "filterClass=" + classFilterSpec +
                '}';
    }
}
