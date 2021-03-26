package ca.encodeous.mwx.commands.subcommands;

import ca.encodeous.mwx.MwPlugin;
import ca.encodeous.mwx.util.Msg;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;

public final class ReloadSubcommand {
    private final MwPlugin plugin;

    public ReloadSubcommand(final MwPlugin plugin) {
        this.plugin = plugin;
    }

    public LiteralArgumentBuilder<CommandSourceStack> node() {
        return Commands.literal("reload").executes(ctx -> {
            final CommandSender sender = ctx.getSource().getSender();
            plugin.getGame().reloadConfig();
            sender.sendMessage(Msg.component("&aConfig reloaded."));
            return Command.SINGLE_SUCCESS;
        });
    }
}

