package com.didichuxing.tools.droidassist.transform.insert;

import com.didichuxing.tools.droidassist.transform.ExprExecTransformer;

/**
 * An abstract transform that inserts code at the pointcut.
 *
 * <p> See {@link ConstructorCallInsertTransformer}, {@link ConstructorExecutionInsertTransformer},
 * {@link FieldAccessInsertTransformer}, {@link InitializerExecutionInsertTransformer},
 * {@link MethodCallInsertTransformer}, {@link MethodExecutionInsertTransformer}
 */
@SuppressWarnings("WeakerAccess")
public abstract class InsertTransformer extends ExprExecTransformer {

    private boolean asBefore = false;
    private boolean asAfter = false;

    @Override
    public String getCategoryName() {
        return "Insert";
    }

    @Override
    public String getName() {
        return "InsertTransformer";
    }

    public boolean isAsBefore() {
        return asBefore;
    }

    public InsertTransformer setAsBefore(boolean asBefore) {
        this.asBefore = asBefore;
        return this;
    }

    public boolean isAsAfter() {
        return asAfter;
    }

    public InsertTransformer setAsAfter(boolean asAfter) {
        this.asAfter = asAfter;
        return this;
    }

}
