package ca.encodeous.mwx.util;

import ca.encodeous.mwx.game.MwMatch;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.Collection;

/**
 * Utility class for common messages and chat formatting.
 */
public class Msg {

    public static final String JOIN_MESSAGE = "&7Welcome to &6MissileWarsX&7!";
    public static final String TEAM_FULL = "&cThat team is full!";
    public static final String GAME_FULL = "&cThe game is full!";
    public static final String STARTING_GAME = "&aGame starting in &e%d &asecond!";
    public static final String STARTING_GAME_PLURAL = "&aGame starting in &e%d &aseconds!";
    public static final String GAME_STARTED = "&aThe game has started!";
    public static final String GAME_RESET = "&eThe map is being reset...";
    public static final String PORTAL_BROKEN = "%s&e's portal has been broken by &e%s&e!";
    public static final String PORTAL_BROKEN_UNKNOWN = "%s&e's portal has been broken!";
    public static final String TEAM_WIN_TITLE = "&a&lYou Win";
    public static final String TEAM_WIN_SUBTITLE = "&fCongrats!";
    public static final String TEAM_LOSE_TITLE = "&c&lYou Lose :(";
    public static final String TEAM_LOSE_SUBTITLE = "&fBetter luck next time!";
    public static final String DRAW_TITLE = "&7&lIt's a Draw!";
    public static final String DRAW_SUBTITLE = "&fVery Close Game!";
    public static final String DRAW = "&eThe game is a draw!";
    public static final String INVENTORY_FULL = "&cYour inventory is full!";
    public static final String ITEM_CAPPED = "&cYou already have a %s&c!";
    public static final String ITEM_CAPPED_VOWEL = "&cYou already have an %s&c!";
    public static final String DEPLOY_FAILED = "&cYou cannot deploy here!";
    public static final String DRAW_CHECK = "&6Waiting &e%s &6seconds for a draw.";
    public static final String NO_GAME_RUNNING = "&cNo game is currently running.";

    /**
     * Translate '&' color codes in a string.
     */
    public static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    /**
     * Convert a legacy-formatted string to an Adventure Component.
     */
    public static Component component(String s) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(s);
    }

    /**
     * Send an action bar message to a player.
     */
    public static void actionBar(Player p, String msg) {
        p.sendActionBar(component(msg));
    }

    /**
     * Send a title + subtitle to a player.
     */
    public static void title(Player p, String title, String subtitle) {
        Title t = Title.title(
                component(title),
                component(subtitle),
                Title.Times.times(
                        Duration.ofMillis(200),
                        Duration.ofSeconds(2),
                        Duration.ofMillis(500)
                )
        );
        p.showTitle(t);
    }

    public static String formatNames(MwMatch match, String separator, Collection<Player> players) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Player p : players) {
            sb.append(match.getTeam(p).getColor())
                    .append(p.getName());
            if(i != players.size() - 1) {
                sb.append(separator);
            }
            i++;
        }
        return sb.toString();
    }

    /**
     * Broadcast a color-formatted message to a collection of players.
     */
    public static void broadcast(Collection<Player> players, String msg) {
        Component c = component(msg);
        for (Player p : players) {
            p.sendMessage(c);
        }
    }

    /**
     * Send a color-formatted message to one player.
     */
    public static void send(Player p, String msg) {
        p.sendMessage(component(msg));
    }
}
