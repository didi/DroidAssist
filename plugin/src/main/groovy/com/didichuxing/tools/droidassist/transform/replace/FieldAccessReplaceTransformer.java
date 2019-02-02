package com.didichuxing.tools.droidassist.transform.replace;

import com.didichuxing.tools.droidassist.util.Logger;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.expr.FieldAccess;

/**
 * Transform that replaces field-access with new code.
 */
public class FieldAccessReplaceTransformer extends ReplaceTransformer {

    private boolean fieldWrite = false;

    @Override
    public String getName() {
        return "FieldAccessReplaceTransformer";
    }

    @Override
    protected String getTransformType() {
        return TRANSFORM_EXPR;
    }

    @Override
    protected String getExecuteType() {
        return FIELD_ACCESS;
    }

    @Override
    protected boolean filterClass(CtClass inputClass, String inputClassName) {
        return !isMatchSourceClassName(inputClassName);
    }

    @Override
    protected boolean execute(
            CtClass inputClass,
            String inputClassName,
            FieldAccess fieldAccess)
            throws CannotCompileException, NotFoundException {

        String insnClassName = fieldAccess.getClassName();
        String insnSignature = fieldAccess.getSignature();
        String insnFieldName = fieldAccess.getFieldName();

        if (!isMatchFieldSource(insnClassName, insnSignature, insnFieldName)
                || !meetConditions(fieldAccess)) {
            return false;
        }

        String target = getTarget();
        String replacement = replaceInstrument(fieldAccess, target);

        Logger.warning(getPrettyName() + " by: " +
                (fieldAccess.isWriter() ? " write" : " read") + replacement + " at " +
                inputClassName + ".java" + ":" + fieldAccess.getLineNumber());
        return true;
    }

    private boolean meetConditions(FieldAccess fieldAccess) {
        return fieldAccess.isWriter() == fieldWrite;
    }

    public FieldAccessReplaceTransformer setFieldWrite(boolean write) {
        this.fieldWrite = write;
        return this;
    }
}
