package ca.encodeous.mwx.command;

import ca.encodeous.mwx.command.nms.ArgumentEntity;
import ca.encodeous.mwx.command.nms.ArgumentPosition;
import ca.encodeous.simplenms.proxy.NMSCore;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import org.bukkit.command.CommandSender;

import java.util.function.Predicate;

import static ca.encodeous.mwx.command.ExecutionSource.PLAYER;

public class CommandSubCommand {
    
    protected final ArgumentBuilder<?, ?> command;
    
    public CommandSubCommand(ArgumentBuilder<?, ?> command) {
        this.command = command;
    }

    public CommandSubCommand SubCommand(CommandSubCommand subCommand) {
        command.then((ArgumentBuilder) subCommand.command);
        return this;
    }

    public CommandSubCommand Executes(Predicate<CommandSender> requirement, Command cmd) {
        command.executes((context) -> cmd.Execute(new CommandContext(context, requirement)));
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
    public static CommandSubCommand Boolean(String name) {
        return new CommandSubCommand(RequiredArgumentBuilder.argument(name, BoolArgumentType.bool()));
    }
    public static CommandSubCommand PlayerSingle(String name) {
        try {
            return new CommandSubCommand(RequiredArgumentBuilder.argument(name, (ArgumentType<?>) NMSCore.constructNMSObject(ArgumentEntity.class, true, true).getProxyHandle()));
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static CommandSubCommand PlayerMultiple(String name) {
        try {
            return new CommandSubCommand(RequiredArgumentBuilder.argument(name, (ArgumentType<?>) NMSCore.constructNMSObject(ArgumentEntity.class, false, true).getProxyHandle()));
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static CommandSubCommand EntitySingle(String name) {
        try {
            return new CommandSubCommand(RequiredArgumentBuilder.argument(name, (ArgumentType<?>) NMSCore.constructNMSObject(ArgumentEntity.class, true, false).getProxyHandle()));
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static CommandSubCommand EntityMultiple(String name) {
        try {
            return new CommandSubCommand(RequiredArgumentBuilder.argument(name, (ArgumentType<?>) NMSCore.constructNMSObject(ArgumentEntity.class, false, false).getProxyHandle()));
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static CommandSubCommand Position3d(String name) {
        try {
            return new CommandSubCommand(RequiredArgumentBuilder.argument(name, (ArgumentType<?>) NMSCore.constructNMSObject(ArgumentPosition.class).getProxyHandle()));
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        return null;
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
