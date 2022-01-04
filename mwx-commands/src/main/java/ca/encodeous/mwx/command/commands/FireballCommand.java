package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.CommandCore;
import ca.encodeous.mwx.command.CommandRegister;
import ca.encodeous.mwx.command.MissileWarsCommand;
import ca.encodeous.mwx.core.game.MissileWarsMatch;
import ca.encodeous.mwx.core.utils.Chat;
import ca.encodeous.mwx.core.utils.Utils;
import ca.encodeous.mwx.data.Ref;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.LocationType;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FireballCommand extends MissileWarsCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try{
            int x = (int)Double.parseDouble(args[0]), y = (int)Double.parseDouble(args[1]), z = (int)Double.parseDouble(args[2]);
            SummonFireball(sender, x, y, z);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public void SummonFireball(CommandSender sender, int x, int y, int z){
        MissileWarsMatch match = null;
        if(sender instanceof BlockCommandSender s){
            match = LobbyEngine.FromWorld(s.getBlock().getWorld());
        }
        Player p = null;
        if(sender instanceof Player){
            match = LobbyEngine.FromPlayer((Player) sender);
            p = (Player) sender;
        }
        if(match != null){
            match.DeployFireball(match.Map.MswWorld.getBlockAt(x, y, z), new Ref<>(false), new Ref<>(false), p);
            sender.sendMessage(Chat.FCL("&aFireball summoned at the specified location."));
        }else{
            sender.sendMessage(Chat.FCL("&cYou cannot summon a fireball"));
        }
    }

    @Override
    public void BuildCommand(CommandCore core) {
        new CommandRegister("mwfireball", "Summons a stationary fireball", true).Create(e ->
                e.withAliases("fireball", "fb")
                        .withArguments(new LocationArgument("location", LocationType.BLOCK_POSITION))
                        .executes((s, args) -> {
                            Location loc = (Location) args[0];
                            SummonFireball(s, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
                        })
        );
    }
}
