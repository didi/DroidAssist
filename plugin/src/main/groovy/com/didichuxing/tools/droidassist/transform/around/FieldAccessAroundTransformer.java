package com.didichuxing.tools.droidassist.transform.around;

import com.didichuxing.tools.droidassist.util.Logger;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.expr.FieldAccess;

/**
 * Transform that adds code before and after the field access simultaneously.
 */
public class FieldAccessAroundTransformer extends AroundTransformer {

    //When fieldWrite is true, representing variable is written.
    private boolean fieldWrite = false;

    @Override
    public String getName() {
        return "FieldAccessAroundTransformer";
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

        String before = getTargetBefore();
        String after = getTargetAfter();

        String proceed = fieldAccess.isWriter() ? "$proceed($$);" : "$_=$proceed($$);";
        String statement = "{" + before + proceed + after + "}";
        String replacement = replaceInstrument(fieldAccess, statement);

        Logger.warning(getPrettyName() + " by: " + replacement
                + " at " + inputClassName + ".java" + ":" + fieldAccess.getLineNumber());
        return true;
    }

    private boolean meetConditions(FieldAccess fieldAccess) {
        return fieldAccess.isWriter() == fieldWrite;
    }

    public FieldAccessAroundTransformer setFieldWrite(boolean write) {
        this.fieldWrite = write;
        return this;
    }
}
