package com.didichuxing.tools.droidassist.util

import com.google.common.collect.Lists

class TimingLogger {

    private String tag
    private String label
    private String beginMsg
    private boolean disabled
    private ArrayList<Long> splits
    private ArrayList<String> splitLabels

    TimingLogger(String tag, String label, String beginMsg) {
        reset(tag, label, beginMsg)
    }

    TimingLogger(String tag, String label) {
        this(tag, label, "")
    }

    void reset(String tag, String label) {
        reset(tag, label, "")
    }

    void reset(String tag, String label, String beginMsg) {
        this.tag = tag
        this.label = label
        this.beginMsg = beginMsg
        reset()
    }

    void reset() {
        if (disabled) {
            return
        }
        if (splits == null) {
            splits = Lists.newArrayList()
            splitLabels = Lists.newArrayList()
        } else {
            splits.clear()
            splitLabels.clear()
        }
        addSplit(null)
    }


    void addSplit(String splitLabel) {
        if (disabled) {
            return
        }
        long now = System.currentTimeMillis()
        splits.add(now)
        splitLabels.add(splitLabel)
    }


    void dumpToLog() {
        if (disabled) {
            return
        }
        Logger.debug(tag, label + ": begin: " + beginMsg)
        final long first = splits.get(0)
        long now = first
        for (int i = 1; i < splits.size(); i++) {
            now = splits.get(i)
            final String splitLabel = splitLabels.get(i)
            final long prev = splits.get(i - 1)

            Logger.debug(tag, label + ":" +
                    " ${(now - prev)} ms".padRight(7) + ", " +
                    splitLabel)
        }
        Logger.debug(tag, label + ": end, " + (now - first) + " ms")
    }
}
