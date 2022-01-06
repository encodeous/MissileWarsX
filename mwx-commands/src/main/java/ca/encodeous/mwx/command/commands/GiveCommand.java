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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static ca.encodeous.mwx.command.CommandNode.*;
import static ca.encodeous.mwx.command.ExecutionSource.ANY;
import static ca.encodeous.mwx.command.ExecutionSource.PLAYER;

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

    private CommandNode AddItem(MissileWarsItem item, boolean hasTarget){
        String name = item == null ? "all" : item.MissileWarsItemId;
        if(hasTarget){
            return Literal(name)
                    .SubCommand(Integer("count", 1, 2304)
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
        }else{
            return Literal(name)
                    .SubCommand(Integer("count", 1, 2304)
                            .Executes(PLAYER, (context) -> {
                                var match = LobbyEngine.FromWorld(context.GetSendingWorld());
                                CompleteGive(match, Arrays.asList(context.GetSendingPlayer()), item, context.GetInteger("count"));
                                return 1;
                            }))
                    .Executes(PLAYER, (context) -> {
                        var match = LobbyEngine.FromWorld(context.GetSendingWorld());
                        CompleteGive(match, Arrays.asList(context.GetSendingPlayer()), item, 1);
                        return 1;
                    });
        }
    }

    private void AddMissiles(CommandNode prevArg, boolean hasTarget){
        for(MissileWarsItem item : CoreGame.Instance.mwConfig.Items){
            if(item.IsExempt) continue;
            prevArg.SubCommand(AddItem(item, hasTarget));
        }
        prevArg.SubCommand(AddItem(null, hasTarget));
    }

    @Override
    public RootCommand BuildCommand() {
        var rootCmd = new RootCommand("mwgive", Command::DefaultRestrictedCommand, "mwi");
        AddMissiles(rootCmd, false);
        var prevCmd = PlayerMultiple("targets");
        AddMissiles(prevCmd, true);
        return rootCmd
                .SubCommand(prevCmd);
    }

    @Override
    public String GetCommandName() {
        return "mwgive";
    }
}
