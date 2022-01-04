package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.*;
import ca.encodeous.mwx.configuration.MissileWarsItem;
import ca.encodeous.mwx.core.game.CoreGame;
import ca.encodeous.mwx.core.utils.Chat;
import ca.encodeous.mwx.core.utils.Utils;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static ca.encodeous.mwx.command.CommandExecutionRequirement.NONE;
import static ca.encodeous.mwx.command.CommandSubCommand.Literal;
import static ca.encodeous.mwx.command.CommandSubCommand.Integer;
import static ca.encodeous.mwx.command.CommandSubCommand.PlayerSingle;

public class mwgive extends MissileWarsCommand {
    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        try{
            Player p = null;
            boolean successMsg;
            boolean hasCount = false;
            int aidx = 0;
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
            if(!Utils.CheckPrivPermission(p)) return true;
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

    private int runCommand(CommandContext context, MissileWarsItem item, boolean defCount) throws CommandSyntaxException {
        Player player = context.GetPlayer("selector");
        int count = defCount ? 1 : context.GetInteger("count");
        if(!Utils.CheckPrivPermission(player)) return 0;
        if(item == null) { // item being null means that all items will be given to the player
            for(MissileWarsItem givenItem : CoreGame.GetImpl().CreateDefaultItems()){
                if(givenItem.IsExempt) continue;
                ItemStack ritem = CoreGame.GetImpl().CreateItem(givenItem);
                ritem.setAmount(count);
                player.getInventory().addItem(ritem);
            }
            player.sendMessage(Chat.FCL("&6You have been given all the items"));
            context.SendMessage("&aSuccess!");
        }else {
            ItemStack ritem = CoreGame.GetImpl().CreateItem(item);
            ritem.setAmount(count);
            player.getInventory().addItem(ritem);
            context.SendMessage("&6You have been given &c" + item.MissileWarsItemId + "&6!");
        }
        return count;
    }

    private CommandSubCommand addMissiles(CommandSubCommand cmd) {
        for(MissileWarsItem items : CoreGame.Instance.mwConfig.Items) {
            if(!items.IsExempt)
                cmd.SubCommand(Literal(items.MissileWarsItemId).SubCommand(Integer("count", 1, 64).Executes(NONE, (context) -> runCommand(context, items, false))).Executes(NONE, (context) -> runCommand(context, items, true)));
        }
        cmd.SubCommand(Literal("*").SubCommand(Integer("count", 1, 64).Executes(NONE, (context) -> runCommand(context, null, false))).Executes(NONE, (context) -> runCommand(context, null, true)));
        return cmd;
    }

    @Override
    public RootCommand BuildCommand() {
        return new RootCommand("mwgive", Command::FunctionPermissionLevel, "mwi")
                .SubCommand(addMissiles(PlayerSingle("selector")));
    }

    @Override
    public String GetCommandName() {
        return "mwgive";
    }
}
