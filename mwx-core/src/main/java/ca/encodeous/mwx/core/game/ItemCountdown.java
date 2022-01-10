package ca.encodeous.mwx.core.game;

import ca.encodeous.mwx.configuration.MissileWarsItem;
import ca.encodeous.mwx.core.lang.Strings;
import ca.encodeous.mwx.core.utils.Countable;
import ca.encodeous.mwx.core.utils.Utils;
import ca.encodeous.mwx.data.SoundType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemCountdown implements Countable {
    public ItemCountdown(MissileWarsMatch match) {
        Match = match;
    }

    public MissileWarsMatch Match;
    @Override
    public void Count(Counter counter, int count) {
        if(count % CoreGame.Instance.mwConfig.ResupplySeconds == 0){
            while(true){
                int item = Match.mwRand.nextInt(CoreGame.Instance.mwConfig.Items.size());
                MissileWarsItem mwItem = CoreGame.Instance.mwConfig.Items.get(item);
                if(mwItem.IsExempt) continue;
                for(Player p : Match.Green){
                    GivePlayerItem(p, CoreGame.Instance.mwConfig.Items.get(item));
                }
                for(Player p : Match.Red){
                    GivePlayerItem(p, CoreGame.Instance.mwConfig.Items.get(item));
                }
                break;
            }
        }
        int remTime = CoreGame.Instance.mwConfig.ResupplySeconds - (count % CoreGame.Instance.mwConfig.ResupplySeconds);
        for(Player p : Match.Green){
            p.setLevel(remTime);
            p.setExp(remTime / ((float)CoreGame.Instance.mwConfig.ResupplySeconds));
        }
        for(Player p : Match.Red){
            p.setLevel(remTime);
            p.setExp(remTime / ((float)CoreGame.Instance.mwConfig.ResupplySeconds));
        }
    }

    public void GivePlayerItem(Player p, MissileWarsItem item){
        int curCnt = Utils.CountItem(p, item);
        boolean DisableItemLimit = Match.settingsManager.getBooleanSetting("DisableItemLimit").getValue();
        if(item.MaxStackSize > curCnt || DisableItemLimit){
            ItemStack citem = CoreGame.GetImpl().CreateItem(item);

            if(!DisableItemLimit) citem.setAmount(Math.min(item.StackSize, item.MaxStackSize - curCnt));
            else citem.setAmount(item.StackSize);

            if(!p.getInventory().addItem(citem).isEmpty()){
                CoreGame.GetImpl().PlaySound(p, SoundType.ITEM_NOT_GIVEN);
                CoreGame.GetImpl().SendActionBar(p, Strings.INVENTORY_FULL);
            }else{
                CoreGame.GetImpl().PlaySound(p, SoundType.ITEM_GIVEN);
            }
        }else{
            CoreGame.GetImpl().PlaySound(p, SoundType.ITEM_NOT_GIVEN);
            CoreGame.GetImpl().SendActionBar(p, String.format(Strings.ITEM_NOT_GIVEN, item.MissileWarsItemId));
        }
    }

    @Override
    public void FinishedCount(Counter counter) { }
}
