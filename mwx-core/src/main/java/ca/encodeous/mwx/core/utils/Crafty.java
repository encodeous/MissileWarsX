package ca.encodeous.mwx.core.utils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;
import org.bukkit.Bukkit;
/*
 * This file is part of adventure-platform, licensed under the MIT License.
 *
 * Copyright (c) 2018-2020 KyoriPowered
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

// https://github.com/jpenilla/jmplib/blob/master/src/main/java/xyz/jpenilla/jmplib/Crafty.java
public final class Crafty {
    private static final Lookup LOOKUP = MethodHandles.lookup();
    private static final String PREFIX_NMS = "net.minecraft.server";
    private static final String PREFIX_CRAFTBUKKIT = "org.bukkit.craftbukkit";
    private static final String CRAFT_SERVER = "CraftServer";
    private static final String VERSION;

    private Crafty() {
    }

    public static Class<?> needNMSClassOrElse(String nms, String... classNames) throws RuntimeException {
        Class<?> nmsClass = findNmsClass(nms);
        if (nmsClass != null) {
            return nmsClass;
        } else {
            String[] var3 = classNames;
            int var4 = classNames.length;

            for (int var5 = 0; var5 < var4; ++var5) {
                String name = var3[var5];
                Class<?> maybe = findClass(name);
                if (maybe != null) {
                    return maybe;
                }
            }

            throw new IllegalStateException(String.format("Couldn't find a class! NMS: '%s' or '%s'.", nms, Arrays.toString(classNames)));
        }
    }


    public static Class<?> findClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException var2) {
            return null;
        }
    }

    public static boolean hasClass(String className) {
        return findClass(className) != null;
    }


    public static MethodHandle findMethod(Class<?> holderClass, String methodName, Class<?> returnClass, Class<?>... parameterClasses) {
        if (holderClass != null && returnClass != null) {
            Class[] var4 = parameterClasses;
            int var5 = parameterClasses.length;

            for (int var6 = 0; var6 < var5; ++var6) {
                Class<?> parameterClass = var4[var6];
                if (parameterClass == null) {
                    return null;
                }
            }

            try {
                return LOOKUP.findVirtual(holderClass, methodName, MethodType.methodType(returnClass, parameterClasses));
            } catch (IllegalAccessException | NoSuchMethodException var8) {
                return null;
            }
        } else {
            return null;
        }
    }


    public static MethodHandle findStaticMethod(Class<?> holderClass, String methodName, Class<?> returnClass, Class<?>... parameterClasses) {
        if (holderClass != null && returnClass != null) {
            Class[] var4 = parameterClasses;
            int var5 = parameterClasses.length;

            for (int var6 = 0; var6 < var5; ++var6) {
                Class<?> parameterClass = var4[var6];
                if (parameterClass == null) {
                    return null;
                }
            }

            try {
                return LOOKUP.findStatic(holderClass, methodName, MethodType.methodType(returnClass, parameterClasses));
            } catch (IllegalAccessException | NoSuchMethodException var8) {
                return null;
            }
        } else {
            return null;
        }
    }

    public static boolean hasField(Class<?> holderClass, String name, Class<?> type) {
        if (holderClass == null) {
            return false;
        } else {
            try {
                Field field = holderClass.getDeclaredField(name);
                return field.getType() == type;
            } catch (NoSuchFieldException var4) {
                return false;
            }
        }
    }

    public static boolean hasMethod(Class<?> holderClass, String methodName, Class<?>... parameterClasses) {
        if (holderClass == null) {
            return false;
        } else {
            Class[] var3 = parameterClasses;
            int var4 = parameterClasses.length;

            for (int var5 = 0; var5 < var4; ++var5) {
                Class<?> parameterClass = var3[var5];
                if (parameterClass == null) {
                    return false;
                }
            }

            try {
                holderClass.getMethod(methodName, parameterClasses);
                return true;
            } catch (NoSuchMethodException var7) {
                return false;
            }
        }
    }


    public static MethodHandle findConstructor(Class<?> holderClass, Class<?>... parameterClasses) {
        if (holderClass == null) {
            return null;
        } else {
            Class[] var2 = parameterClasses;
            int var3 = parameterClasses.length;

            for (int var4 = 0; var4 < var3; ++var4) {
                Class<?> parameterClass = var2[var4];
                if (parameterClass == null) {
                    return null;
                }
            }

            try {
                return LOOKUP.findConstructor(holderClass, MethodType.methodType(Void.TYPE, parameterClasses));
            } catch (IllegalAccessException | NoSuchMethodException var6) {
                return null;
            }
        }
    }


    public static Field needField(Class<?> holderClass, String fieldName) throws NoSuchFieldException {
        Field field = holderClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
    }


    public static Field findField(Class<?> holderClass, String fieldName) {
        return findField(holderClass, fieldName, (Class) null);
    }


    public static Field findField(Class<?> holderClass, String fieldName, Class<?> expectedType) {
        if (holderClass == null) {
            return null;
        } else {
            Field field;
            try {
                field = holderClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException var5) {
                return null;
            }

            field.setAccessible(true);
            return expectedType != null && !expectedType.isAssignableFrom(field.getType()) ? null : field;
        }
    }


    public static MethodHandle findSetterOf(Field field) {
        if (field == null) {
            return null;
        } else {
            try {
                return LOOKUP.unreflectSetter(field);
            } catch (IllegalAccessException var2) {
                return null;
            }
        }
    }


    public static MethodHandle findGetterOf(Field field) {
        if (field == null) {
            return null;
        } else {
            try {
                return LOOKUP.unreflectGetter(field);
            } catch (IllegalAccessException var2) {
                return null;
            }
        }
    }


    public static Object findEnum(Class<?> enumClass, String enumName) {
        return findEnum(enumClass, enumName, 2147483647);
    }


    public static Object findEnum(Class<?> enumClass, String enumName, int enumFallbackOrdinal) {
        if (enumClass != null && Enum.class.isAssignableFrom(enumClass)) {
            try {
                return Enum.valueOf(enumClass.asSubclass(Enum.class), enumName);
            } catch (IllegalArgumentException var5) {
                Object[] constants = enumClass.getEnumConstants();
                return constants.length > enumFallbackOrdinal ? constants[enumFallbackOrdinal] : null;
            }
        } else {
            return null;
        }
    }

    public static boolean isCraftBukkit() {
        return VERSION != null;
    }


    public static String findCraftClassName(String className) {
        return isCraftBukkit() ? "org.bukkit.craftbukkit" + VERSION + className : null;
    }


    public static Class<?> findCraftClass(String className) {
        String craftClassName = findCraftClassName(className);
        return craftClassName == null ? null : findClass(craftClassName);
    }


    public static <T> Class<? extends T> findCraftClass(String className, Class<T> superClass) {
        Class<?> craftClass = findCraftClass(className);
        return craftClass != null && ((Class) Objects.requireNonNull(superClass, "superClass")).isAssignableFrom(craftClass) ? craftClass.asSubclass(superClass) : null;
    }


    public static Class<?> needCraftClass(String className) {
        return (Class) Objects.requireNonNull(findCraftClass(className), "Could not find org.bukkit.craftbukkit class " + className);
    }


    public static String findNmsClassName(String className) {
        return isCraftBukkit() ? "net.minecraft.server" + VERSION + className : null;
    }


    public static Class<?> findNmsClass(String className) {
        String nmsClassName = findNmsClassName(className);
        return nmsClassName == null ? null : findClass(nmsClassName);
    }


    public static Class<?> needNmsClass(String className) {
        return (Class) Objects.requireNonNull(findNmsClass(className), "Could not find net.minecraft.server class " + className);
    }


    public static Lookup lookup() {
        return LOOKUP;
    }

    static {
        Class<?> serverClass = Bukkit.getServer().getClass();
        if (!serverClass.getSimpleName().equals("CraftServer")) {
            VERSION = null;
        } else if (serverClass.getName().equals("org.bukkit.craftbukkit.CraftServer")) {
            VERSION = ".";
        } else {
            String name = serverClass.getName();
            name = name.substring("org.bukkit.craftbukkit".length());
            name = name.substring(0, name.length() - "CraftServer".length());
            VERSION = name;
        }

    }
}
