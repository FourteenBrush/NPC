package io.github.fourteenbrush.npc.versioned;

import io.github.fourteenbrush.NPCPlugin;
import io.github.fourteenbrush.npc.BaseNPC;
import io.github.fourteenbrush.npc.NPCOptions;
import io.github.fourteenbrush.utils.Utils;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public class NPC_Reflection extends BaseNPC {

    private Object entityPlayer;

    public NPC_Reflection(NPCPlugin plugin, NPCOptions npcOptions) {
        super(plugin, npcOptions);
        addToWorld(npcOptions.getLocation());
    }

    @Override
    public Location getLocation() {
        try {
            Class<?> EntityPlayer = entityPlayer.getClass();
            Object minecraftWorld = EntityPlayer.getMethod("getWorld").invoke(entityPlayer);
            Object craftWorld = minecraftWorld.getClass().getMethod("getWorld").invoke(minecraftWorld);

            double locX = (double) EntityPlayer.getMethod("locX").invoke(entityPlayer);
            double locY = (double) EntityPlayer.getMethod("locY").invoke(entityPlayer);
            double locZ = (double) EntityPlayer.getMethod("locZ").invoke(entityPlayer);
            float yaw = (float) EntityPlayer.getField("yaw").get(entityPlayer);
            float pitch = (float) EntityPlayer.getField("pitch").get(entityPlayer);

            return new Location(
                    (World) craftWorld,
                    locX,
                    locY,
                    locZ,
                    yaw,
                    pitch
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int getId() {
        if (entityPlayer == null) return -1;
        try {
            Method getId = entityPlayer.getClass().getSuperclass().getSuperclass().getSuperclass().getDeclaredMethod("getId");
            return (int) getId.invoke(entityPlayer);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public void addToWorld(Location location) {
        try {
            Object minecraftServer = NMS_HELPER.getCraftBukkitClass("CraftServer").getMethod("getServer").invoke(Bukkit.getServer());
            Object worldServer = NMS_HELPER.getCraftBukkitClass("CraftWorld").getMethod("getHandle").invoke(location.getWorld());
            Constructor<?> entityPlayerConstructor = NMS_HELPER.getNMSClass("EntityPlayer").getDeclaredConstructors()[0];
            Constructor<?> interactManagerConstructor = NMS_HELPER.getNMSClass("PlayerInteractManager").getDeclaredConstructors()[0];
            Object interactManager = interactManagerConstructor.newInstance(worldServer);
            entityPlayer = entityPlayerConstructor.newInstance(minecraftServer, worldServer, createGameprofile(), interactManager);
            entityPlayer.getClass().getMethod("setLocation", double.class, double.class, double.class, float.class, float.class)
                    .invoke(entityPlayer,
                    location.getX(),
                    location.getY(),
                    location.getZ(),
                    location.getYaw(),
                    location.getPitch());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void showTo(Player target) {
        viewers.add(target.getUniqueId());
        try {
            // PacketPlayOutPlayerInfo
            Object addPlayerEnum = NMS_HELPER.getNMSClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction").getField("ADD_PLAYER").get(null);
            Constructor<?> packetPlayOutPlayerInfoConstructor =
                    NMS_HELPER.getNMSClass("PacketPlayOutPlayerInfo").getConstructor(
                            NMS_HELPER.getNMSClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction"),
                            Class.forName("[Lnet.minecraft.server." + NMS_HELPER.getServerVersionString() + ".EntityPlayer;")
                    );
            Object array = Array.newInstance(NMS_HELPER.getNMSClass("EntityPlayer"), 1);
            Array.set(array, 0, this.entityPlayer);

            Object packetPlayOutPlayerInfo = packetPlayOutPlayerInfoConstructor.newInstance(addPlayerEnum, array);
            sendPacket(target, packetPlayOutPlayerInfo);

            // PacketPlayOutNamedEntitySpawn
            Constructor<?> packetPlayOutNamedEntitySpawnConstructor = NMS_HELPER.getNMSClass("PacketPlayOutNamedEntitySpawn")
                    .getConstructor(NMS_HELPER.getNMSClass("EntityHuman"));
            Object packetPlayOutNamedEntitySpawn = packetPlayOutNamedEntitySpawnConstructor.newInstance(this.entityPlayer);
            sendPacket(target, packetPlayOutNamedEntitySpawn);

            // Scoreboard Team
            Object scoreboardManager = Bukkit.getServer().getClass().getMethod("getScoreboardManager")
                    .invoke(Bukkit.getServer());
            Object mainScoreboard = scoreboardManager.getClass().getMethod("getMainScoreboard")
                    .invoke(scoreboardManager);
            Object scoreboard = mainScoreboard.getClass().getMethod("getHandle").invoke(mainScoreboard);

            Method getTeamMethod = scoreboard.getClass().getMethod("getTeam", String.class);
            Constructor<?> scoreboardTeamConstructor = NMS_HELPER.getNMSClass("ScoreboardTeam").getDeclaredConstructor(NMS_HELPER.getNMSClass("Scoreboard"), String.class);
            Object scoreboardTeam = getTeamMethod.invoke(scoreboard, entityName) == null ?
                    scoreboardTeamConstructor.newInstance(scoreboard, entityName) :
                    getTeamMethod.invoke(scoreboard, entityName);
            Class<?> nameTagStatusEnum = NMS_HELPER.getNMSClass("ScoreboardTeamBase$EnumNameTagVisibility");
            Method setNameTagVisibility = scoreboardTeam.getClass().getMethod("setNameTagVisibility", nameTagStatusEnum);

            if (hideNametag) {
                setNameTagVisibility.invoke(scoreboardTeam, nameTagStatusEnum.getField("NEVER").get(null));
            } else {
                setNameTagVisibility.invoke(scoreboardTeam, nameTagStatusEnum.getField("ALWAYS").get(null));
            }
            Class<?> collisionStatusEnum = NMS_HELPER.getNMSClass("ScoreboardTeamBase$EnumTeamPush");
            Method setCollisionRule = scoreboardTeam.getClass().getMethod("setCollisionRule", collisionStatusEnum);
            setCollisionRule.invoke(scoreboardTeam, collisionStatusEnum.getField("NEVER").get(null));

            if (hideNametag) {
                Object grayChatFormat = NMS_HELPER.getNMSClass("EnumChatFormat").getField("GRAY").get(null);
                scoreboardTeam.getClass().getMethod("setColor", NMS_HELPER.getNMSClass("EnumChatFormat"))
                        .invoke(scoreboardTeam, grayChatFormat);

                Constructor<?> chatMessageConstructor = NMS_HELPER.getNMSClass("ChatMessage").getDeclaredConstructor(String.class);
                scoreboardTeam.getClass().getMethod("setPrefix", NMS_HELPER.getNMSClass("IChatBaseComponent"))
                        .invoke(scoreboardTeam, chatMessageConstructor.newInstance(Utils.colorize("&7[NPC] ")));
            }

            Class<?> packetPlayOutScoreboardTeamClass = NMS_HELPER.getNMSClass("PacketPlayOutScoreboardTeam");
            Constructor<?> packetPlayOutScoreboardTeamTeamIntConstructor = packetPlayOutScoreboardTeamClass.getConstructor(NMS_HELPER.getNMSClass("ScoreboardTeam"), int.class);
            Constructor<?> packetPlayOutScoreboardTeamTeamCollectionIntConstructor = packetPlayOutScoreboardTeamClass.getConstructor(NMS_HELPER.getNMSClass("ScoreboardTeam"), Collection.class, int.class);

            sendPacket(target, packetPlayOutScoreboardTeamTeamIntConstructor.newInstance(scoreboardTeam, 1));
            sendPacket(target, packetPlayOutScoreboardTeamTeamIntConstructor.newInstance(scoreboardTeam, 0));
            sendPacket(target,
                    packetPlayOutScoreboardTeamTeamCollectionIntConstructor.newInstance(
                            scoreboardTeam,
                            Collections.singletonList(entityName),
                            3
                    ));
            sendHeadRotationPacketFor(target);
            Bukkit.getServer().getScheduler().runTaskTimer(plugin, task -> {
                Player currentlyOnline = Bukkit.getPlayer(target.getUniqueId());
                if (currentlyOnline == null || !currentlyOnline.isOnline()) {
                    task.cancel();
                    return;
                }
                sendHeadRotationPacketFor(target);
            }, 0, 2);
            Bukkit.getServer().getScheduler().runTaskLater(plugin, () -> {
                try {
                    Object removePlayerEnum = NMS_HELPER.getNMSClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction").getField("REMOVE_PLAYER").get(null);
                    Object removeFromTabPacket = packetPlayOutPlayerInfoConstructor.newInstance(removePlayerEnum, array);
                    sendPacket(target, removeFromTabPacket);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }, 20);
            Bukkit.getServer().getScheduler().runTaskLater(plugin, () -> fixSkinHelmetLayerForPlayer(target), 8);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void hideFrom(Player target) {
        if (!viewers.remove(target.getUniqueId())) return;
        try {
            Constructor<?> destroyPacketConstructor = NMS_HELPER
                    .getNMSClass("PacketPlayOutEntityDestroy").getConstructor(int[].class);
            Object packet = destroyPacketConstructor.newInstance(new Object[] { new int[] { getId() } });
            sendPacket(target, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendHeadRotationPacketFor(Player target) {
        Location original = getLocation();
        Location location = original.clone().setDirection(target.getLocation().subtract(original.clone()).toVector());

        byte yaw = (byte) (location.getYaw() * 256 / 360);
        byte pitch = (byte) (location.getPitch() * 256 / 360);

        try {
            // PacketPlayOutEntityHeadRotation
            Constructor<?> packetPlayOutEntityHeadRotationConstructor = NMS_HELPER.getNMSClass("PacketPlayOutEntityHeadRotation")
                    .getConstructor(NMS_HELPER.getNMSClass("Entity"), byte.class);

            Object packetPlayOutEntityHeadRotation = packetPlayOutEntityHeadRotationConstructor.newInstance(entityPlayer, yaw);
            sendPacket(target, packetPlayOutEntityHeadRotation);

            Constructor<?> packetPlayOutEntityLookConstructor = NMS_HELPER.getNMSClass("PacketPlayOutEntity$PacketPlayOutEntityLook")
                    .getConstructor(int.class, byte.class, byte.class, boolean.class);
            Object packetPlayOutEntityLook = packetPlayOutEntityLookConstructor.newInstance(getId(), yaw, pitch, false);
            sendPacket(target, packetPlayOutEntityLook);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fixSkinHelmetLayerForPlayer(Player player) {
        byte skinFixByte = 0x01 | 0x02 | 0x04 | 0x08 | 0x10 | 0x20 | 0x40;
        sendMetadata(player, skinFixByte);
    }

    private void sendMetadata(Player player, byte b) {
        try {
            Object dataWatcher = entityPlayer.getClass().getMethod("getDataWatcher").invoke(entityPlayer);
            Class<?> dataWatcherRegistryClass = NMS_HELPER.getNMSClass("DataWatcherRegistry");
            Object registry = dataWatcherRegistryClass.getField("a").get(null);
            Method watcherCreateMethod = registry.getClass().getMethod("a", int.class);

            Method dataWatcherSetMethod = dataWatcher.getClass().getMethod("set", NMS_HELPER.getNMSClass("DataWatcherObject"), Object.class);
            dataWatcherSetMethod.invoke(dataWatcher, watcherCreateMethod.invoke(registry, 16), b);

            Constructor<?> packetPlayOutEntityMetadataConstructor = NMS_HELPER.getNMSClass("PacketPlayOutEntityMetadata")
                    .getDeclaredConstructor(int.class, NMS_HELPER.getNMSClass("DataWatcher"), boolean.class);
            sendPacket(player, packetPlayOutEntityMetadataConstructor.newInstance(getId(), dataWatcher, false));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete() {
        viewers.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).forEach(this::hideFrom);
    }
}
