package ca.encodeous.mwx.command;

import ca.encodeous.mwx.mwxcompat1_8.Reflection;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.lucko.commodore.Commodore;
import me.lucko.commodore.CommodoreProvider;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.io.IOException;
import java.util.*;

public class CommandCore {
    private Plugin plugin;
    private Commodore commodore;
    public CommandCore(Plugin registrantPlugin){
        plugin = registrantPlugin;
        commodore = CommodoreProvider.getCommodore(plugin);
        plugin.getLogger().info("Detected modern minecraft, registering Brigadier command completions...");
    }
    public boolean RegisterCommand(Class<?> missileWarsCommand){
        MissileWarsCommand cmd = (MissileWarsCommand) Reflection.newInstance(
                Objects.requireNonNull(Reflection.getConstructor(missileWarsCommand)));


        PluginCommand pCmd = plugin.getServer().getPluginCommand(cmd.GetCommandName());

        if(CommodoreProvider.isSupported()){
            LiteralArgumentBuilder<?> lab = LiteralArgumentBuilder.literal(cmd.GetCommandName());
            cmd.BuildCommandAutocomplete(lab);
            commodore.register(pCmd, lab.build());
        }
        pCmd.setExecutor(cmd);

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
