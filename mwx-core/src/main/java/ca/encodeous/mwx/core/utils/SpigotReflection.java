package ca.encodeous.mwx.core.utils;

/*
 * This file is part of TabTPS, licensed under the MIT License.
 *
 * Copyright (c) 2020-2021 Jason Penilla
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;
import org.bukkit.entity.Player;

import static ca.encodeous.mwx.core.utils.Crafty.findField;
import static ca.encodeous.mwx.core.utils.Crafty.needCraftClass;
import static ca.encodeous.mwx.core.utils.Crafty.needNMSClassOrElse;

public final class SpigotReflection {
    private static final class Holder {
        static final SpigotReflection INSTANCE = new SpigotReflection();
    }

    public static SpigotReflection get() {
        return Holder.INSTANCE;
    }

    private static final Class<?> MinecraftServer_class = needNMSClassOrElse(
            "MinecraftServer",
            "net.minecraft.server.MinecraftServer"
    );
    private static final Class<?> CraftPlayer_class = needCraftClass("entity.CraftPlayer");
    private static final Class<?> EntityPlayer_class = needNMSClassOrElse(
            "EntityPlayer",
            "net.minecraft.server.level.EntityPlayer",
            "net.minecraft.server.level.ServerPlayer"
    );

    private static final MethodHandle CraftPlayer_getHandle_method = needMethod(CraftPlayer_class, "getHandle", EntityPlayer_class);
    private static final MethodHandle MinecraftServer_getServer_method = needStaticMethod(MinecraftServer_class, "getServer", MinecraftServer_class);

    private static final Field EntityPlayer_ping_field = findField(EntityPlayer_class, "ping");
    private static final Field MinecraftServer_recentTps_field = needField(MinecraftServer_class, "recentTps"); // Spigot added field

    public int ping(final Player player) {
        if (EntityPlayer_ping_field == null) {
            throw new IllegalStateException("The ping Field is null!");
        }
        final Object nmsPlayer = invokeOrThrow(CraftPlayer_getHandle_method, player);
        try {
            return EntityPlayer_ping_field.getInt(nmsPlayer);
        } catch (final IllegalAccessException e) {
            throw new IllegalStateException(String.format("Failed to get ping for player: '%s'", player.getName()), e);
        }
    }

    public double [] recentTps() {
        final Object server = invokeOrThrow(MinecraftServer_getServer_method);
        try {
            return (double[]) MinecraftServer_recentTps_field.get(server);
        } catch (final IllegalAccessException e) {
            throw new IllegalStateException("Failed to get server TPS", e);
        }
    }

    private static MethodHandle needMethod(final Class<?> holderClass, final String methodName, final Class<?> returnClass, final Class<?> ... parameterClasses) {
        return Objects.requireNonNull(
                Crafty.findMethod(holderClass, methodName, returnClass, parameterClasses),
                String.format(
                        "Could not locate method '%s' in class '%s'",
                        methodName,
                        holderClass.getCanonicalName()
                )
        );
    }

    private static MethodHandle needStaticMethod(final Class<?> holderClass, final String methodName, final Class<?> returnClass, final Class<?> ... parameterClasses) {
        return Objects.requireNonNull(
                Crafty.findStaticMethod(holderClass, methodName, returnClass, parameterClasses),
                String.format(
                        "Could not locate static method '%s' in class '%s'",
                        methodName,
                        holderClass.getCanonicalName()
                )
        );
    }

    public static Field needField(final Class<?> holderClass, final String fieldName) {
        final Field field;
        try {
            field = holderClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (final NoSuchFieldException e) {
            throw new IllegalStateException(String.format("Unable to find field '%s' in class '%s'", fieldName, holderClass.getCanonicalName()), e);
        }
    }

    private static Object invokeOrThrow(final MethodHandle methodHandle, final Object ... params) {
        try {
            if (params.length == 0) {
                return methodHandle.invoke();
            }
            return methodHandle.invokeWithArguments(params);
        } catch (final Throwable throwable) {
            throw new IllegalStateException(String.format("Unable to invoke method with args '%s'", Arrays.toString(params)), throwable);
        }
    }
}