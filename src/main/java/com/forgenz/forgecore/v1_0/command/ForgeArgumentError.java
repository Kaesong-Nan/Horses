package com.forgenz.forgecore.v1_0.command;

import org.bukkit.ChatColor;

public final class ForgeArgumentError {
    private final String errorMessage;
    private final ErrorType type;

    private ForgeArgumentError(String errorMessage, ErrorType type) {
        this.errorMessage = errorMessage;
        this.type = type;
    }

    public String getMessage() {
        return this.errorMessage;
    }

    public ErrorType getType() {
        return this.type;
    }

    public static ForgeArgumentError buildError(ErrorType type, ForgeCommandArgument arg) {
        switch (type.ordinal()) {
            case 1:
            case 2:
            case 3:
                return type.getError();
            case 4:
        }
        return new ForgeArgumentError(arg.getError(), ErrorType.INVALID_ARG);
    }

    public enum ErrorType {
        GOOD(""),

        TOO_FEW_ARGS(String.format("%sToo few arguments", ChatColor.AQUA)),

        TOO_MANY_ARGS(String.format("%sToo many arguments", ChatColor.AQUA)),

        INVALID_ARG(null);

        private final ForgeArgumentError error;

        ErrorType(String error) {
            if (error != null) {
                this.error = new ForgeArgumentError(error, this);
            } else {
                this.error = null;
            }
        }

        private ForgeArgumentError getError() {
            return this.error;
        }
    }
}