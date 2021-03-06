package ca.encodeous.mwx.core.utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

public class Reflection {


    /**
     * Get the server's version
     *
     * @return version
     */
    public static String getVersion() {
        String name = Bukkit.getServer().getClass().getPackage().getName();
        String version = name.substring(name.lastIndexOf('.') + 1);
        return version;
    }

    /**
     * Get a nms class
     *
     * @param name the name
     * @return nms class
     */
    public static Class<?> getNMSClass(String name) {
        String simpleName = name.substring(name.lastIndexOf('.') + 1);
        return getClass(
                "net.minecraft.server." + getVersion() + "." + name,
                "net.minecraft." + name,
                "net.minecraft.server." + getVersion() + "." + simpleName,
                "net.minecraft." + simpleName);
    }


    /**
     * Get a bukkit class
     * (org.bukkit.craftbukkit)
     *
     * @param name the name
     * @return craft class
     */
    public static Class<?> getCraftClass(String name) {
        try {
            return Class.forName("org.bukkit.craftbukkit." + getVersion() + "." + name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get classes
     *
     * @param names names of classes
     * @return class
     */
    public static Class<?> getClass(String... names) {
        for(int i = 0; i<names.length; i++) {
            try {
                return Class.forName(names[i]);
            } catch (ClassNotFoundException ignored) {}
        }
        new ClassNotFoundException(Arrays.toString(names)).printStackTrace();
        return null;
    }

    /**
     * Get the handle of the passed object
     *
     * @param object the object
     * @return handle
     */
    public static Object getHandle(Object object) {
        try {
            return getMethod(object.getClass(), "getHandle").invoke(object, new Object[0]);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Get Constructor from class
     *
     * @param c     the c
     * @param types the types
     * @return constructor
     */
    public static Constructor getConstructor(Class<?> c, Class<?>... types) {
        try {
            return c.getDeclaredConstructor(types);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get a method from the class
     *
     * @param c          the c
     * @param methodName the method name
     * @return method
     */
    public static Method getMethod(Class<?> c, String methodName) {
        for (Method method : c.getMethods())
            if (method.getName().equals(methodName))
                return method;
        return null;
    }

    /**
     * Get a method from a class with specific parameter types
     *
     * @param c              the c
     * @param methodName     the method name
     * @param parameterTypes the parameter types
     * @return method
     */
    public static Method getMethod(Class<?> c, String methodName, Class<?>... parameterTypes) {
        try {
            return c.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Invoke a method
     *
     * @param c              the c
     * @param methodName     the method name
     * @param parameterTypes the parameter types
     * @param parameter      the parameter
     * @return the object
     */
    public static Object invokeMethod(Class<?> c, String methodName, Class<?>[] parameterTypes, Object[] parameter) {
        return invokeMethod(c, methodName, null, parameterTypes, parameter);
    }

    /**
     * Invoke a method on an object
     *
     * @param c          the c
     * @param methodName the method name
     * @param object     the object
     * @return the object
     */
    public static Object invokeMethod(Class<?> c, String methodName, Object object) {
        return invokeMethod(c, methodName, object, null, null);
    }

    /**
     * Invoke a method on an object
     *
     * @param methodName the method name
     * @param object     the object
     * @return the object
     */
    public static <T> T invokeMethod(String methodName, Object object) {
        return (T) invokeMethod(object.getClass(), methodName, object, null, null);
    }

    /**
     * Invoke a method on an object
     *
     * @param methodName the method name
     * @param object     the object
     * @return the object
     */
    public static <T> T invokeMethodThrown(String methodName, Object object) throws InvocationTargetException {
        return (T) invokeMethodThrown(object.getClass(), methodName, object, null, null);
    }

    /**
     * Invoke method object.
     *
     * @param c              The Class
     * @param methodName     The MethodName
     * @param object         The Object
     * @param parameterTypes The ParameterTypess
     * @param parameter      The Parameters
     * @return The Object from the Invoked Method
     */
    public static Object invokeMethod(Class<?> c, String methodName, Object object, Class<?>[] parameterTypes, Object[] parameter) {
        Method m = getMethod(c, methodName, parameterTypes);
        try {
            return m.invoke(object, parameter);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Invoke method object.
     *
     * @param c              The Class
     * @param methodName     The MethodName
     * @param object         The Object
     * @param parameterTypes The ParameterTypess
     * @param parameter      The Parameters
     * @return The Object from the Invoked Method
     */
    public static Object invokeMethodThrown(Class<?> c, String methodName, Object object, Class<?>[] parameterTypes, Object[] parameter) throws InvocationTargetException {
        Method m = getMethod(c, methodName, parameterTypes);
        try {
            return m.invoke(object, parameter);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Invoke method object.
     *
     * @param method The Method
     * @param object The Object
     * @param args   The Args
     * @return The object from the Invoked Method
     */
    public static <T> T invokeMethod(Method method, Object object, Object... args) {
        try {
            return (T) method.invoke(object, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Invoke method object.
     *
     * @param method The Method
     * @param object The Object
     * @param args   The Args
     * @return The object from the Invoked Method
     */
    public static <T> T invokeMethodThrown(Method method, Object object, Object... args) throws InvocationTargetException {
        try {
            return (T) method.invoke(object, args);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * New instance object.
     *
     * @param constructor The Constructor
     * @param args        The args
     * @return Get a new instance from a constructor
     */
    public static Object newInstance(Constructor constructor, Object... args) {
        try {
            constructor.setAccessible(true);
            return constructor.newInstance(args);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Set a Field
     *
     * @param c         The Class
     * @param fieldName The FieldName
     * @param obj       The Object
     * @param value     The Value
     */
    public static void setField(Class<?> c, String fieldName, Object obj, Object value) {
        try {
            Field field = c.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set a Field
     *
     * @param field The field
     * @param obj   The Object
     * @param value The Value
     */
    public static void setField(Field field, Object obj, Object value) {
        try {
            field.setAccessible(true);
            field.set(obj, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets field.
     *
     * @param c         The Class
     * @param fieldName The FieldName
     * @return Get the field from the class
     */
    public static Field getField(Class<?> c, String fieldName) {
        try {
            Field field = c.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Get object.
     *
     * @param field  The field
     * @param object The Object
     * @return Get the Object from the Field
     */
    public static Object get(Field field, Object object) {
        try {
            field.setAccessible(true);
            return field.get(object);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Assumes field isn't private
     *
     * @param fieldName The field
     * @param object    The Object
     * @return Get the Object from the Field
     */
    public static <T> T get(String fieldName, Object object) {
        try {
            return (T) get(object.getClass().getDeclaredField(fieldName), object);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Assumes field isn't private
     *
     * @param c         The Class
     * @param fieldName The field
     * @param object    The Object
     * @return Get the Object from the Field
     */
    public static <T> T get(Class<?> c, String fieldName, Object object) {
        try {
            return (T) get(c.getDeclaredField(fieldName), object);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    /**
     * Get online players player [ ].
     *
     * @return Get Array of online players regardless of Mc Version
     */
    public static Player[] getOnlinePlayers() {
        try {
            Collection<? extends Player> p = Bukkit.getOnlinePlayers();
            return p.<Player>toArray(new Player[p.size()]);
        } catch (NoSuchMethodError e) {
            try {
                Player[] players = (Player[]) Reflection.getMethod(Bukkit.class, "getOnlinePlayers").invoke(null, new Object[0]);
                return players;
            } catch (SecurityException | IllegalAccessException | IllegalArgumentException | java.lang.reflect.InvocationTargetException e1) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
