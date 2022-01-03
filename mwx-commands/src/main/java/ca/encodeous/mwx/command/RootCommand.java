package ca.encodeous.mwx.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import java.util.function.Predicate;

public final class RootCommand extends CommandSubCommand {

    public RootCommand(String name, Predicate<Object> usageRequirement) {
        super(LiteralArgumentBuilder.literal(name).requires(usageRequirement));
    }

    public void Register(CommandDispatcher<Object> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder<Object>) command);
    }

    public RootCommand SubCommand(CommandSubCommand subCommand) {
        super.SubCommand(subCommand);
        return this;
    }
}
