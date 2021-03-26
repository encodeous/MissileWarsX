package ca.encodeous.mwx.commands.subcommands;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;

import ca.encodeous.mwx.MwGame;
import ca.encodeous.mwx.MwPlugin;
import ca.encodeous.mwx.commands.MwxSuggestions;
import ca.encodeous.mwx.game.GameState;
import ca.encodeous.mwx.game.MwMatch;
import ca.encodeous.mwx.util.Msg;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;

public final class SetMapSubcommand {
    private final MwPlugin plugin;

    public SetMapSubcommand(final MwPlugin plugin) {
        this.plugin = plugin;
    }

    public LiteralArgumentBuilder<CommandSourceStack> node() {
        return Commands.literal("setmap")
                .then(Commands.argument("name", word())
                        .suggests((ctx, builder) -> MwxSuggestions.suggestMaps(plugin, builder))
                        .executes(ctx -> {
                            final CommandSender sender = ctx.getSource().getSender();
                            final MwMatch match = MwGame.getInstance().getMatch();
                            if (match != null && match.getState() == GameState.PLAYING) {
                                sender.sendMessage(Msg.component("&cCannot change map while a game is running."));
                                return Command.SINGLE_SUCCESS;
                            }
                            final String mapName = getString(ctx, "name");
                            plugin.getGame().setMap(mapName);
                            sender.sendMessage(Msg.component("&aMap set to: &e" + mapName));
                            return Command.SINGLE_SUCCESS;
                        }));
    }
}

