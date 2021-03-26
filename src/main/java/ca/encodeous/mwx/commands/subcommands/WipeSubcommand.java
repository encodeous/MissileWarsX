package ca.encodeous.mwx.commands.subcommands;

import ca.encodeous.mwx.MwGame;
import ca.encodeous.mwx.MwPlugin;
import ca.encodeous.mwx.game.MwMatch;
import ca.encodeous.mwx.util.Msg;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;

public final class WipeSubcommand {
    @SuppressWarnings("unused")
    private final MwPlugin plugin;

    public WipeSubcommand(final MwPlugin plugin) {
        this.plugin = plugin;
    }

    public int execute(final CommandSourceStack source) {
        final CommandSender sender = source.getSender();
        final MwMatch match = MwGame.getInstance().getMatch();
        if (match == null) {
            sender.sendMessage(Msg.component("&cNo active match."));
            return Command.SINGLE_SUCCESS;
        }
        if (match.getMap().busy) {
            sender.sendMessage(Msg.component("&cMap is currently being modified."));
            return Command.SINGLE_SUCCESS;
        }

        match.getMap().resetMap(() -> sender.sendMessage(Msg.component("&aMap wiped successfully.")));
        sender.sendMessage(Msg.component("&eWiping map..."));
        return Command.SINGLE_SUCCESS;
    }

    public LiteralArgumentBuilder<CommandSourceStack> node() {
        return Commands.literal("wipe").executes(ctx -> execute(ctx.getSource()));
    }
}
