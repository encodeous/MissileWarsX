package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.*;
import ca.encodeous.mwx.configuration.Missile;
import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.mwx.core.game.MissileWarsMatch;
import ca.encodeous.mwx.core.utils.Chat;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Map.Entry;

import static ca.encodeous.mwx.command.CommandSubCommand.*;

public class mwpaste extends MissileWarsCommand {
    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        try{
            if(sender instanceof Player){
                Player p = (Player) sender;
                Vector v = new Vector(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
                if(CoreGame.Instance.mwMissiles.containsKey(args[3])){
                    Missile mws = CoreGame.Instance.mwMissiles.get(args[3]);
                    boolean result = CoreGame.GetStructureManager().PlaceMissile(mws, v, p.getWorld(), args[4].equals("red"), false, p);
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

    private int spawn(CommandContext context, Missile missile, boolean red) throws CommandSyntaxException {
        boolean result = CoreGame.GetStructureManager().PlaceMissile(missile, context.GetPosition("pivot").toVector(), context.GetPlayer().getWorld(), red, false, context.GetPlayer());
        if(!result){
            MissileWarsMatch.SendCannotPlaceMessage(context.GetPlayer());
            return 0;
        }
        return 1;
    }

    private CommandSubCommand addMissiles(CommandSubCommand cmd) {
        for(Entry<String, Missile> missileEntry : CoreGame.Instance.mwMissiles.entrySet()) {
            cmd.SubCommand(Literal(missileEntry.getKey())
                    .SubCommand(Literal("red").Executes(context -> spawn(context, missileEntry.getValue(), true)))
                    .SubCommand(Literal("green").Executes(context -> spawn(context, missileEntry.getValue(), false)))
            );
        }
        return cmd;
    }

    @Override
    public RootCommand BuildCommand() {
        return new RootCommand("mwpaste", Command::FunctionPermissionLevel)
                .SubCommand(addMissiles(Position3d("pivot")));
    }

    @Override
    public String GetCommandName() {
        return "mwpaste";
    }
}
