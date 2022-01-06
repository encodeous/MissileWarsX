package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.*;
import ca.encodeous.mwx.configuration.MissileWarsItem;
import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.mwx.core.game.MissileWarsMatch;
import ca.encodeous.mwx.core.utils.Chat;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

import static ca.encodeous.mwx.command.CommandNode.*;
import static ca.encodeous.mwx.command.ExecutionSource.ANY;

public class GiveCommand extends MissileWarsCommand {
    private void CompleteGive(MissileWarsMatch sourceMatch, Collection<Entity> entities, MissileWarsItem item, int count){
        for(Entity entity : entities){
            if(entity instanceof Player pl){
                MissileWarsMatch match2 = LobbyEngine.FromPlayer(pl);
                if(sourceMatch != match2) continue;
                if(item == null){
                    for(MissileWarsItem citem : CoreGame.GetImpl().CreateDefaultItems()){
                        if(citem.IsExempt) continue;
                        ItemStack ritem = CoreGame.GetImpl().CreateItem(citem);
                        ritem.setAmount(count);
                        pl.getInventory().addItem(ritem);
                    }
                    pl.sendMessage(Chat.FCL("&6You have been given all the items"));
                }else{

                    ItemStack ritem = CoreGame.GetImpl().CreateItem(item);
                    ritem.setAmount(count);
                    pl.getInventory().addItem(ritem);
                    pl.sendMessage(Chat.FCL("&6You have been given "+ count + " &c" + item.MissileWarsItemId + "&6!"));
                }
            }
        }
    }

    private CommandNode AddItem(MissileWarsItem item){
        String name = item == null ? "all" : item.MissileWarsItemId;
        return Literal(name)
                .SubCommand(Integer("count", 1, 64)
                        .Executes(ANY, (context) -> {
                            var match = LobbyEngine.FromWorld(context.GetSendingWorld());
                            CompleteGive(match, context.GetEntities("targets"), item, context.GetInteger("count"));
                            return 1;
                        }))
                .Executes(ANY, (context) -> {
                    var match = LobbyEngine.FromWorld(context.GetSendingWorld());
                    CompleteGive(match, context.GetEntities("targets"), item, 1);
                    return 1;
                });
    }

    private void AddMissiles(CommandNode prevArg){
        for(MissileWarsItem item : CoreGame.Instance.mwConfig.Items){
            if(item.IsExempt) continue;
            prevArg.SubCommand(AddItem(item));
        }
        prevArg.SubCommand(AddItem(null));
    }

    @Override
    public RootCommand BuildCommand() {
        var prevCmd = PlayerMultiple("targets");
        AddMissiles(prevCmd);
        return new RootCommand("mwgive", Command::DefaultRestrictedCommand, "mwi")
                .SubCommand(prevCmd);
    }

    @Override
    public String GetCommandName() {
        return "mwgive";
    }
}
