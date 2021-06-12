package com.forgenz.forgecore.v1_0.command;

import com.forgenz.forgecore.v1_0.ForgeCore;
import com.forgenz.forgecore.v1_0.bukkit.ForgePlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public abstract class ForgeCommand
        implements ForgeCore {
    private final ForgePlugin plugin;
    private String mainCommand;
    private final ArrayList<String> aliases = new ArrayList();
    private String aliasString = "";

    private final ArrayList<ForgeCommandArgument> args = new ArrayList();

    private int minArgs = 0;
    private int maxArgs = 0;
    private List<String> permissions;
    private boolean allowOp;
    private boolean allowConsole = true;

    private String argString = "";
    private String desc = "No Description";

    protected ForgeCommand(ForgePlugin plugin) {
        this.plugin = plugin;

        this.permissions = null;

        this.allowOp = true;
    }

    protected final void registerAlias(String alias, boolean main) {
        alias = alias.trim();

        if (this.aliasString.length() != 0) {
            this.aliasString += ",";
        }

        this.aliasString += alias;

        alias = alias.toLowerCase();

        if ((main) || (this.mainCommand == null)) {
            this.mainCommand = alias;
        }

        this.aliases.add(alias);
    }

    protected final void registerArgument(ForgeCommandArgument arg) {
        if ((!arg.isOptional()) && (this.minArgs != this.maxArgs)) {
            throw new IllegalArgumentException("All required arguments must be before any optional arguments");
        }

        if (arg.isOptional()) {
            this.maxArgs += 1;
        } else {
            this.maxArgs = (++this.minArgs);
        }

        this.args.add(arg);
    }

    protected final void registerPermission(String perm) {
        if (this.permissions == null) {
            this.permissions = new ArrayList<>(1);
        }

        this.permissions.add(perm);
    }

    protected final void setAllowOp(boolean allowOp) {
        this.allowOp = allowOp;
    }

    protected final void setAllowConsole(boolean allowConsole) {
        this.allowConsole = allowConsole;
    }

    protected final void setArgumentString(String argString) {
        this.argString = argString;
    }

    protected final void setDescription(String desc) {
        this.desc = desc;
    }

    protected final boolean checkPermissions(Player player) {
        if ((this.permissions == null) || ((this.allowOp) && (player.isOp()))) {
            return true;
        }

        for (String perm : this.permissions) {
            if (player.hasPermission(perm)) {
                return true;
            }
        }

        return false;
    }

    protected final boolean validateArguments(CommandSender sender, ForgeArgs args) {
        int length = args.getNumArgs();

        if (length < this.minArgs) {
            sender.sendMessage(ForgeArgumentError.buildError(ForgeArgumentError.ErrorType.TOO_FEW_ARGS, null).getMessage());
            return false;
        }

        if (length > this.maxArgs) {
            sender.sendMessage(ForgeArgumentError.buildError(ForgeArgumentError.ErrorType.TOO_MANY_ARGS, null).getMessage());
            return false;
        }

        for (int i = 0; i < length; i++) {
            ForgeCommandArgument argument = this.args.get(i);
            String value = args.getArg(i);

            if (!argument.argumentMatches(value)) {
                sender.sendMessage(argument.getError());
                return false;
            }
        }

        return true;
    }

    protected abstract void onCommand(CommandSender paramCommandSender, ForgeArgs paramForgeArgs);

    public final String getMainCommand() {
        return this.mainCommand;
    }

    public final String[] getAliases() {
        return this.aliases.toArray(new String[0]);
    }

    public final String getAliasString() {
        return this.aliasString;
    }

    public final String getArgString() {
        return this.argString;
    }

    public final boolean allowConsole() {
        return this.allowConsole;
    }

    public final String getDescription() {
        return this.desc;
    }

    public ForgePlugin getPlugin() {
        return this.plugin;
    }
}