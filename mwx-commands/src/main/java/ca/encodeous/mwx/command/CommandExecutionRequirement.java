package ca.encodeous.mwx.command;

import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.function.Predicate;

public enum CommandExecutionRequirement implements Predicate<CommandSender> {
    NONE(s->true),
    ENTITY(s->s instanceof Entity),
    PLAYER(s->s instanceof Player),
    COMMAND_BLOCK(s->s instanceof BlockCommandSender),
    CONSOLE(s->s instanceof ConsoleCommandSender);
    private Predicate<CommandSender> pred;
    CommandExecutionRequirement(Predicate<CommandSender> pred){
        this.pred = pred;
    }

    @Override
    public boolean test(CommandSender commandSender) {
        return pred.test(commandSender);
    }
}
