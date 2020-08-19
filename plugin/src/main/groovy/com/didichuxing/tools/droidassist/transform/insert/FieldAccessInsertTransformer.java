package com.didichuxing.tools.droidassist.transform.insert;

import com.didichuxing.tools.droidassist.util.Logger;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.expr.FieldAccess;

/**
 * Transform that inserts custom code at the pointcut where field is read or written.
 */
public class FieldAccessInsertTransformer extends InsertTransformer {

    private boolean fieldWrite = false;

    @Override
    public String getName() {
        return "FieldAccessInsertTransformer";
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
        String proceed = fieldAccess.isWriter() ? "$proceed($$);" : "$_=$proceed($$);";
        String statement = ""
                + "{"
                + (isAsBefore() ? target : "")
                + proceed
                + (isAsAfter() ? target : "")
                + "}";

        String replacement = replaceInstrument(inputClassName, fieldAccess, statement);

        Logger.warning(getPrettyName() + " by: " + replacement
                + " at " + inputClassName + ".java" + ":" + fieldAccess.getLineNumber());
        return true;
    }

    private boolean meetConditions(FieldAccess fieldAccess) {
        return fieldAccess.isWriter() == fieldWrite;
    }

    public FieldAccessInsertTransformer setFieldWrite(boolean write) {
        this.fieldWrite = write;
        return this;
    }
}
