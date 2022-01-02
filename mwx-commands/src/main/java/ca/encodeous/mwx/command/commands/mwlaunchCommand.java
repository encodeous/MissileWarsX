package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.MissileWarsCommand;
import ca.encodeous.mwx.configuration.Missile;
import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.mwx.core.game.MissileWarsMatch;
import ca.encodeous.mwx.core.utils.Chat;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class mwlaunchCommand extends MissileWarsCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try{
            if(sender instanceof Player){
                Player p = (Player) sender;
                Vector v = new Vector(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
                if(CoreGame.Instance.mwMissiles.containsKey(args[3])){
                    Missile mws = CoreGame.Instance.mwMissiles.get(args[3]);
                    boolean result = CoreGame.GetStructureManager().PlaceMissile(mws, v, p.getWorld(), args[4].equals("red"), true, p);
                    if(!result){
                        MissileWarsMatch.SendCannotPlaceMessage(p);
                    }
                }else{
                    p.sendMessage(Chat.FCL("&cMissile Not Found!"));
                }
                return true;
            }
        }catch (Exception e){
            return false;
        }
        return false;
    }

    @Override
    public void BuildCommandAutocomplete(LiteralArgumentBuilder<?> builder) {
        ArgumentBuilder x = RequiredArgumentBuilder.argument("x", IntegerArgumentType.integer());
        ArgumentBuilder y = RequiredArgumentBuilder.argument("y", IntegerArgumentType.integer());
        ArgumentBuilder z = RequiredArgumentBuilder.argument("z", IntegerArgumentType.integer());
        builder.then(x.then(y).then(z));
        for(String missile : CoreGame.Instance.mwMissiles.keySet()) {
            z.then(LiteralArgumentBuilder.literal(missile));
        }
    }

    @Override
    public String GetCommandName() {
        return "mwlaunch";
    }
}
