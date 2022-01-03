package ca.encodeous.mwx;

<<<<<<< HEAD
import ca.encodeous.mwx.commands.*;
import ca.encodeous.mwx.mwxcompat1_13.MissileWars1_13;
import ca.encodeous.mwx.mwxcompat1_8.MissileWars1_8;
import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.mwx.core.utils.MCVersion;
import ca.encodeous.mwx.core.game.MissileWarsImplementation;
import com.keenant.tabbed.skin.SkinFetcher;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.LogManager;
=======
import ca.encodeous.mwx.command.Command;
import ca.encodeous.mwx.command.CommandCore;
import ca.encodeous.mwx.command.RootCommand;
import ca.encodeous.mwx.command.CommandSubCommand;
import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.mwx.core.game.MissileWarsImplementation;
import com.karuslabs.commons.command.dispatcher.Dispatcher;
import com.karuslabs.commons.command.tree.nodes.Argument;
import com.karuslabs.commons.command.tree.nodes.Literal;
import com.karuslabs.commons.command.types.PlayerType;
import com.karuslabs.commons.command.types.PointType;
import com.mojang.brigadier.CommandDispatcher;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

>>>>>>> command-test
import java.util.logging.Logger;

public final class MissileWarsX extends JavaPlugin {
    public static MissileWarsX Instance;
    public MissileWarsImplementation mwImpl;
    public CoreGame MissileWars;
<<<<<<< HEAD
    private Logger logger = null;
    @Override
    public void onEnable() {
        // Plugin startup logic
        LogManager.getLogManager().getLogger("").setFilter(new ConsoleFilter());
        Bukkit.getServer().getPluginManager().registerEvents(new MiscEventHandler(), this);

        Map<MCVersion, Class<?>> implementations = new HashMap<MCVersion, Class<?>>();
        implementations.put(MCVersion.v1_8, MissileWars1_8.class);
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
            ModernStart();
            ResumeDeferredStartup(this);
        }
    }

    public void ResumeDeferredStartup(JavaPlugin resourcePlugin){
        MissileWars = new CoreGame(mwImpl, this, resourcePlugin);
        MissileWars.InitializeGame();
        LobbyEngine.Fetcher = new SkinFetcher(this);
    }

    private void ModernStart(){
        getLogger().info("Registering commands...");
        getCommand("mwmake").setExecutor(new mwmakeCommand());
        getCommand("mwpaste").setExecutor(new mwpasteCommand());
        getCommand("mwlaunch").setExecutor(new mwlaunchCommand());
        getCommand("mwedit").setExecutor(new mweditCommand());
        getCommand("mwreload").setExecutor(new mwreloadCommand());
        getCommand("mwgive").setExecutor(new mwgiveCommand());
        getCommand("reset").setExecutor(new mwresetCommand());
        getCommand("players").setExecutor(new playersCommand());
        getCommand("ready").setExecutor(new readyCommand());
        getCommand("wipe").setExecutor(new mwwipeCommand());
        getCommand("mwitems").setExecutor(new mwitemsCommand());
        getCommand("mwmissiles").setExecutor(new mwmissilesCommand());
        getCommand("start").setExecutor(new mwstartCommand());
        getCommand("spectate").setExecutor(new spectateCommand());
        getCommand("mwteam").setExecutor(new mwteamCommand());
        getCommand("lobby").setExecutor(new lobbyCommand());
        getCommand("ping").setExecutor(new pingCommand());
        getCommand("mwfireball").setExecutor(new mwfireballCommand());
        getCommand("mode").setExecutor(new modeCommand());
=======
    public CommandCore Commands;
    private Logger logger = null;

    @Override
    public void onEnable() {
        // Plugin startup logic
//        LogManager.getLogManager().getLogger("").setFilter(new ConsoleFilter());
//        Bukkit.getServer().getPluginManager().registerEvents(new MiscEventHandler(), this);
//
//        Map<MCVersion, Class<?>> implementations = new HashMap<MCVersion, Class<?>>();
//        implementations.put(MCVersion.v1_8, MissileWars1_8.class);
//        implementations.put(MCVersion.v1_13, MissileWars1_13.class);
//        Instance = this;
//        logger = Bukkit.getLogger();
//        logger.info("Starting MissileWarsX...");
//        logger.info("Getting version compatibility adapter");
//
//        MCVersion version = MCVersion.QueryVersion();
//        MCVersion newestVersion = null;
//        boolean found = false;
//        if(version.getValue() < MCVersion.v1_13.getValue()){
//            // legacy
//            for(Map.Entry<MCVersion, Class<?>> impl : implementations.entrySet()){
//                if((!found || newestVersion.getValue() < impl.getKey().getValue()) && impl.getKey().getValue() < MCVersion.v1_13.getValue()){
//                    found = true;
//                    newestVersion = impl.getKey();
//                }
//            }
//        }else{
//            for(Map.Entry<MCVersion, Class<?>> impl : implementations.entrySet()){
//                if((!found || newestVersion.getValue() < impl.getKey().getValue()) && impl.getKey().getValue() >= MCVersion.v1_13.getValue()){
//                    found = true;
//                    newestVersion = impl.getKey();
//                }
//            }
//        }
//
//        if(!found){
//            logger.severe("No suitable version adapter found for "+version.toString()+"! MissileWarsX cannot continue executing!");
//            throw new RuntimeException();
//        }
//
//        logger.info("Found version adapter " + newestVersion + " for version " + version.toString());
//        try {
//            mwImpl = (MissileWarsImplementation) implementations.get(newestVersion).newInstance();
//        } catch (InstantiationException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        }
//
//        MissileWars = new CoreGame(mwImpl, this);
//
//        MissileWars.InitializeGame();
//
//        LobbyEngine.Fetcher = new SkinFetcher(this);

//        Commands = new CommandCore(this);
//        logger.info("Registering commands...");
//        Commands.RegisterAllCommands();


        Object dedicatedServer = Reflection.invokeMethod("getServer", Bukkit.getServer());
        Class<?> minecraftServer = dedicatedServer.getClass().getSuperclass();
        Object vanillaCommandDispatcher = Reflection.get(minecraftServer, "vanillaCommandDispatcher", dedicatedServer);
        CommandDispatcher<Object> commandDispatcher = Reflection.get("g", vanillaCommandDispatcher);


        new RootCommand("testcommand", Command::AnyPermissionLevel)
                .SubCommand(CommandSubCommand.Literal("int")
                        .SubCommand(CommandSubCommand.Integer("num", 0, 10).Executes((context) -> {
                            Bukkit.broadcastMessage("Integer: " + context.GetInteger("num"));
                            return 1;
                        })))
                .SubCommand(CommandSubCommand.Literal("double")
                        .SubCommand(CommandSubCommand.Double("num", 0, 10).Executes((context) -> {
                            Bukkit.broadcastMessage("Double: " + context.GetDouble("num"));
                            return 1;
                        })))
                .SubCommand(CommandSubCommand.Literal("message").Executes((context) -> {
                            context.SendMessage(context.GetEntity().getName() + " sent this command.");
                            return 1;
                        }))

                .Register(commandDispatcher);

        Argument.Builder<CommandSender, Player> player = Argument.of("player", new PlayerType());
        Argument.Builder<CommandSender, ?> pt = Argument.of("pos", PointType.CUBIC.mapped());

        Argument<CommandSender, Player> arguments = player.then(pt).build();

//        LiteralCommandNode<Object> testCmd = LiteralArgumentBuilder.literal("asdf")
//                .then(RequiredArgumentBuilder.argument("player", new PlayerType())
//                    .then(RequiredArgumentBuilder.argument("pos", PointType.CUBIC.mapped())
//                        .executes(ctx -> {
//                            Bukkit.broadcastMessage("test");
//                            Player p = ctx.getArgument("player", Player.class);
//                            Object point = ctx.getArgument("pos", Object.class);
//                            double x = Reflection.get("a", point);
//                            double y = Reflection.get("b", point);
//                            double z = Reflection.get("c", point);
//                            p.teleport(new Location(p.getWorld(), x, y, z));
//                            p.sendMessage("You have been teleported to " + point);
//                            return 1;
//                        }))).build();

        Literal<CommandSender> testCmd2 = Literal.of("asdf")
                .then(
                        Argument.of("player", new PlayerType())
                                .then(
                                        Argument.of("pos", PointType.CUBIC.mapped())
                                                .executes(ctx->{
                                                    Bukkit.broadcastMessage("test");
                                                    Player p = ctx.getArgument("player", Player.class);
                                                    Object point = ctx.getArgument("pos", Object.class);
                                                    double x = Reflection.get("e", Reflection.get("a", point));
                                                    double y = Reflection.get("e", Reflection.get("b", point));
                                                    double z = Reflection.get("e", Reflection.get("c", point));
                                                    p.teleport(new Location(p.getWorld(), x, y, z, p.getLocation().getYaw(), p.getLocation().getPitch()));
                                                    p.sendMessage("You have been teleported to " + x + " " + y + " " + z);
                                                    return 1;
                                                })
                                )
                )
                .build();

        Dispatcher dispatcher = Dispatcher.of(this);
        dispatcher.getRoot().addChild(testCmd2);

//        Commodore commodore = CommodoreProvider.getCommodore(this);

//        PluginCommand pc = getCommand("lobby");
//        commandDispatcher.register(LiteralArgumentBuilder.literal("lobby")
//                .then(RequiredArgumentBuilder.argument("some-argument", StringArgumentType.string()))
//                .then(RequiredArgumentBuilder.argument("some-other-argument", BoolArgumentType.bool())));
//        pc.setExecutor(new TestCommand());
////        commodore.register(pc, lab.build(), player -
////            return true;
////        });
>>>>>>> command-test
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        MissileWars.StopGame(true);
    }
}
