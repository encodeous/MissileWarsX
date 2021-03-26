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

public final class InfoSubcommand {
    @SuppressWarnings("unused")
    private final MwPlugin plugin;

    public InfoSubcommand(final MwPlugin plugin) {
        this.plugin = plugin;
    }

    public LiteralArgumentBuilder<CommandSourceStack> node() {
        return Commands.literal("info").executes(ctx -> {
            final CommandSender sender = ctx.getSource().getSender();
            final MwMatch match = MwGame.getInstance().getMatch();
            if (match == null) {
                sender.sendMessage(Msg.component("&7No active match."));
                return Command.SINGLE_SUCCESS;
            }
            sender.sendMessage(Msg.component("&6=== MissileWarsX Info ==="));
            sender.sendMessage(Msg.component("&7State: &e" + match.getState()));
            sender.sendMessage(Msg.component("&7Map: &e" + match.getMap().config.name));
            sender.sendMessage(Msg.component("&cRed: &f" + match.getRedPlayers().size() + " players"));
            sender.sendMessage(Msg.component("&aGreen: &f" + match.getGreenPlayers().size() + " players"));
            sender.sendMessage(Msg.component("&7Spectators: &f" + match.getSpectators().size()));
            sender.sendMessage(Msg.component("&7Lobby: &f" + match.getLobbyPlayers().size()));
            sender.sendMessage(Msg.component("&7Map busy: &f" + match.isBusy()));
            return Command.SINGLE_SUCCESS;
        });
    }
}

