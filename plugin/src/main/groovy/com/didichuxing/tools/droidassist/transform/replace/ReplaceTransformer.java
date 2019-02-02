package com.didichuxing.tools.droidassist.transform.replace;

import com.didichuxing.tools.droidassist.transform.ExprExecTransformer;

/**
 * An abstract transform that replaces the specified code with new code.
 *
 * <p> See{@link ConstructorCallReplaceTransformer}, {@link ConstructorExecutionReplaceTransformer},
 * {@link FieldAccessReplaceTransformer}, {@link InitializerExecutionReplaceTransformer},
 * {@link MethodCallReplaceTransformer}, {@link MethodExecutionReplaceTransformer}
 */
public abstract class ReplaceTransformer extends ExprExecTransformer {
    @Override
    public String getName() {
        return "ReplaceTransformer";
    }

    @Override
    public String getCategoryName() {
        return "Replace";
    }
}
