package ca.encodeous.mwx.command;

import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.function.Predicate;

public enum ExecutionSource implements Predicate<CommandSender> {
    ANY(s->true),
    ENTITY(s->s instanceof Entity),
    PLAYER(s->s instanceof Player),
    COMMAND_BLOCK(s->s instanceof BlockCommandSender),
    CONSOLE(s->s instanceof ConsoleCommandSender),
    HAS_WORLD(ENTITY.or(PLAYER).or(COMMAND_BLOCK));
    private Predicate<CommandSender> pred;
    ExecutionSource(Predicate<CommandSender> pred){
        this.pred = pred;
    }

    @Override
    public boolean test(CommandSender commandSender) {
        return pred.test(commandSender);
    }
}
