package ca.encodeous.mwx.commands.subcommands;

import ca.encodeous.mwx.MwGame;
import ca.encodeous.mwx.MwPlugin;
import ca.encodeous.mwx.game.GameState;
import ca.encodeous.mwx.game.MwMatch;
import ca.encodeous.mwx.util.Msg;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;

public final class StartSubcommand {
    private final MwPlugin plugin;

    public StartSubcommand(final MwPlugin plugin) {
        this.plugin = plugin;
    }

    public int execute(final CommandSourceStack source) {
        final CommandSender sender = source.getSender();
        final MwMatch match = MwGame.getInstance().getMatch();
        if (match == null) {
            sender.sendMessage(Msg.component("&cNo active match. Initializing..."));
            plugin.getGame().createNewMatch();
            return Command.SINGLE_SUCCESS;
        }
        final GameState state = match.getState();
        if (state == GameState.PLAYING) {
            sender.sendMessage(Msg.component("&cGame is already running!"));
            return Command.SINGLE_SUCCESS;
        }
        if (state != GameState.WAITING && state != GameState.STARTING) {
            sender.sendMessage(Msg.component("&cCannot start during: " + state));
            return Command.SINGLE_SUCCESS;
        }
        match.beginGame();
        sender.sendMessage(Msg.component("&aForce-started the game."));
        return Command.SINGLE_SUCCESS;
    }

    public LiteralArgumentBuilder<CommandSourceStack> node() {
        return Commands.literal("start").executes(ctx -> execute(ctx.getSource()));
    }
}
