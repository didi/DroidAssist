package com.didichuxing.tools.droidassist.spec;

import org.apache.commons.io.FilenameUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * ClassFilterSpec with wildcard match
 */
@SuppressWarnings("WeakerAccess")
public class ClassFilterSpec {
    private Set<String> includes = new HashSet<>();
    private Set<String> excludes = new HashSet<>();

    public void addInclude(String filter) {
        if (filter == null) {
            return;
        }
        filter = filter.trim();
        if (filter.equals("")) {
            return;
        }
        includes.add(filter);
    }

    public void addExclude(String filter) {
        if (filter == null) {
            return;
        }
        filter = filter.trim();
        if (filter.equals("")) {
            return;
        }
        excludes.add(filter);
    }

    public void addIncludes(Collection<String> filters) {
        for (String filter : filters) {
            addInclude(filter);
        }
    }

    public void addExcludes(Collection<String> filters) {
        for (String filter : filters) {
            addExclude(filter);
        }
    }

    public Set<String> getIncludes() {
        return includes;
    }

    public Set<String> getExcludes() {
        return excludes;
    }

    private boolean isIncludeClass(String className) {
        if (includes.isEmpty()) {
            return false;
        }
        for (String fi : includes) {
            if (FilenameUtils.wildcardMatch(className, fi)) {
                return true;
            }
        }
        return false;
    }

    private boolean isExcludeClass(String className) {
        if (excludes.isEmpty()) {
            return false;
        }
        for (String fi : excludes) {
            if (FilenameUtils.wildcardMatch(className, fi)) {
                return true;
            }
        }
        return false;
    }

    public boolean classAllowed(String className) {
        return isIncludeClass(className) && !isExcludeClass(className);
    }

    @Override
    public String toString() {
        return "{" +
                "includes=" + includes +
                ", excludes=" + excludes +
                '}';
    }
}
