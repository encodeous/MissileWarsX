package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.CommandCore;
import ca.encodeous.mwx.command.CommandRegister;
import ca.encodeous.mwx.command.MissileWarsCommand;
import ca.encodeous.mwx.configuration.Missile;
import ca.encodeous.mwx.configuration.MissileWarsItem;
import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.mwx.core.game.MissileWarsMatch;
import ca.encodeous.mwx.core.lang.Strings;
import ca.encodeous.mwx.core.utils.Chat;
import ca.encodeous.mwx.core.utils.Utils;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.LocationType;
import dev.jorel.commandapi.arguments.StringArgument;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PasteCommand extends MissileWarsCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try{
            if(sender instanceof Player){
                Player p = (Player) sender;
                Vector v = new Vector(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));

                return true;
            }
        }catch (Exception e){
            return false;
        }
        return false;
    }

    public boolean PasteMissile(CommandSender cs, Vector pos, String missile, boolean isRed, boolean update, World world){
        if(CoreGame.Instance.mwMissiles.containsKey(missile)){
            Missile mws = CoreGame.Instance.mwMissiles.get(missile);
            Player p = (cs instanceof Player pl) ? pl : null;
            boolean result = CoreGame.GetStructureManager().PlaceMissile(mws, pos, world, isRed, update, p);
            if(!result){
                MissileWarsMatch.SendCannotPlaceMessage(cs);
            }
            return true;
        }else{
            return false;
        }
    }

    @Override
    public void BuildCommand(CommandCore core) {
        new CommandRegister("mwpaste", "Pastes a missile schematic", true).Create(e ->
                e.withArguments(new LocationArgument("anchor", LocationType.BLOCK_POSITION),
                                new StringArgument("missileName").replaceSuggestions(r->
                                        CoreGame.Instance.mwMissiles.values().stream().map(x->
                                                x.MissileItemId).toArray(String[]::new)),
                                new StringArgument("facing").replaceSuggestions(x->new String[]{
                                        "fromRed",
                                        "fromGreen"
                                }),
                                new BooleanArgument("updateBlocks"))
                        .executesNative((p, args) -> {
                            var anchor = (Location) args[0];
                            var name = (String) args[1];
                            var facing = (String) args[2];
                            var update = (Boolean) args[3];
                            if(!facing.equals("fromRed") && !facing.equals("fromGreen")){
                                CommandAPI.fail("The specified facing is invalid");
                                return;
                            }
                            boolean isRed = facing.equals("fromRed");
                            PasteMissile(p, anchor.toVector(), name, isRed, update, p.getWorld());
                        }));
    }
}
