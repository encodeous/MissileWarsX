package ca.encodeous.mwx.commands.subcommands;

import ca.encodeous.mwx.MwGame;
import ca.encodeous.mwx.MwPlugin;
import ca.encodeous.mwx.data.PlayerTeam;
import ca.encodeous.mwx.game.MwMatch;
import ca.encodeous.mwx.util.Msg;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class SpectateSubcommand {
    @SuppressWarnings("unused")
    private final MwPlugin plugin;

    public SpectateSubcommand(final MwPlugin plugin) {
        this.plugin = plugin;
    }

    public int execute(final CommandSourceStack source) {
        final CommandSender sender = source.getSender();
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Msg.component("&cOnly players can use this command."));
            return Command.SINGLE_SUCCESS;
        }
        final MwMatch match = MwGame.getInstance().getMatch();
        if (match == null) {
            sender.sendMessage(Msg.component("&cNo active match."));
            return Command.SINGLE_SUCCESS;
        }
        match.addPlayer(player, PlayerTeam.SPECTATOR);
        return Command.SINGLE_SUCCESS;
    }

    public LiteralArgumentBuilder<CommandSourceStack> node() {
        return Commands.literal("spectate").executes(ctx -> execute(ctx.getSource()));
    }
}

