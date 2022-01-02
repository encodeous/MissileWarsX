package ca.encodeous.mwx;

import ca.encodeous.mwx.command.CommandCore;
import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.mwx.core.game.MissileWarsImplementation;
import com.karuslabs.commons.command.dispatcher.Dispatcher;
import com.karuslabs.commons.command.tree.nodes.Argument;
import com.karuslabs.commons.command.tree.nodes.Literal;
import com.karuslabs.commons.command.types.PlayerType;
import com.karuslabs.commons.command.types.PlayersType;
import com.karuslabs.commons.command.types.PointType;
import com.karuslabs.commons.util.Point;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.lucko.commodore.Commodore;
import me.lucko.commodore.CommodoreProvider;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Logger;

public final class MissileWarsX extends JavaPlugin {
    public static MissileWarsX Instance;
    public MissileWarsImplementation mwImpl;
    public CoreGame MissileWars;
    public CommandCore Commands;
    private Logger logger = null;

    public static class CommandContext {
        private final Object context;
        public CommandContext(Object context) {
            this.context = context;
        }
        public int GetInteger(String name) {
            return IntegerArgumentType.getInteger((com.mojang.brigadier.context.CommandContext<?>) context, name);
        }
        public double GetDouble(String name) {
            return DoubleArgumentType.getDouble((com.mojang.brigadier.context.CommandContext<?>) context, name);
        }
    }

    public interface Command {
        int execute(CommandContext context);
    }

    public static class CommandSubCommand {
        protected final ArgumentBuilder<?, ?> command;
        public CommandSubCommand(ArgumentBuilder<?, ?> command) {
            this.command = command;
        }
        public CommandSubCommand SubCommand(CommandSubCommand subCommand) {
            command.then((ArgumentBuilder) subCommand.command);
            return this;
        }
        public CommandSubCommand Executes(Command cmd) {
            command.executes((context) -> cmd.execute(new CommandContext(context)));
            return this;
        }
        public static CommandSubCommand Literal(String literal) {
            return new CommandSubCommand(LiteralArgumentBuilder.literal(literal));
        }
        public static CommandSubCommand Integer(String name) {
            return new CommandSubCommand(RequiredArgumentBuilder.argument(name, IntegerArgumentType.integer()));
        }
        public static CommandSubCommand Integer(String name, int minimum) {
            return new CommandSubCommand(RequiredArgumentBuilder.argument(name, IntegerArgumentType.integer(minimum)));
        }
        public static CommandSubCommand Integer(String name, int minimum, int maximum) {
            return new CommandSubCommand(RequiredArgumentBuilder.argument(name, IntegerArgumentType.integer(minimum, maximum)));
        }
        public static CommandSubCommand Double(String name) {
            return new CommandSubCommand(RequiredArgumentBuilder.argument(name, DoubleArgumentType.doubleArg()));
        }
        public static CommandSubCommand Double(String name, double minimum) {
            return new CommandSubCommand(RequiredArgumentBuilder.argument(name, DoubleArgumentType.doubleArg(minimum)));
        }
        public static CommandSubCommand Double(String name, double minimum, double maximum) {
            return new CommandSubCommand(RequiredArgumentBuilder.argument(name, DoubleArgumentType.doubleArg(minimum, maximum)));
        }
    }

    public static final class CommandGen extends CommandSubCommand {
        public CommandGen(String name, Predicate<Object> usageRequirement) {
            super(LiteralArgumentBuilder.literal(name).requires(usageRequirement));
        }
        public void Register(CommandDispatcher<Object> dispatcher) {dispatcher.register((LiteralArgumentBuilder<Object>) command);}
        public CommandGen SubCommand(CommandSubCommand subCommand) {
            super.SubCommand(subCommand);
            return this;
        }
        public static boolean PermissionLevel(Object source, int level) {
            return true;
//            return Reflection.invokeMethod(Reflection.getMethod(source.getClass(), "hasPermission", Integer.class), level);
        }
        public static boolean HighestPermissionLevel(Object source) {return PermissionLevel(source, 4);}
        public static boolean FunctionPermissionLevel(Object source) {return PermissionLevel(source, 2);}
        public static boolean AnyPermissionLevel(Object source) {return PermissionLevel(source, 0);}
    }

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


        new CommandGen("testcommand", CommandGen::AnyPermissionLevel)
                .SubCommand(CommandSubCommand.Literal("int")
                        .SubCommand(CommandSubCommand.Integer("num", 0, 10).Executes((context) -> {
                            Bukkit.broadcastMessage("Integer: " + context.GetInteger("num"));
                            return 1;
                        })))
                .SubCommand(CommandSubCommand.Literal("double")
                        .SubCommand(CommandSubCommand.Double("num", 0, 10).Executes((context) -> {
                            Bukkit.broadcastMessage("Double: " + context.GetDouble("num"));
                            return 1;
                        }))
                )
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
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        //MissileWars.StopGame(true);
    }
}
