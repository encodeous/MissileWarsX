package ca.encodeous.mwx.commands;

import ca.encodeous.mwx.MwPlugin;
import ca.encodeous.mwx.commands.subcommands.TeamSubcommand;
import ca.encodeous.mwx.commands.subcommands.LobbySubcommand;
import ca.encodeous.mwx.commands.subcommands.SpectateSubcommand;
import ca.encodeous.mwx.commands.subcommands.GiveSubcommand;
import ca.encodeous.mwx.commands.subcommands.InfoSubcommand;
import ca.encodeous.mwx.commands.subcommands.ReloadSubcommand;
import ca.encodeous.mwx.commands.subcommands.SetMapSubcommand;
import ca.encodeous.mwx.commands.subcommands.StartSubcommand;
import ca.encodeous.mwx.commands.subcommands.EndSubcommand;
import ca.encodeous.mwx.commands.subcommands.WipeSubcommand;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import java.util.List;

public final class MwxCommand {
    private static final String PERMISSION = "mwx.admin";

    private MwxCommand() {
    }

    public static void register(final MwPlugin plugin, final Commands commands) {
        final StartSubcommand start = new StartSubcommand(plugin);
        final EndSubcommand end = new EndSubcommand(plugin);
        final SpectateSubcommand spectate = new SpectateSubcommand(plugin);
        final LobbySubcommand lobby = new LobbySubcommand(plugin);
        final ReloadSubcommand reload = new ReloadSubcommand(plugin);
        final SetMapSubcommand setMap = new SetMapSubcommand(plugin);
        final InfoSubcommand info = new InfoSubcommand(plugin);
        final TeamSubcommand team = new TeamSubcommand(plugin);
        final GiveSubcommand give = new GiveSubcommand(plugin);
        final WipeSubcommand wipe = new WipeSubcommand(plugin);

        final LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("mwx")
                .executes(ctx -> {
                    MwxHelp.send(ctx.getSource().getSender(), ctx.getSource().getSender().hasPermission(PERMISSION));
                    return Command.SINGLE_SUCCESS;
                });

        // /mwx start/spectate/lobby are intentionally usable without mwx.admin.
        root.then(start.node());
        root.then(spectate.node());
        root.then(lobby.node());
        root.then(end.node().requires(src -> src.getSender().hasPermission(PERMISSION)));
        root.then(reload.node().requires(src -> src.getSender().hasPermission(PERMISSION)));
        root.then(setMap.node().requires(src -> src.getSender().hasPermission(PERMISSION)));
        root.then(info.node().requires(src -> src.getSender().hasPermission(PERMISSION)));
        root.then(team.node().requires(src -> src.getSender().hasPermission(PERMISSION)));
        root.then(give.node().requires(src -> src.getSender().hasPermission(PERMISSION)));
        root.then(wipe.node().requires(src -> src.getSender().hasPermission(PERMISSION)));

        // Explicit help alias under /mwx help for discoverability.
        root.then(Commands.literal("help").executes(ctx -> {
            MwxHelp.send(ctx.getSource().getSender(), ctx.getSource().getSender().hasPermission(PERMISSION));
            return Command.SINGLE_SUCCESS;
        }));

        commands.register(
                root.build(),
                "MissileWarsX command",
                List.of()
        );

        // Standalone aliases
        commands.register(
                Commands.literal("start")
                        .executes(ctx -> start.execute(ctx.getSource()))
                        .build(),
                "Start a MissileWarsX match",
                List.of()
        );
        commands.register(
                Commands.literal("spectate")
                        .executes(ctx -> spectate.execute(ctx.getSource()))
                        .build(),
                "Enter spectator mode for MissileWarsX",
                List.of()
        );
        commands.register(
                Commands.literal("lobby")
                        .executes(ctx -> lobby.execute(ctx.getSource()))
                        .build(),
                "Return to the MissileWarsX lobby",
                List.of()
        );
        commands.register(
                Commands.literal("leave")
                        .executes(ctx -> lobby.execute(ctx.getSource()))
                        .build(),
                "Return to the MissileWarsX lobby",
                List.of()
        );
        commands.register(
                Commands.literal("end")
                        .requires(src -> src.getSender().hasPermission(PERMISSION))
                        .executes(ctx -> end.execute(ctx.getSource()))
                        .build(),
                "End the current MissileWarsX match",
                List.of()
        );
        commands.register(
                Commands.literal("wipe")
                        .requires(src -> src.getSender().hasPermission(PERMISSION))
                        .executes(ctx -> wipe.execute(ctx.getSource()))
                        .build(),
                "Wipe the current MissileWarsX map",
                List.of()
        );
        commands.register(
                give.aliasRoot("mwi")
                        .requires(src -> src.getSender().hasPermission(PERMISSION))
                        .build(),
                "MissileWarsX give shortcut",
                List.of()
        );
    }
}
