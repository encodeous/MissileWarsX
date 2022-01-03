package ca.encodeous.mwx.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

public class CommandSubCommand {
    
    protected final ArgumentBuilder<?, ?> command;
    
    public CommandSubCommand(ArgumentBuilder<?, ?> command) {
        this.command = command;
    }
    public CommandSubCommand SubCommand(CommandSubCommand subCommand) {
        command.then((ArgumentBuilder) subCommand.command);
        return this;
    }
    public CommandSubCommand Executes(Command cmd) {
        command.executes((context) -> cmd.Execute(new CommandContext(context)));
        return this;
    }
    public static CommandSubCommand Literal(String literal) {
        return new CommandSubCommand(LiteralArgumentBuilder.literal(literal));
    }
    public static CommandSubCommand Integer(String name) {
        return new CommandSubCommand(RequiredArgumentBuilder.argument(name, IntegerArgumentType.integer()));
    }
    public static CommandSubCommand Integer(String name, int minimum) {
        return new CommandSubCommand(RequiredArgumentBuilder.argument(name, IntegerArgumentType.integer(minimum)));
    }
    public static CommandSubCommand Integer(String name, int minimum, int maximum) {
        return new CommandSubCommand(RequiredArgumentBuilder.argument(name, IntegerArgumentType.integer(minimum, maximum)));
    }
    public static CommandSubCommand Double(String name) {
        return new CommandSubCommand(RequiredArgumentBuilder.argument(name, DoubleArgumentType.doubleArg()));
    }
    public static CommandSubCommand Double(String name, double minimum) {
        return new CommandSubCommand(RequiredArgumentBuilder.argument(name, DoubleArgumentType.doubleArg(minimum)));
    }
    public static CommandSubCommand Double(String name, double minimum, double maximum) {
        return new CommandSubCommand(RequiredArgumentBuilder.argument(name, DoubleArgumentType.doubleArg(minimum, maximum)));
    }

}
