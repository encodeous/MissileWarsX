package ca.encodeous.mwx.command;

import ca.encodeous.mwx.command.nms.CommandListenerWrapper;
import ca.encodeous.simplenms.proxy.NMSCore;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.function.Predicate;

public final class RootCommand extends CommandNode {

    private final String[] aliases;

    public RootCommand(String name, Predicate<CommandListenerWrapper> usageRequirement, String... aliases) {
        super(LiteralArgumentBuilder.literal(name).requires((o) ->
                usageRequirement.test(NMSCore.getNMSObject(CommandListenerWrapper.class, o))));
        this.aliases = aliases;
    }

    public RootCommand(String name, String... aliases) {
        super(LiteralArgumentBuilder.literal(name));
        this.aliases = aliases;
    }

    public void Register(CommandDispatcher<Object> dispatcher) {
        try{
            var builder = (LiteralArgumentBuilder<Object>) command;
            Bukkit.getPluginManager().addPermission(new Permission("minecraft.command." + builder.getLiteral(), PermissionDefault.TRUE));
            for(String alias : aliases) Bukkit.getPluginManager().addPermission(new Permission("minecraft.command." + alias, PermissionDefault.TRUE));
            var cmd = dispatcher.register(builder);
            for(String alias : aliases) {
                var aliasCmd = LiteralArgumentBuilder.literal(alias).redirect(cmd).executes(builder.getCommand());
                if(builder.getRequirement() != null) aliasCmd.requires(builder.getRequirement());
                dispatcher.register(aliasCmd);
            }
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    public RootCommand SubCommand(CommandNode subCommand) {
        super.SubCommand(subCommand);
        return this;
    }

    public RootCommand Executes(ExecutionSource requirement, Command cmd) {
        super.Executes(requirement, cmd);
        return this;
    }

    public RootCommand Executes(Command command) {
        super.Executes(command);
        return this;
    }
}
