package ca.encodeous.mwx.core.utils;

import org.bukkit.Bukkit;

public enum MCVersion{
    v1_8(1), v1_9(2), v1_10(3), v1_11(4), v1_12(5), v1_13(6), v1_14(7), v1_15(8), v1_16(9), v1_17(10), v1_18(11), v1_19(12);
    private final int value;
    MCVersion(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
    public static MCVersion QueryVersion(){
        String[] s = Bukkit.getBukkitVersion().split("[.-]");
        return MCVersion.valueOf("v"+s[0]+"_"+s[1]);
    }
    public static boolean IsPaper(){
        boolean isPaper = false;
        try {
            Class.forName("com.destroystokyo.paper.ParticleBuilder");
            isPaper = true;
        } catch (ClassNotFoundException ignored) {
        }
        return isPaper;
    }
}
