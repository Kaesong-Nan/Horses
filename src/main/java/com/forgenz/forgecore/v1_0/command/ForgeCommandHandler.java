package com.forgenz.forgecore.v1_0.command;

import com.forgenz.forgecore.v1_0.ForgeCore;
import com.forgenz.forgecore.v1_0.bukkit.ForgePlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public final class ForgeCommandHandler extends ForgeCommand
        implements ForgeCore, CommandExecutor {
    private static final Pattern NUMBER = Pattern.compile("\\d+");
    public static final String HEADER_REPLACE_PLUGIN_NAME = "%NAME%";
    public static final String HEADER_REPLACE_VERSION = "%VERSION%";
    public static final String HEADER_REPLACE_AUTHORS = "%AUTHORS%";
    private static final Pattern HEADER_REPLACE_PATTERN_PLUGIN_NAME = Pattern.compile("%NAME%", 16);
    private static final Pattern HEADER_REPLACE_PATTERN_VERSION = Pattern.compile("%VERSION%", 16);
    private static final Pattern HEADER_REPLACE_PATTERN_AUTHORS = Pattern.compile("%AUTHORS%", 16);
    public static final String HELP_REPLACE_CMD_ALIAS = "%1$s";
    public static final String HELP_REPLACE_SUBCOMMAND = "%2$s";
    public static final String HELP_REPLACE_SUBCOMMAND_ALIASES = "%3$s";
    public static final String HELP_REPLACE_ARGUMENTS = "%4$s";
    public static final String HELP_REPLACE_DESCRIPTION = "%5$s";
    public static final String HELP_MISSING_COMMAND_REPLACE_ARGUMENT = "%1$s";
    private final ArrayList<ForgeCommand> registeredCommands = new ArrayList();
    private final HashMap<String, ForgeCommand> aliases = new HashMap();

    private int numCommandsPerHelpPage = 8;
    private String header;
    private String helpCommandFormat;
    private String helpMissingCommand;
    private String noPermission;
    private boolean showAllCommands = false;

    public ForgeCommandHandler(ForgePlugin plugin) {
        super(plugin);

        setHeaderFormat(String.format("%1$s%3$s %2$sv%1$s%4$s %2$sby %1$s%5$s", ChatColor.DARK_GREEN, ChatColor.YELLOW, "%NAME%", "%VERSION%", "%AUTHORS%"));
        setHelpCommandFormat(String.format("%1$s/%4$s %5$s %2$s%6$s %3$s%7$s", ChatColor.AQUA, ChatColor.DARK_AQUA, ChatColor.YELLOW, "%1$s", "%3$s", "%4$s", "%5$s"));
        setHelpMissingCommandFormat(String.format("%sNo sub-commands like %s", ChatColor.RED, "%1$s"));
        setNoPermissionMessage(String.format("%sNo permission to use this sub-command", ChatColor.RED));

        registerAlias("help", true);
        registerAlias("h", false);
        registerAlias("?", false);

        registerArgument(new ForgeCommandArgument("^.+$", true, ""));

        setArgumentString("[subcommand]");
        setDescription("Shows information about subcommands");

        registerCommand(this);
    }

    public void setNumCommandsPerHelpPage(int count) {
        if (count <= 0) {
            return;
        }

        this.numCommandsPerHelpPage = count;
    }

    public void setHeaderFormat(String headerFormat) {
        if (headerFormat == null) {
            return;
        }

        this.header = HEADER_REPLACE_PATTERN_PLUGIN_NAME.matcher(headerFormat).replaceAll(getPlugin().getName());
        this.header = HEADER_REPLACE_PATTERN_VERSION.matcher(this.header).replaceAll(getPlugin().getDescription().getVersion());
        this.header = HEADER_REPLACE_PATTERN_AUTHORS.matcher(this.header).replaceAll(getPlugin().getAuthors());
    }

    public void setHelpCommandFormat(String helpCommandFormat) {
        if (helpCommandFormat == null) {
            return;
        }

        this.helpCommandFormat = helpCommandFormat;
    }

    public void setHelpMissingCommandFormat(String helpMissingCommand) {
        if (helpMissingCommand == null) {
            return;
        }

        this.helpMissingCommand = helpMissingCommand;
    }

    public void setNoPermissionMessage(String noPermission) {
        if (noPermission == null) {
            return;
        }

        this.noPermission = noPermission;
    }

    public void setShowAllCommands(boolean showAll) {
        this.showAllCommands = showAll;
    }

    public void registerCommand(ForgeCommand command)
            throws IllegalArgumentException {
        if (!this.registeredCommands.add(command)) {
            throw new IllegalArgumentException(String.format("The command '%s' is already been registered", command.getMainCommand()));
        }

        for (String alias : command.getAliases()) {
            this.aliases.put(alias, command);
        }
    }

    public final ForgeCommand findCommand(String like) {
        ForgeCommand command = this.aliases.get(like);

        if (command == null) {
            for (Map.Entry<String, ForgeCommand> e : this.aliases.entrySet()) {
                if (e.getKey().startsWith(like)) {
                    command = e.getValue();
                    break;
                }
            }
        }

        return command;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(this.header);
            if (!this.registeredCommands.isEmpty()) {
                sender.sendMessage(String.format("%sType /%s help to see subcommands", ChatColor.AQUA, label));
            }
            return true;
        }

        ForgeCommand subCommand = findCommand(args[0]);

        if (subCommand == null) {
            sender.sendMessage(String.format(this.helpMissingCommand, args[0]));
            return true;
        }

        args[0] = subCommand.getMainCommand();

        boolean isPlayer = sender instanceof Player;

        if ((!isPlayer) && (!subCommand.allowConsole())) {
            sender.sendMessage(String.format("%1$sOnly players can use this sub-command", ChatColor.RED));
            return true;
        }

        if ((isPlayer) && (!subCommand.checkPermissions((Player) sender))) {
            sender.sendMessage(this.noPermission);
            return true;
        }

        ForgeArgs arguments = new ForgeArgs(label, args);

        if (!subCommand.validateArguments(sender, arguments)) {
            sender.sendMessage(String.format("%1$sType %2$s/%4$s help %3$s%5$s %1$sto see how to use this command", ChatColor.YELLOW, ChatColor.AQUA, ChatColor.DARK_AQUA, label, arguments.getSubCommandAlias()));
            return true;
        }

        subCommand.onCommand(sender, arguments);

        return true;
    }

    protected final void onCommand(CommandSender sender, ForgeArgs args) {
        boolean player = sender instanceof Player;

        sender.sendMessage(this.header);

        boolean hasPage = false;

        if ((args.getNumArgs() == 1) && (!(hasPage = NUMBER.matcher(args.getArg(0)).matches()))) {
            ForgeCommand command = findCommand(args.getArg(0));

            if (command != null) {
                if ((!player) || (this.showAllCommands) || (command.checkPermissions((Player) sender))) {
                    sender.sendMessage(String.format(this.helpCommandFormat, args.getCommandUsed(), args.getSubCommandAlias(), command.getAliasString(), command.getArgString(), command.getDescription()));
                } else {
                    sender.sendMessage(this.noPermission);
                }
            } else {
                sender.sendMessage(String.format(this.helpMissingCommand, args.getArg(0)));
            }
            return;
        }

        int page = 1;
        if (hasPage) {
            page = Integer.parseInt(args.getArg(0));
        }

        sender.sendMessage(String.format("%1$sShowing page %2$s%3$d %1$sof %2$s%4$d", ChatColor.YELLOW, ChatColor.AQUA, page, this.registeredCommands.size() / this.numCommandsPerHelpPage + 1));

        for (int i = (page - 1) * this.numCommandsPerHelpPage; (i < this.registeredCommands.size()) && (i < page * this.numCommandsPerHelpPage); i++) {
            ForgeCommand command = this.registeredCommands.get(i);

            if ((!player) || (this.showAllCommands) || (command.checkPermissions((Player) sender))) {
                sender.sendMessage(String.format(this.helpCommandFormat, args.getCommandUsed(), command.getMainCommand(), command.getAliasString(), command.getArgString(), command.getDescription()));
            }
        }
    }
}