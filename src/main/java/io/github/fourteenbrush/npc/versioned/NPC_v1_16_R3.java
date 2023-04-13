package io.github.fourteenbrush.npc.versioned;

import com.mojang.authlib.GameProfile;
import io.github.fourteenbrush.NPCPlugin;
import io.github.fourteenbrush.npc.BaseNPC;
import io.github.fourteenbrush.npc.NPCOptions;
import io.github.fourteenbrush.utils.Utils;
import net.minecraft.server.v1_16_R3.*;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.scoreboard.CraftScoreboard;
import org.bukkit.craftbukkit.v1_16_R3.scoreboard.CraftScoreboardManager;
import org.bukkit.entity.Player;

import java.util.*;

public class NPC_v1_16_R3 extends BaseNPC {

    private EntityPlayer entityPlayer;

    public NPC_v1_16_R3(NPCPlugin plugin, NPCOptions npcOptions) {
        super(plugin, npcOptions);
        addToWorld(npcOptions.getLocation());
    }

    @Override
    public void addToWorld(Location location) {
        Validate.notNull(location.getWorld(), "world cannot be null");
        MinecraftServer minecraftServer = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer worldServer = ((CraftWorld) location.getWorld()).getHandle();
        GameProfile gameProfile = createGameprofile();
        entityPlayer = new EntityPlayer(minecraftServer, worldServer, gameProfile, new PlayerInteractManager(worldServer));
        entityPlayer.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    @Override
    public Location getLocation() {
        return entityPlayer.getBukkitEntity().getLocation();
    }

    @Override
    public int getId() {
        return entityPlayer.getId();
    }

    @Override
    public void showTo(Player target) {
        viewers.add(target.getUniqueId());
        PacketPlayOutPlayerInfo packetPlayOutPlayerInfo = new PacketPlayOutPlayerInfo(
                PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entityPlayer
        );
        sendPacket(target, packetPlayOutPlayerInfo);
        PacketPlayOutNamedEntitySpawn packetPlayOutNamedEntitySpawn = new PacketPlayOutNamedEntitySpawn(entityPlayer);
        sendPacket(target, packetPlayOutNamedEntitySpawn);
        CraftScoreboardManager scoreboardManager = ((CraftServer) Bukkit.getServer()).getScoreboardManager();
        assert scoreboardManager != null;
        CraftScoreboard scoreboard = scoreboardManager.getMainScoreboard();
        Scoreboard board = scoreboard.getHandle();
        ScoreboardTeam scoreboardTeam = board.getTeam(entityName);
        if (scoreboardTeam == null) {
            scoreboardTeam = new ScoreboardTeam(board, entityName);
        }
        scoreboardTeam.setNameTagVisibility(hideNametag ? ScoreboardTeamBase.EnumNameTagVisibility.NEVER :
                        ScoreboardTeamBase.EnumNameTagVisibility.ALWAYS
        );
        scoreboardTeam.setCollisionRule(ScoreboardTeamBase.EnumTeamPush.NEVER);
        if (hideNametag) {
            scoreboardTeam.setColor(EnumChatFormat.GRAY);
            scoreboardTeam.setPrefix(new ChatMessage(Utils.colorize("&7NPC ")));
        }
        sendPacket(target, new PacketPlayOutScoreboardTeam(scoreboardTeam, 1)); // Create team
        sendPacket(target, new PacketPlayOutScoreboardTeam(scoreboardTeam, 0)); // Setup team options
        sendPacket(target, new PacketPlayOutScoreboardTeam(scoreboardTeam, Collections.singletonList(entityName), 3)); // Add entityPlayer to team entries
        Bukkit.getScheduler().runTaskTimer(plugin, task -> {
            Player to = Bukkit.getPlayer(target.getUniqueId());
            if (to == null || !viewers.contains(to.getUniqueId()) || !to.isOnline()) {
                task.cancel();
                return;
            }
            sendHeadRotationPacketFor(target);
        }, 0, 2);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            PacketPlayOutPlayerInfo removeFromTabPacket = new PacketPlayOutPlayerInfo(
                    PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, entityPlayer
            );
            sendPacket(target, removeFromTabPacket);
        }, 20);
        Bukkit.getServer().getScheduler().runTaskLater(plugin, () -> fixSkinHelmetLayerForPlayer(target), 8);
    }

    private void sendHeadRotationPacketFor(Player target) {
        Location original = getLocation();
        Location cloned = original.clone().setDirection(target.getLocation().subtract(original.clone()).toVector());
        byte yaw = (byte) (cloned.getYaw() * 256 / 360);
        byte pitch = (byte) (cloned.getPitch() * 256 / 360);
        PacketPlayOutEntityHeadRotation headRotationPacket = new PacketPlayOutEntityHeadRotation(entityPlayer, yaw);
        sendPacket(target, headRotationPacket);
        PacketPlayOutEntity.PacketPlayOutEntityLook entityLook = new PacketPlayOutEntity.PacketPlayOutEntityLook(
                getId(), yaw, pitch, false
        );
        sendPacket(target, entityLook);
    }

    private void fixSkinHelmetLayerForPlayer(Player player) {
        byte skinFixByte = 0x01 | 0x02 | 0x04 | 0x08 | 0x10 | 0x20 | 0x40;
        sendMetadata(player, skinFixByte);
    }

    private void sendMetadata(Player target, byte b) {
        DataWatcher dataWatcher = entityPlayer.getDataWatcher();
        DataWatcherSerializer<Byte> registry = DataWatcherRegistry.a;
        dataWatcher.set(registry.a(16), b);
        PacketPlayOutEntityMetadata metadataPacket = new PacketPlayOutEntityMetadata(
                getId(),
                dataWatcher,
                false
        );
        sendPacket(target, metadataPacket);
    }

    @Override
    public void hideFrom(Player target) {
        if (viewers.remove(target.getUniqueId())) {
            PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(entityPlayer.getId());
            sendPacket(target, packet);
        }
    }

    @Override
    public void delete() {
        viewers.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).forEach(this::hideFrom);
    }
}
