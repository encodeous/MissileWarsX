package ca.encodeous.mwx.game;

import ca.encodeous.mwx.MwPlugin;
import ca.encodeous.mwx.config.ItemConfig;
import ca.encodeous.mwx.config.PluginConfig;
import ca.encodeous.mwx.config.SoundType;
import ca.encodeous.mwx.util.Msg;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Distributes random items to players on a configurable timer.
 */
public class ItemDistributor {

    private final MwMatch match;
    private int tickCount = 0;
    private final SecureRandom rng = new SecureRandom();
    private BukkitTask task;

    public ItemDistributor(MwMatch match) {
        this.match = match;
    }

    /**
     * Begin the distribution task (runs every 20 ticks = 1 second).
     */
    public void start() {
        stop();
        tickCount = 0;
        task = MwPlugin.getInstance().getServer().getScheduler()
                .runTaskTimer(MwPlugin.getInstance(), this::tick, 20L, 20L);
    }

    /**
     * Stop the distribution task.
     */
    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void tick() {
        PluginConfig config = MwPlugin.getInstance().getGame().getConfig();
        int resupplySeconds = config.resupplySeconds;

        int remaining = resupplySeconds - (tickCount % resupplySeconds);

        // Update XP bar for players
        updatePlayerXP(remaining);

        if (tickCount % resupplySeconds == 0) {
            // Give random item to each active player
            ItemConfig item = randomItem();
            for (Player p : match.getRedPlayers()) {
                if (item != null) giveItem(p, item);
            }
            for (Player p : match.getGreenPlayers()) {
                if (item != null) giveItem(p, item);
            }
        }
        tickCount = (tickCount + 1) % resupplySeconds;
    }

    private void giveItem(Player p, ItemConfig itemCfg) {
        // Count how many of this item the player already has
        int count = 0;
        var inv = p.getInventory();
        var allStacks = new ArrayList<>(Arrays.asList(inv.getContents()));
        allStacks.add(p.getItemOnCursor());
        if (p.getOpenInventory().getTopInventory() instanceof CraftingInventory cInv) {
            allStacks.addAll(Arrays.asList(cInv.getContents()));
        }
        for (ItemStack stack : allStacks) {
            if (stack == null) continue;
            String id = ItemConfig.getItemId(MwPlugin.getInstance(), stack);
            if (itemCfg.id.equals(id)) {
                count += stack.getAmount();
            }
        }

        if (count >= itemCfg.maxStack) {
            match.playSound(List.of(p), SoundType.FAIL);
            if ("aeiou".contains(itemCfg.id.substring(0, 1))) {
                Msg.actionBar(p, String.format(Msg.ITEM_CAPPED_VOWEL, itemCfg.displayName));
            } else {
                Msg.actionBar(p, String.format(Msg.ITEM_CAPPED, itemCfg.displayName));
            }
            return;
        }
        match.playSound(List.of(p), SoundType.GIVE_ITEM);

        ItemStack toGive = itemCfg.createItemStack(MwPlugin.getInstance());
        // Cap to remaining space
        int canGive = Math.min(toGive.getAmount(), itemCfg.maxStack - count);
        if (canGive <= 0) return;
        toGive.setAmount(canGive);

        java.util.Map<Integer, ItemStack> leftover = p.getInventory().addItem(toGive);
        if (!leftover.isEmpty()) {
            Msg.send(p, Msg.INVENTORY_FULL);
        }
    }

    private ItemConfig randomItem() {
        List<ItemConfig> items = MwPlugin.getInstance().getGame().getConfig().getDistributableItems();
        if (items.isEmpty()) return null;
        return items.get(rng.nextInt(items.size()));
    }

    public void updatePlayerXP(int remainingTicks) {
        float progress = 1.0f - ((float) remainingTicks / MwPlugin.getInstance().getGame().getConfig().resupplySeconds);
        for (Player p : match.getAllActivePlayers()) {
            p.setLevel(remainingTicks);
            p.setExp(Math.clamp(progress, 0f, 1f));
        }
    }
}
