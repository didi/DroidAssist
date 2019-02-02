package com.didichuxing.tools.droidassist.transform.around;

import com.didichuxing.tools.droidassist.transform.ExprExecTransformer;

/**
 * An abstract transform that adds code before and after the target pointcut simultaneously.
 *
 * <p> See {@link ConstructorCallAroundTransformer}, {@link ConstructorExecutionAroundTransformer},
 * {@link FieldAccessAroundTransformer}, {@link InitializerExecutionAroundTransformer},
 * {@link MethodCallAroundTransformer}, {@link MethodExecutionAroundTransformer}
 */
public abstract class AroundTransformer extends ExprExecTransformer {

    private String targetBefore;

    private String targetAfter;

    public String getTargetBefore() {
        return targetBefore;
    }

    public AroundTransformer setTargetBefore(String targetBefore) {
        this.targetBefore = targetBefore.endsWith(";") ? targetBefore : targetBefore + ";";
        return this;
    }

    public String getTargetAfter() {
        return targetAfter;
    }

    public AroundTransformer setTargetAfter(String targetAfter) {
        this.targetAfter = targetAfter.endsWith(";") ? targetAfter : targetAfter + ";";
        return this;
    }

    @Override
    public String getName() {
        return "AroundTransformer";
    }

    @Override
    public String getCategoryName() {
        return "Around";
    }

    @Override
    public String toString() {
        return getName() + "\n{" +
                "\n    source='" + getSource() + '\'' +
                "\n    targetBefore='" + targetBefore + '\'' +
                "\n    targetAfter='" + targetAfter + '\'' +
                "\n    filterClass=" + classFilterSpec +
                "\n}";
    }


}
