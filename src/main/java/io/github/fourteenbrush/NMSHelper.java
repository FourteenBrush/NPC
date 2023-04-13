package io.github.fourteenbrush;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;

public class NMSHelper {

    private static final NMSHelper instance = new NMSHelper();
    private final ServerVersion serverVersion;
    private final String serverVersionString;

    private NMSHelper() {
        serverVersionString = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        ServerVersion version = ServerVersion.UNKNOWN;
        for (ServerVersion option : ServerVersion.values()) {
            if (option.name().equalsIgnoreCase(serverVersionString)) {
                version = option;
            }
        }
        serverVersion = version;
    }

    public static NMSHelper getInstance() {
        return instance;
    }

    public ServerVersion getServerVersion() {
        return serverVersion;
    }

    public String getServerVersionString() {
        return serverVersionString;
    }

    public void sendPacket(Player target, Object packet) {
        try {
            Object handle = getHandle(target);
            Object playerConnection = target.getClass().getField("playerConnection").get(handle);
            playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Object getHandle(Player player) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        return player.getClass().getMethod("getHandle").invoke(player);

    }

    public Class<?> getNMSClass(String name) throws ClassNotFoundException {
        return Class.forName("net.minecraft.server." + getServerVersion() + "." + name);
    }

    public Class<?> getCraftBukkitClass(String name) throws ClassNotFoundException {
        return Class.forName("org.bukkit.craftbukkit." + getServerVersion() + "." + name);
    }
}
