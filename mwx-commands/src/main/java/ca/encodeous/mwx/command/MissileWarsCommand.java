package ca.encodeous.mwx.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.Plugin;

public abstract class MissileWarsCommand implements CommandExecutor {

    public abstract void BuildCommandAutocomplete(LiteralArgumentBuilder<?> builder);
    public abstract String GetCommandName();
}
