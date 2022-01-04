package ca.encodeous.mwx.command;

import ca.encodeous.mwx.core.utils.Reflection;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import static ca.encodeous.mwx.command.Command.ArgumentEntity;
import static ca.encodeous.mwx.command.Command.ArgumentPosition;
import static ca.encodeous.mwx.command.CommandExecutionRequirement.NONE;
import static ca.encodeous.mwx.command.CommandExecutionRequirement.PLAYER;

public class CommandSubCommand {
    
    protected final ArgumentBuilder<?, ?> command;
    
    public CommandSubCommand(ArgumentBuilder<?, ?> command) {
        this.command = command;
    }

    public CommandSubCommand SubCommand(CommandSubCommand subCommand) {
        command.then((ArgumentBuilder) subCommand.command);
        return this;
    }

    public CommandSubCommand Executes(CommandExecutionRequirement requirement, Command cmd) {
        command.executes((context) -> cmd.Execute(new CommandContext(context, requirement != NONE, requirement == PLAYER)));
        return this;
    }

    public CommandSubCommand Executes(Command cmd) {
        return Executes(PLAYER, cmd);
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
    public static CommandSubCommand PlayerSingle(String name) {
        return new CommandSubCommand(RequiredArgumentBuilder.argument(name, (ArgumentType<?>) Reflection.newInstance(Reflection.getConstructor(ArgumentEntity, boolean.class, boolean.class), true, true)));
    }
    public static CommandSubCommand PlayerMutliple(String name) {
        return new CommandSubCommand(RequiredArgumentBuilder.argument(name, (ArgumentType<?>) Reflection.newInstance(Reflection.getConstructor(ArgumentEntity, boolean.class, boolean.class), false, true)));
    }
    public static CommandSubCommand EntitySingle(String name) {
        return new CommandSubCommand(RequiredArgumentBuilder.argument(name, (ArgumentType<?>) Reflection.newInstance(Reflection.getConstructor(ArgumentEntity, boolean.class, boolean.class), true, false)));
    }
    public static CommandSubCommand EntityMutliple(String name) {
        return new CommandSubCommand(RequiredArgumentBuilder.argument(name, (ArgumentType<?>) Reflection.newInstance(Reflection.getConstructor(ArgumentEntity, boolean.class, boolean.class), false, false)));
    }
    public static CommandSubCommand Position3d(String name) {
        return new CommandSubCommand(RequiredArgumentBuilder.argument(name, (ArgumentType<?>) Reflection.invokeMethod(ArgumentPosition, "a", null)));
    }
    public static CommandSubCommand String(String name) {
        return new CommandSubCommand(RequiredArgumentBuilder.argument(name, StringArgumentType.string()));
    }
    public static CommandSubCommand GreedyString(String name) {
        return new CommandSubCommand(RequiredArgumentBuilder.argument(name, StringArgumentType.greedyString()));
    }
    public static CommandSubCommand Word(String name) {
        return new CommandSubCommand(RequiredArgumentBuilder.argument(name, StringArgumentType.word()));
    }

}
