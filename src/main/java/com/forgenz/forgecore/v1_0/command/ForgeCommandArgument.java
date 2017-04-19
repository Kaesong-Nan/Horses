package com.forgenz.forgecore.v1_0.command;

import java.util.regex.Pattern;

public final class ForgeCommandArgument {
    private final Pattern pattern;
    private final boolean optional;
    private final String error;

    public ForgeCommandArgument(String patternStr, boolean optional, String error) {
        this(patternStr, 0, optional, error);
    }

    public ForgeCommandArgument(String patternStr, int patternArg, boolean optional, String error) {
        this.pattern = Pattern.compile(patternStr, patternArg);
        this.optional = optional;
        this.error = error;
    }

    public boolean argumentMatches(String arg) {
        return this.pattern.matcher(arg).matches();
    }

    public boolean isOptional() {
        return this.optional;
    }

    public String getError() {
        return this.error;
    }
}