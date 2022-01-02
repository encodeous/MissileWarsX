package ca.encodeous.mwx.commands;

import ca.encodeous.mwx.configuration.Missile;
import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.mwx.core.game.MissileWarsMatch;
import ca.encodeous.mwx.core.utils.Chat;
import ca.encodeous.mwx.data.Ref;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;
import org.bukkit.World;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class mwfireballCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
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
}
