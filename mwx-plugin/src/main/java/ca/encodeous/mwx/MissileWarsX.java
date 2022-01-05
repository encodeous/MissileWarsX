package ca.encodeous.mwx;

import ca.encodeous.mwx.core.utils.Reflection;
import ca.encodeous.mwx.mwxcompat1_13.MissileWars1_13;
import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.mwx.core.utils.MCVersion;
import ca.encodeous.mwx.core.game.MissileWarsImplementation;
import com.keenant.tabbed.skin.SkinFetcher;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.LogManager;

import ca.encodeous.mwx.command.CommandCore;
import ca.encodeous.mwx.command.RootCommand;
import ca.encodeous.mwx.command.CommandSubCommand;
import com.mojang.brigadier.CommandDispatcher;

import java.util.logging.Logger;

import static ca.encodeous.mwx.command.ExecutionSource.*;

public final class MissileWarsX extends JavaPlugin {
    public static MissileWarsX Instance;
    public MissileWarsImplementation mwImpl;
    public CoreGame MissileWars;
    public CommandCore Commands;
    private Logger logger = null;
    @Override
    public void onEnable() {
        // Plugin startup logic
        LogManager.getLogManager().getLogger("").setFilter(new ConsoleFilter());
        Bukkit.getServer().getPluginManager().registerEvents(new MiscEventHandler(), this);

        Map<MCVersion, Class<?>> implementations = new HashMap<>();
        implementations.put(MCVersion.v1_13, MissileWars1_13.class);
        Instance = this;
        logger = Bukkit.getLogger();
        logger.info("Starting MissileWarsX...");
        logger.info("Getting version compatibility adapter");

        MCVersion version = MCVersion.QueryVersion();
        MCVersion newestVersion = null;
        boolean found = false;
        if(version.getValue() < MCVersion.v1_13.getValue()){
            // legacy
            for(Map.Entry<MCVersion, Class<?>> impl : implementations.entrySet()){
                if((!found || newestVersion.getValue() < impl.getKey().getValue()) && impl.getKey().getValue() < MCVersion.v1_13.getValue()){
                    found = true;
                    newestVersion = impl.getKey();
                }
            }
        }else{
            for(Map.Entry<MCVersion, Class<?>> impl : implementations.entrySet()){
                if((!found || newestVersion.getValue() < impl.getKey().getValue()) && impl.getKey().getValue() >= MCVersion.v1_13.getValue()){
                    found = true;
                    newestVersion = impl.getKey();
                }
            }
        }

        if(!found){
            logger.severe("No suitable version adapter found for "+version+"! MissileWarsX cannot continue executing!");
            throw new RuntimeException();
        }

        logger.info("Found version adapter " + newestVersion + " for version " + version.toString());
        try {
            mwImpl = (MissileWarsImplementation) implementations.get(newestVersion).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        if(version.getValue() < MCVersion.v1_13.getValue()){
            logger.info("You are running MissileWarsX on legacy Minecraft, you will be missing out on many features that only exist on the latest version of Minecraft.");
            logger.info("Startup will be deferred. If startup has not resumed in a few moments, please install MissileWarsX-Compatibility if you have not already.");
        }else{
            ResumeDeferredStartup(this);
            ModernStart();
        }
    }

    public void ResumeDeferredStartup(JavaPlugin resourcePlugin){
        MissileWars = new CoreGame(mwImpl, this, resourcePlugin);
        MissileWars.InitializeGame();
        LobbyEngine.Fetcher = new SkinFetcher(this);
    }

    private void ModernStart() {
        getLogger().info("Registering commands...");
        Object dedicatedServer = Reflection.invokeMethod("getServer", Bukkit.getServer());
        Class<?> minecraftServer = dedicatedServer.getClass().getSuperclass();
        Object vanillaCommandDispatcher = Reflection.get(minecraftServer, "vanillaCommandDispatcher", dedicatedServer);
        CommandDispatcher<Object> commandDispatcher = Reflection.get("g", vanillaCommandDispatcher);

        new RootCommand("testcommand", "tc")
                .SubCommand(CommandSubCommand.Literal("int")
                        .SubCommand(CommandSubCommand.Integer("num", 0, 10).Executes(NONE, (context) -> {
                            Bukkit.broadcastMessage("Integer: " + context.GetInteger("num"));
                            return 1;
                        })))
                .SubCommand(CommandSubCommand.Literal("double")
                        .SubCommand(CommandSubCommand.Double("num", 0, 10).Executes(NONE, (context) -> {
                            Bukkit.broadcastMessage("Double: " + context.GetDouble("num"));
                            return 1;
                        })))
                .SubCommand(CommandSubCommand.Literal("message")
                        .SubCommand(CommandSubCommand.Literal("player").Executes(PLAYER, (context) -> {
                            context.SendMessage(context.GetSenderName() + " sent this command (Player).");
                            return 1;
                        }))
                        .SubCommand(CommandSubCommand.Literal("entity").Executes(ENTITY, (context) -> {
                            context.SendMessage(context.GetSenderName() + " sent this command (Entity).");
                            return 1;
                        }))
                        .SubCommand(CommandSubCommand.Literal("console").Executes(NONE, (context) -> {
                            context.SendMessage(context.GetSenderName() + " sent this command (CommandSender).");
                            return 1;
                        })))
                .SubCommand(CommandSubCommand.Literal("selector")
                        .SubCommand(CommandSubCommand.Literal("player")
                                .SubCommand(CommandSubCommand.Literal("single")
                                        .SubCommand(CommandSubCommand.PlayerSingle("selector").Executes(NONE, context -> {
                                            context.SendMessage(Objects.toString(context.GetPlayer("selector")));
                                            return 1;
                                        })))
                                .SubCommand(CommandSubCommand.Literal("multiple")
                                        .SubCommand(CommandSubCommand.PlayerMultiple("selector").Executes(NONE, context -> {
                                            context.SendMessage(Objects.toString(context.GetPlayers("selector")));
                                            return 1;
                                        }))))
                        .SubCommand(CommandSubCommand.Literal("entity")
                                .SubCommand(CommandSubCommand.Literal("single")
                                        .SubCommand(CommandSubCommand.EntitySingle("selector").Executes(NONE, context -> {
                                            context.SendMessage(Objects.toString(context.GetEntity("selector")));
                                            return 1;
                                        })))
                                .SubCommand(CommandSubCommand.Literal("multiple")
                                        .SubCommand(CommandSubCommand.EntityMultiple("selector").Executes(NONE, context -> {
                                            context.SendMessage(Objects.toString(context.GetEntities("selector")));
                                            return 1;
                                        })))))
                .SubCommand(CommandSubCommand.Literal("coordinates")
                        .SubCommand(CommandSubCommand.Literal("vec3")
                                .SubCommand(CommandSubCommand.Position3d("position").Executes(PLAYER, context -> {
                                    context.SendMessage(context.GetPosition("position").toString());
                                    return 1;
                                }))))
                .SubCommand(CommandSubCommand.Literal("string")
                        .SubCommand(CommandSubCommand.Literal("string")
                                .SubCommand(CommandSubCommand.String("string").Executes(PLAYER, context -> {
                                    context.SendMessage(context.GetString("string"));
                                    return 1;
                                })))
                        .SubCommand(CommandSubCommand.Literal("greedy").
                                SubCommand(CommandSubCommand.GreedyString("string").Executes(PLAYER, context -> {
                                    context.SendMessage(context.GetString("string"));
                                    return 1;
                                })))
                        .SubCommand(CommandSubCommand.Literal("word").
                                SubCommand(CommandSubCommand.Word("string").Executes(PLAYER, context -> {
                                    context.SendMessage(context.GetString("string"));
                                    return 1;
                                }))))

                .Register(commandDispatcher);

        Commands = new CommandCore(this);
        Commands.RegisterAllCommands();
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
        MissileWars.StopGame(true);
    }
}
