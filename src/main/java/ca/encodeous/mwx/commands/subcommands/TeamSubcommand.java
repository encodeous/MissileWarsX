package ca.encodeous.mwx.commands.subcommands;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;

import ca.encodeous.mwx.MwGame;
import ca.encodeous.mwx.MwPlugin;
import ca.encodeous.mwx.commands.MwxSuggestions;
import ca.encodeous.mwx.data.PlayerTeam;
import ca.encodeous.mwx.game.MwMatch;
import ca.encodeous.mwx.util.Msg;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class TeamSubcommand {
    @SuppressWarnings("unused")
    private final MwPlugin plugin;

    public TeamSubcommand(final MwPlugin plugin) {
        this.plugin = plugin;
    }

    public int execute(final CommandSourceStack source, final String playerName, final String teamRaw) {
        final CommandSender sender = source.getSender();
        final Player target = Bukkit.getPlayerExact(playerName);
        if (target == null) {
            sender.sendMessage(Msg.component("&cPlayer not found: " + playerName));
            return Command.SINGLE_SUCCESS;
        }

        final String normalized = teamRaw.toLowerCase();
        final PlayerTeam team = switch (normalized) {
            case "red" -> PlayerTeam.RED;
            case "green" -> PlayerTeam.GREEN;
            default -> null;
        };
        if (team == null) {
            sender.sendMessage(Msg.component("&cInvalid team. Use 'red' or 'green'."));
            return Command.SINGLE_SUCCESS;
        }

        final MwMatch match = MwGame.getInstance().getMatch();
        if (match == null) {
            sender.sendMessage(Msg.component("&cNo active match."));
            return Command.SINGLE_SUCCESS;
        }
        match.addPlayer(target, team);
        sender.sendMessage(Msg.component("&aAdded &e" + target.getName() + " &ato team " + normalized + "."));
        return Command.SINGLE_SUCCESS;
    }

    public LiteralArgumentBuilder<CommandSourceStack> node() {
        return Commands.literal("team")
                .then(Commands.argument("player", word())
                        .suggests((ctx, builder) -> MwxSuggestions.suggestPlayers(builder))
                        .then(Commands.argument("team", word())
                                .suggests((ctx, builder) -> MwxSuggestions.suggestTeams(builder))
                                .executes(ctx -> execute(ctx.getSource(), getString(ctx, "player"), getString(ctx, "team")))));
    }
}
