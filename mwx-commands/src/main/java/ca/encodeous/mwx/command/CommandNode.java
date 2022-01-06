package ca.encodeous.mwx.command;

import ca.encodeous.mwx.command.nms.ArgumentEntity;
import ca.encodeous.mwx.command.nms.ArgumentPosition;
import ca.encodeous.mwx.command.nms.CommandListenerWrapper;
import ca.encodeous.simplenms.proxy.NMSCore;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import org.bukkit.command.CommandSender;

import java.util.function.Predicate;

import static ca.encodeous.mwx.command.ExecutionSource.PLAYER;

public class CommandNode {
    
    protected final ArgumentBuilder<?, ?> command;
    
    public CommandNode(ArgumentBuilder<?, ?> command) {
        this.command = command;
    }

    public CommandNode SubCommand(CommandNode subCommand) {
        command.then((ArgumentBuilder) subCommand.command);
        return this;
    }

    public CommandNode Executes(Predicate<CommandSender> requirement, Command cmd) {
        command.executes((context) -> cmd.Execute(new CommandContext(context, requirement)));
        return this;
    }

    public CommandNode Executes(Command cmd) {
        return Executes(PLAYER, cmd);
    }

    public CommandNode Requires(Predicate<CommandListenerWrapper> usageRequirement){
        command.requires((o) ->
                usageRequirement.test(NMSCore.getNMSObject(CommandListenerWrapper.class, o)));
        return this;
    }

    public static CommandNode Literal(String literal) {
        return new CommandNode(LiteralArgumentBuilder.literal(literal));
    }
    public static CommandNode Integer(String name) {
        return new CommandNode(RequiredArgumentBuilder.argument(name, IntegerArgumentType.integer()));
    }
    public static CommandNode Integer(String name, int minimum) {
        return new CommandNode(RequiredArgumentBuilder.argument(name, IntegerArgumentType.integer(minimum)));
    }
    public static CommandNode Integer(String name, int minimum, int maximum) {
        return new CommandNode(RequiredArgumentBuilder.argument(name, IntegerArgumentType.integer(minimum, maximum)));
    }
    public static CommandNode Double(String name) {
        return new CommandNode(RequiredArgumentBuilder.argument(name, DoubleArgumentType.doubleArg()));
    }
    public static CommandNode Double(String name, double minimum) {
        return new CommandNode(RequiredArgumentBuilder.argument(name, DoubleArgumentType.doubleArg(minimum)));
    }
    public static CommandNode Double(String name, double minimum, double maximum) {
        return new CommandNode(RequiredArgumentBuilder.argument(name, DoubleArgumentType.doubleArg(minimum, maximum)));
    }
    public static CommandNode Boolean(String name) {
        return new CommandNode(RequiredArgumentBuilder.argument(name, BoolArgumentType.bool()));
    }
    public static CommandNode PlayerSingle(String name) {
        try {
            return new CommandNode(RequiredArgumentBuilder.argument(name, (ArgumentType<?>) NMSCore.constructNMSObject(ArgumentEntity.class, true, true).getProxyHandle()));
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("CommandNode.PlayerSingle(String) failed");
    }
    public static CommandNode PlayerMultiple(String name) {
        try {
            return new CommandNode(RequiredArgumentBuilder.argument(name, (ArgumentType<?>) NMSCore.constructNMSObject(ArgumentEntity.class, false, true).getProxyHandle()));
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("CommandNode.PlayerMultiple(String) failed");
    }
    public static CommandNode EntitySingle(String name) {
        try {
            return new CommandNode(RequiredArgumentBuilder.argument(name, (ArgumentType<?>) NMSCore.constructNMSObject(ArgumentEntity.class, true, false).getProxyHandle()));
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("CommandNode.EntitySingle(String) failed");
    }
    public static CommandNode EntityMultiple(String name) {
        try {
            return new CommandNode(RequiredArgumentBuilder.argument(name, (ArgumentType<?>) NMSCore.constructNMSObject(ArgumentEntity.class, false, false).getProxyHandle()));
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("CommandNode.EntityMultiple(String) failed");
    }
    public static CommandNode Position3d(String name) {
        try {
            return new CommandNode(RequiredArgumentBuilder.argument(name, (ArgumentType<?>) NMSCore.constructNMSObject(ArgumentPosition.class).getProxyHandle()));
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("CommandNode.Position3d(String) failed");
    }
    public static CommandNode String(String name) {
        return new CommandNode(RequiredArgumentBuilder.argument(name, StringArgumentType.string()));
    }
    public static CommandNode GreedyString(String name) {
        return new CommandNode(RequiredArgumentBuilder.argument(name, StringArgumentType.greedyString()));
    }
    public static CommandNode Word(String name) {
        return new CommandNode(RequiredArgumentBuilder.argument(name, StringArgumentType.word()));
    }

}
