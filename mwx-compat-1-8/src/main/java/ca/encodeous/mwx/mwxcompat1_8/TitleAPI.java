package ca.encodeous.mwx.mwxcompat1_8;

import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TitleAPI {

    private static Class<?> chatSerializer;
    private static Method serializer;
    private static Class<?> chatBaseComponent;
    private static Constructor<?> chatConstructor;
    private static Constructor<?> timeConstructor;
    private static Class<?> titleAction;
    private static Object timeEnum, titleEnum, subEnum, resetEnum;

    static {
        chatBaseComponent = Reflection.getNMSClass("IChatBaseComponent");
        chatSerializer = Reflection.getNMSClass("IChatBaseComponent$ChatSerializer");
        titleAction = Reflection.getNMSClass("PacketPlayOutTitle$EnumTitleAction");

        try {
            serializer = chatSerializer.getMethod("a", String.class);

            Class<?> packetType = Reflection.getNMSClass("PacketPlayOutTitle");
            chatConstructor = packetType.getConstructor(titleAction, chatBaseComponent);
            timeConstructor = packetType.getConstructor(titleAction, chatBaseComponent, Integer.TYPE, Integer.TYPE, Integer.TYPE);

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        titleEnum = titleAction.getEnumConstants()[0];
        subEnum = titleAction.getEnumConstants()[1];

        if (Reflection.getVersion().startsWith("v1_7_") || Reflection.getVersion().startsWith("v1_8_") || Reflection.getVersion().startsWith("v1_9_") || Reflection.getVersion().startsWith("v1_10_"))
            timeEnum = titleAction.getEnumConstants()[2];
        else {
            timeEnum = titleAction.getEnumConstants()[3];
            resetEnum = titleAction.getEnumConstants()[5];
        }

    }

    /**
     * Send title.
     *
     * @param player   the player
     * @param title    the title
     * @param subTitle the sub title
     * @param fadeIn   the fade in
     * @param show     the show
     * @param fadeOut  the fade out
     */
    public static void sendTitle(Player player, String title, String subTitle, int fadeIn, int show, int fadeOut) {
        try {
            Object lengthPacket = timeConstructor.newInstance(timeEnum, null, fadeIn, show, fadeOut);
            sendPacket(player, lengthPacket);

            Object titleSerialized = serializer.invoke(null, "{\"text\":\"" + title + "\"}");
            Object titlePacket = chatConstructor.newInstance(titleEnum, titleSerialized);
            sendPacket(player, titlePacket);

            if (subTitle != "") {
                Object subSerialized = serializer.invoke(null, "{\"text\":\"" + subTitle + "\"}");
                Object subPacket = chatConstructor.newInstance(subEnum, subSerialized);
                sendPacket(player, subPacket);
            }

        } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reset.
     *
     * @param player the player
     */
    public static void reset(Player player) {
        try {
            if (resetEnum == null)
                sendTitle(player, "", "", 0, 0, 0);
            else {
                Object titlePacket = chatConstructor.newInstance(resetEnum, null);
                sendPacket(player, titlePacket);
            }
        } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
    }
    private static Class<?> classPacket = Reflection.getNMSClass("Packet");
    public static void sendPacket(Player player, Object packet) {
        Object handle = Reflection.getHandle(player);
        Object playerConnection = Reflection.get(handle.getClass(), "playerConnection", handle);
        Method methodSend = Reflection.getMethod(playerConnection.getClass(), "sendPacket", classPacket);
        Reflection.invokeMethod(methodSend, playerConnection, new Object[]{packet});
    }
}
