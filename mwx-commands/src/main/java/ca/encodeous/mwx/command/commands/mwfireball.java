package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.Command;
import ca.encodeous.mwx.command.MissileWarsCommand;
import ca.encodeous.mwx.command.RootCommand;
import ca.encodeous.mwx.core.game.MissileWarsMatch;
import ca.encodeous.mwx.core.utils.Chat;
import ca.encodeous.mwx.data.Ref;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static ca.encodeous.mwx.command.CommandSubCommand.Position3d;

public class mwfireball extends MissileWarsCommand {
    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        try{
            MissileWarsMatch match = null;
            if(sender instanceof BlockCommandSender){
                BlockCommandSender s = (BlockCommandSender) sender;
                match = LobbyEngine.FromWorld(s.getBlock().getWorld());
            }
            Player p = null;
            if(sender instanceof Player){
                match = LobbyEngine.FromPlayer((Player) sender);
                p = (Player) sender;
            }
            if(match != null){
                int x = (int)Double.parseDouble(args[0]), y = (int)Double.parseDouble(args[1]), z = (int)Double.parseDouble(args[2]);
                match.DeployFireball(match.Map.MswWorld.getBlockAt(x, y, z), new Ref<>(false), new Ref<>(false), p);
                sender.sendMessage(Chat.FCL("&aFireball summoned at the specified location."));
            }else{
                sender.sendMessage(Chat.FCL("&cYou cannot summon a fireball"));
            }
            return true;
        }catch (Exception e){
            return false;
        }
    }

    @Override
    public RootCommand BuildCommand() {
        return new RootCommand("mwfireball", Command::FunctionPermissionLevel, "mwf")
                .SubCommand(Position3d("location").Executes((context) -> {
                    MissileWarsMatch match = LobbyEngine.FromPlayer(context.GetPlayer());
                    if(match == null) {
                        context.SendMessage("&cYou cannot summon a fireball");
                        return 0;
                    }
                    Location loc = context.GetPosition("location");
                    match.DeployFireball(loc.getBlock(), new Ref<>(false), new Ref<>(false), context.GetPlayer());
                    context.SendMessage(Chat.FCL("&aFireball summoned at the specified location."));
                    return 1;
                }));
    }

    @Override
    public String GetCommandName() {
        return "mwfireball";
    }
}
