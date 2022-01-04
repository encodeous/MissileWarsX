package ca.encodeous.mwx.command;

import ca.encodeous.mwx.core.utils.Reflection;
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



        PluginCommand pCmd = plugin.getServer().getPluginCommand(cmd.GetCommandName());

        Object dedicatedServer = Reflection.invokeMethod("getServer", Bukkit.getServer());
        Class<?> minecraftServer = dedicatedServer.getClass().getSuperclass();
        Object vanillaCommandDispatcher = Reflection.get(minecraftServer, "vanillaCommandDispatcher", dedicatedServer);
        if(vanillaCommandDispatcher != null) {
            CommandDispatcher<Object> commandDispatcher = Reflection.get("g", vanillaCommandDispatcher);
            if(commandDispatcher != null) {
                try {
                    cmd.BuildCommand().Register(commandDispatcher);
                }catch(RuntimeException e) {}
            }else {
                pCmd.setExecutor(cmd);
            }
        }else {
            pCmd.setExecutor(cmd);
        }

        return true;
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
    }

    public Set<Class<? extends MissileWarsCommand>> getClasses() {
        Reflections reflections = new Reflections(
                "ca.encodeous.mwx.command.commands", new SubTypesScanner());
        return reflections.getSubTypesOf(MissileWarsCommand.class);
    }
}
