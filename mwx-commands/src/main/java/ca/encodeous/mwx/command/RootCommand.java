package ca.encodeous.mwx.command;

import ca.encodeous.mwx.command.nms.CommandListenerWrapper;
import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.simplenms.proxy.NMSCore;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.function.Predicate;

public final class RootCommand extends CommandSubCommand {

    private final String[] aliases;
    private final boolean noChecks;

    public RootCommand(String name, Predicate<CommandListenerWrapper> usageRequirement, String... aliases) {
        super(LiteralArgumentBuilder.literal(name).requires((o) ->
                usageRequirement.test(NMSCore.getNMSObject(CommandListenerWrapper.class, o))));
        this.aliases = aliases;
        noChecks = false;
    }

    public RootCommand(String name, String... aliases) {
        super(LiteralArgumentBuilder.literal(name));
        this.aliases = aliases;
        noChecks = true;
    }

    public void Register(CommandDispatcher<Object> dispatcher) {
        var builder = (LiteralArgumentBuilder<Object>) command;
        if(noChecks) {
            Bukkit.getPluginManager().addPermission(new Permission("minecraft.command." + builder.getLiteral(), PermissionDefault.TRUE));
            for(String alias : aliases) Bukkit.getPluginManager().addPermission(new Permission("minecraft.command." + alias, PermissionDefault.TRUE));
        }
        var cmd = dispatcher.register(builder);
        for(String alias : aliases) {
            var aliasCmd = LiteralArgumentBuilder.literal(alias).redirect(cmd).executes(builder.getCommand());
            if(builder.getRequirement() != null) aliasCmd.requires(builder.getRequirement());
            dispatcher.register(aliasCmd);
        }
    }

    public RootCommand SubCommand(CommandSubCommand subCommand) {
        super.SubCommand(subCommand);
        return this;
    }

    public RootCommand Executes(CommandExecutionRequirement requirement, Command cmd) {
        super.Executes(requirement, cmd);
        return this;
    }

    public RootCommand Executes(Command command) {
        super.Executes(command);
        return this;
    }
}
