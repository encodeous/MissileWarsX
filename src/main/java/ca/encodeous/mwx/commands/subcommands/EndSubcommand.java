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

public final class EndSubcommand {
    @SuppressWarnings("unused")
    private final MwPlugin plugin;

    public EndSubcommand(final MwPlugin plugin) {
        this.plugin = plugin;
    }

    public int execute(final CommandSourceStack source) {
        final CommandSender sender = source.getSender();
        final MwMatch match = MwGame.getInstance().getMatch();
        if (match == null || match.getState() == GameState.WAITING) {
            sender.sendMessage(Msg.component(Msg.NO_GAME_RUNNING));
            return Command.SINGLE_SUCCESS;
        }
        match.endGame();
        sender.sendMessage(Msg.component("&eGame ended by admin."));
        return Command.SINGLE_SUCCESS;
    }

    public LiteralArgumentBuilder<CommandSourceStack> node() {
        return Commands.literal("end").executes(ctx -> execute(ctx.getSource()));
    }
}
