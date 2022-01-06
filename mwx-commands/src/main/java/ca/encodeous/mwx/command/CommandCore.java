package ca.encodeous.mwx.command;

import ca.encodeous.mwx.command.commands.LobbyCommand;
import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.mwx.core.utils.Reflection;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.util.*;

public class CommandCore {

    private Plugin plugin;

    public CommandCore(Plugin registrantPlugin){
        plugin = registrantPlugin;
        plugin.getLogger().info("Detected modern minecraft, registering Brigadier command completions...");
    }

    public boolean RegisterCommand(Class<?> missileWarsCommand){
        MissileWarsCommand cmd = (MissileWarsCommand) Reflection.newInstance(
                Objects.requireNonNull(Reflection.getConstructor(missileWarsCommand)));

        cmd.BuildCommand().Register(GetDispatcher());

        return true;
    }

    public CommandDispatcher<Object> GetDispatcher(){
        Object dedicatedServer = Reflection.invokeMethod("getServer", Bukkit.getServer());
        Class<?> minecraftServer = dedicatedServer.getClass().getSuperclass();
        var dispatch = Reflection.get(minecraftServer, "vanillaCommandDispatcher", dedicatedServer);
        if(dispatch != null) {
            CommandDispatcher<Object> commandDispatcher = Reflection.get("g", dispatch);
            if(commandDispatcher != null) {
                return commandDispatcher;
            }else {
                throw new RuntimeException("Could not get access to the Brigadier Command Dispatcher");
            }
        }else {
            throw new RuntimeException("Vanilla Command Dispatcher not found");
        }

    }

    public void RegisterAllCommands(){
        int cnt = 0;
        for(Class<?> clazz : getClasses()){
            try{
                if(RegisterCommand(clazz)){
                    cnt++;
                }
            }catch(NullPointerException e){
                e.printStackTrace();
                plugin.getLogger().severe("Error while loading command " + clazz.getName());
            }
        }
        plugin.getLogger().info("Registered " + cnt + " MissileWars commands.");
        RegisterLobbies();
    }

    public void RegisterLobbies(){
        plugin.getLogger().info("Registering lobby commands...");
        int i = 1;
        for(var lobby : CoreGame.Instance.mwLobbies.Lobbies) {
            int lid = i++;
            var cmd = new RootCommand("mw" + lid, Command::DefaultPlayerCommand)
                    .Executes(ExecutionSource.PLAYER, context->{
                        LobbyEngine.FromPlayer(context.GetSendingPlayer()).lobby.RemovePlayer(context.GetSendingPlayer());
                        LobbyEngine.GetLobby(lid).AddPlayer(context.GetSendingPlayer());
                        context.SendMessage("&9You have been teleported to lobby " + lid + ".");
                        return 1;
                    });
            cmd.Register(GetDispatcher());
        }
    }

    public Set<Class<? extends MissileWarsCommand>> getClasses() {
        Reflections reflections = new Reflections(
                "ca.encodeous.mwx.command.commands", new SubTypesScanner());
        return reflections.getSubTypesOf(MissileWarsCommand.class);
    }
}
