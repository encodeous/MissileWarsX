package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.CommandCore;
import ca.encodeous.mwx.command.CommandRegister;
import ca.encodeous.mwx.command.MissileWarsCommand;
import ca.encodeous.mwx.configuration.MissileWarsItem;
import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.mwx.core.game.MissileWarsMatch;
import ca.encodeous.mwx.core.utils.Chat;
import ca.encodeous.mwx.core.utils.Utils;
import ca.encodeous.mwx.engines.lobby.LobbyEngine;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class GiveCommand extends MissileWarsCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try{
            Player p = null;
            boolean successMsg;
            boolean hasCount = false;
            int aidx = 0;
            if(sender instanceof Player pl){
                if(!Utils.CheckPrivPermission(pl)) return true;
            }
            if(args.length == 1){
                if(sender instanceof Player) p = (Player) sender;
                else{
                    sender.sendMessage("You are not a player...");
                    return true;
                }
                successMsg = false;
            }else{
                p = Bukkit.getPlayer(args[0]);
                if(p == null){
                    sender.sendMessage("Player not found!");
                    return true;
                }
                successMsg = true;
                aidx = 1;
            }
            if(aidx + 1 != args.length) hasCount = true;
            int count = 1;
            if(hasCount){
                try{
                    count = Integer.parseInt(args[aidx + 1]);
                }catch (Exception e){
                    sender.sendMessage("Please specify a valid stack size");
                    return true;
                }
            }

            if(args[aidx].equals("*")){
                for(MissileWarsItem item : CoreGame.GetImpl().CreateDefaultItems()){
                    if(item.IsExempt) continue;
                    ItemStack ritem = CoreGame.GetImpl().CreateItem(item);
                    ritem.setAmount(count);
                    p.getInventory().addItem(ritem);
                }
                p.sendMessage(Chat.FCL("&6You have been given all the items"));
                if(successMsg){
                    sender.sendMessage("Success!");
                }
            }else{
                MissileWarsItem item = CoreGame.Instance.GetItemById(args[aidx]);
                if(item == null){
                    sender.sendMessage("Item ID not found! See items by running /mwitems");
                    return true;
                }
                ItemStack ritem = CoreGame.GetImpl().CreateItem(item);
                ritem.setAmount(count);
                p.getInventory().addItem(ritem);
                p.sendMessage(Chat.FCL("&6You have been given &c" + item.MissileWarsItemId + "&6!"));
                if(successMsg){
                    sender.sendMessage("Success!");
                }
            }
            return true;
        }catch (Exception e){
            return false;
        }
    }

    private void CompleteGive(MissileWarsMatch sourceMatch, Collection<Entity> entities, String requestedItem ) throws WrapperCommandSyntaxException {
        MissileWarsItem item = null;
        if(!requestedItem.equals("all")){
            item = CoreGame.Instance.GetItemById(requestedItem);
            if(item == null){
                CommandAPI.fail("Item Id not found! See items by running /mwitems");
                return;
            }
        }
        for(Entity entity : entities){
            if(entity instanceof Player pl){
                MissileWarsMatch match2 = LobbyEngine.FromPlayer(pl);
                if(sourceMatch != match2) continue;
                if(item == null){
                    for(MissileWarsItem citem : CoreGame.GetImpl().CreateDefaultItems()){
                        if(citem.IsExempt) continue;
                        ItemStack ritem = CoreGame.GetImpl().CreateItem(citem);
                        pl.getInventory().addItem(ritem);
                    }
                    pl.sendMessage(Chat.FCL("&6You have been given all the items"));
                }else{

                    ItemStack ritem = CoreGame.GetImpl().CreateItem(item);
                    pl.getInventory().addItem(ritem);
                    pl.sendMessage(Chat.FCL("&6You have been given a &c" + item.MissileWarsItemId + "&6!"));
                }
            }
        }
    }

    @Override
    public void BuildCommand(CommandCore core) {
        new CommandRegister("mwgive", "Gives MissileWars Items", true).Create(e ->
                e.withAliases("mwi", "i")
                        .withArguments(new EntitySelectorArgument("target", EntitySelectorArgument.EntitySelector.MANY_PLAYERS),
                                new StringArgument("itemId").replaceSuggestions(r->{
                                    ArrayList<String> suggestions = new ArrayList<>();
                                    suggestions.add("all");
                                    for(MissileWarsItem item : CoreGame.GetImpl().CreateDefaultItems()){
                                        if(item.IsExempt) continue;
                                        suggestions.add(item.MissileWarsItemId);
                                    }
                                    return suggestions.toArray(new String[0]);
                                }))
                        .executesPlayer((sender, args) -> {
                            MissileWarsMatch match = LobbyEngine.FromPlayer(sender);
                            CompleteGive(match, (Collection<Entity>) args[0], (String) args[1]);
                        })
                        .executesCommandBlock((sender, args) -> {
                            MissileWarsMatch match = LobbyEngine.FromWorld(sender.getBlock().getWorld());
                            CompleteGive(match, (Collection<Entity>) args[0], (String) args[1]);
                        })
        );
    }
}
