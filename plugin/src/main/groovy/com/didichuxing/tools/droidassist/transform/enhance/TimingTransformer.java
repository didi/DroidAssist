package com.didichuxing.tools.droidassist.transform.enhance;

import com.didichuxing.tools.droidassist.transform.ExprExecTransformer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An abstract transform that adds time-consuming statistics code.
 *
 * <p> See {@link ConstructorCallTimingTransformer}, {@link ConstructorExecutionTimingTransformer},
 * {@link InitializerExecutionTimingTransformer}, {@link MethodCallTimingTransformer},
 * {@link MethodExecutionTimingTransformer}
 */
public abstract class TimingTransformer extends ExprExecTransformer {
    @Override
    public String getCategoryName() {
        return "Timing";
    }

    String getDefaultTimingStatement(boolean isVoidReturnType, String target) {
        String proceed = isVoidReturnType ? "$proceed($$);" : "$_ =$proceed($$);";
        return getTimingStatement(proceed, target);
    }

    String getTimingStatement(String proceed, String target) {
        return "long start = java.lang.System.nanoTime();" +
                proceed +
                "long nanoTime = java.lang.System.nanoTime()-start;" +
                target.replaceAll(Pattern.quote("$time"),
                        Matcher.quoteReplacement("(nanoTime/1000000)").
                                replaceAll(Pattern.quote("$nanotime"),
                                        Matcher.quoteReplacement("(nanoTime)")));
    }
}
