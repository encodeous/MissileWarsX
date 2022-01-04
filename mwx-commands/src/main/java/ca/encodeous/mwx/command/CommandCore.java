package ca.encodeous.mwx.command;

import ca.encodeous.mwx.command.commands.LobbyCommands;
import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.mwx.core.utils.Reflection;
import ca.encodeous.mwx.engines.command.CommandBase;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.IntegerArgument;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.io.IOException;
import java.util.*;

public abstract class CommandCore extends CommandBase {
    public static CommandCore Instance;
    private Plugin plugin;
    public CommandCore(){
        Instance = this;
    }
    @Override
    public void Initialize() {
        plugin = CoreGame.Instance.mwPlugin;
        plugin.getLogger().info("Detected modern minecraft, registering Brigadier command completions...");
        RegisterAllCommands();
        RegisterLobbies();
    }
    public void RegisterAllCommands(){
        int cnt = 0;
        for(Class<?> clazz : getClasses()){
            try{
                if(RegisterCommand(clazz)){
                    cnt++;
                }
            }catch(Exception e){
                e.printStackTrace();
                plugin.getLogger().severe("Error while loading command " + clazz.getName());
            }
        }
        plugin.getLogger().info("Registered " + cnt + " MissileWars commands.");
    }

    @Override
    public void RegisterLobbies(){
        plugin.getLogger().info("Registering lobby commands...");
        int i = 1;
        for(var lobby : CoreGame.Instance.mwLobbies.Lobbies) {
            int lid = i++;
            new CommandRegister("mw"+lid, "Switches to missile wars lobby " + lid, false).Create(e ->
                    e.executesPlayer((p, args) -> {
                        LobbyCommands.SendPlayerTo(p, lid);
                    })
            );
        }
    }
    public boolean RegisterCommand(Class<?> missileWarsCommand){
        MissileWarsCommand cmd = (MissileWarsCommand) Reflection.newInstance(
                Objects.requireNonNull(Reflection.getConstructor(missileWarsCommand)));
        cmd.BuildCommand(this);
        return true;
    }
    public Set<Class<? extends MissileWarsCommand>> getClasses() {
        Reflections reflections = new Reflections(
                "ca.encodeous.mwx.command.commands", new SubTypesScanner());
        return reflections.getSubTypesOf(MissileWarsCommand.class);
    }

    private HashSet<CommandRegister> registeredCommands = new HashSet<>();

    public void AddInfo(CommandRegister reg){
        registeredCommands.add(reg);
    }

    @Override
    public void UpdatePlayer(Player p){
        CommandAPI.updateRequirements(p);
    }

    @Override
    public void Disable(){
        for(var reg : registeredCommands){
            CommandAPI.unregister(reg.Name);
        }
    }

    public abstract CommandAPICommand GetCommand(String name);
    public abstract void CreateCommand(CommandAPICommand command);

    public CommandPermission GetMwxAdminPermission(){
        return CommandPermission.fromString("mwx.admin");
    }
}
