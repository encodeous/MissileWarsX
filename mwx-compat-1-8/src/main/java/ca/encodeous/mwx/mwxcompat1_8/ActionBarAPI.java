package ca.encodeous.mwx.mwxcompat1_8;

import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ActionBarAPI {
    private static Class<?> classPacket = Reflection.getNMSClass("Packet");
    private static Class<?> chatSerializer;
    private static Class<?> chatBaseComponent;
    private static Method serializer;
    private static Constructor<?> chatConstructor;
    static {
        chatBaseComponent = Reflection.getNMSClass("IChatBaseComponent");
        chatSerializer = Reflection.getNMSClass("IChatBaseComponent$ChatSerializer");

        try {
            serializer = chatSerializer.getMethod("a", String.class);

            Class<?> packetType = Reflection.getNMSClass("PacketPlayOutChat");
            chatConstructor = packetType.getConstructor(chatBaseComponent, byte.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
    public static void sendPacket(Player player, Object packet) {
        Object handle = Reflection.getHandle(player);
        Object playerConnection = Reflection.get(handle.getClass(), "playerConnection", handle);
        Method methodSend = Reflection.getMethod(playerConnection.getClass(), "sendPacket", classPacket);
        Reflection.invokeMethod(methodSend, playerConnection, new Object[]{packet});
    }
    /**
     * Send the title
     *
     * @param player
     * @param msg
     */
    public static void sendActionBar(Player player, String msg) {
        try {
            Object serialized = serializer.invoke(null, "{\"text\":\"" + msg + "\"}");

            Object packet = chatConstructor.newInstance(serialized, (byte) 2);
            sendPacket(player, packet);

        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

    }
}