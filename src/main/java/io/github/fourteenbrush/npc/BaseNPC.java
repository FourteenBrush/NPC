package io.github.fourteenbrush.npc;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.github.fourteenbrush.NMSHelper;
import io.github.fourteenbrush.NPC;
import io.github.fourteenbrush.NPCPlugin;
import io.github.fourteenbrush.utils.Utils;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public abstract class BaseNPC implements NPC {

    protected static final NMSHelper NMS_HELPER = NMSHelper.getInstance();

    protected final UUID uuid;
    protected final String name;
    protected final String entityName;
    protected final String texture;
    protected final String signature;
    protected final boolean hideNametag;
    protected final Set<UUID> viewers;
    protected final NPCPlugin plugin;

    public BaseNPC(NPCPlugin plugin, NPCOptions npcOptions) {
        uuid = UUID.randomUUID();
        viewers = new HashSet<>();
        this.plugin = plugin;
        name = npcOptions.getName();
        texture = npcOptions.getTexture();
        signature = npcOptions.getTexture();
        hideNametag = npcOptions.isHideNametag();
        entityName = hideNametag ? Utils.randomCharacters(10) : name;
        addToWorld(npcOptions.getLocation());
    }

    @Override
    public String getName() {
        return name;
    }

    protected void sendPacket(Player player, Object packet) {
        NMS_HELPER.sendPacket(player, packet);
    }

    protected GameProfile createGameprofile() {
        GameProfile gameProfile = new GameProfile(uuid, entityName);
        gameProfile.getProperties().put("textures", new Property("textures", texture, signature));
        return gameProfile;
    }
}
