package ca.encodeous.mwx.commands.subcommands;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;

import ca.encodeous.mwx.MwPlugin;
import ca.encodeous.mwx.commands.MwxSuggestions;
import ca.encodeous.mwx.util.Msg;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class GiveSubcommand {
    private final MwPlugin plugin;

    public GiveSubcommand(final MwPlugin plugin) {
        this.plugin = plugin;
    }

    private static void giveStacked(final Player player, final ItemStack base, final int amount) {
        int remaining = amount;
        final int max = Math.max(1, base.getMaxStackSize());
        while (remaining > 0) {
            final int stackAmount = Math.min(max, remaining);
            final ItemStack stack = base.clone();
            stack.setAmount(stackAmount);
            player.getInventory().addItem(stack);
            remaining -= stackAmount;
        }
    }

    public int execute(final CommandSourceStack source, final List<Player> targets, final String itemChoice, final int amount) {
        final CommandSender sender = source.getSender();
        final var config = plugin.getGame().getConfig();
        final String choice = itemChoice.toLowerCase();

        if (targets.isEmpty()) {
            sender.sendMessage(Msg.component("&cNo target players found."));
            return Command.SINGLE_SUCCESS;
        }

        if (choice.equals("all")) {
            for (final Player player : targets) {
                for (final var item : config.getDistributableItems()) {
                    giveStacked(player, item.createItemStack(plugin), amount);
                }
            }
            sender.sendMessage(Msg.component("&aGave all standard items &7(x" + amount + ") &ato &e" + targets.size() + "&a player(s)."));
            return Command.SINGLE_SUCCESS;
        }

        final var item = config.getItem(choice);
        if (item == null) {
            sender.sendMessage(Msg.component("&cUnknown item: " + itemChoice));
            return Command.SINGLE_SUCCESS;
        }

        for (final Player player : targets) {
            giveStacked(player, item.createItemStack(plugin), amount);
        }
        sender.sendMessage(Msg.component("&aGave &e" + item.displayName + " &7(x" + amount + ") &ato &e" + targets.size() + "&a player(s)."));
        return Command.SINGLE_SUCCESS;
    }

    public LiteralArgumentBuilder<CommandSourceStack> node() {
        return buildRoot("give");
    }

    /**
     * Root alias form, e.g. "/mwi <item|all>".
     */
    public LiteralArgumentBuilder<CommandSourceStack> aliasRoot(final String literal) {
        return buildRoot(literal);
    }

    private LiteralArgumentBuilder<CommandSourceStack> buildRoot(final String literal) {
        // /<literal> <item> [amount] : self target (only works if executor is a player)
        final var selfItem = Commands.argument("item", word())
                .suggests((ctx, builder) -> MwxSuggestions.suggestGiveOptions(plugin, builder))
                .executes(ctx -> {
                    final CommandSender sender = ctx.getSource().getSender();
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage(Msg.component("&cFrom console/command blocks, use: /" + literal + " <targets> <item|all> [amount]"));
                        return Command.SINGLE_SUCCESS;
                    }
                    return execute(ctx.getSource(), List.of(player), getString(ctx, "item"), 1);
                });
        selfItem.then(Commands.argument("amount", IntegerArgumentType.integer(1)).executes(ctx -> {
            final CommandSender sender = ctx.getSource().getSender();
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Msg.component("&cFrom console/command blocks, use: /" + literal + " <targets> <item|all> [amount]"));
                return Command.SINGLE_SUCCESS;
            }
            return execute(ctx.getSource(), List.of(player), getString(ctx, "item"), IntegerArgumentType.getInteger(ctx, "amount"));
        }));

        // /<literal> <targets> <item> [amount]
        final var targeted = Commands.argument("targets", ArgumentTypes.players())
                .then(Commands.argument("item", word())
                        .suggests((ctx, builder) -> MwxSuggestions.suggestGiveOptions(plugin, builder))
                        .executes(ctx -> {
                            try {
                                final PlayerSelectorArgumentResolver resolver = ctx.getArgument("targets", PlayerSelectorArgumentResolver.class);
                                return execute(ctx.getSource(), resolver.resolve(ctx.getSource()), getString(ctx, "item"), 1);
                            } catch (final CommandSyntaxException e) {
                                ctx.getSource().getSender().sendMessage(Msg.component("&cInvalid targets: " + e.getMessage()));
                                return Command.SINGLE_SUCCESS;
                            }
                        })
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1)).executes(ctx -> {
                            try {
                                final PlayerSelectorArgumentResolver resolver = ctx.getArgument("targets", PlayerSelectorArgumentResolver.class);
                                return execute(
                                        ctx.getSource(),
                                        resolver.resolve(ctx.getSource()),
                                        getString(ctx, "item"),
                                        IntegerArgumentType.getInteger(ctx, "amount")
                                );
                            } catch (final CommandSyntaxException e) {
                                ctx.getSource().getSender().sendMessage(Msg.component("&cInvalid targets: " + e.getMessage()));
                                return Command.SINGLE_SUCCESS;
                            }
                        })));

        return Commands.literal(literal)
                .then(selfItem)
                .then(targeted);
    }
}
