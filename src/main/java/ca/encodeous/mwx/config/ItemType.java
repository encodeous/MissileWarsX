package ca.encodeous.mwx.config;

/**
 * Defines the special function of an item in MissileWars.
 * <ul>
 *   <li>{@link #SHIELD}   – Launches a snowball that deploys a shield schematic after 1 second
 *                           (if the snowball has not hit a block).</li>
 *   <li>{@link #FIREBALL} – Places a stationary fireball at the targeted ground location so
 *                           the player can punch it.</li>
 *   <li>{@link #ITEM}     – A standard item (no special placement logic).</li>
 *   <li>{@link #MISSILE}  – Deploys the structure schematic linked via {@code schematicId}.</li>
 *   <li>{@link #GUNBLADE} – A hybrid ranged/melee weapon item.</li>
 * </ul>
 */
public enum ItemType {
    SHIELD,
    FIREBALL,
    ITEM,
    MISSILE,
    GUNBLADE
}
