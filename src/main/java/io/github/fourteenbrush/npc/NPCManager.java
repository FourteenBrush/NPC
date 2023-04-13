package io.github.fourteenbrush.npc;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.github.fourteenbrush.NMSHelper;
import io.github.fourteenbrush.NPC;
import io.github.fourteenbrush.NPCPlugin;
import io.github.fourteenbrush.ServerVersion;
import io.github.fourteenbrush.events.NPCClickAction;
import io.github.fourteenbrush.events.NPCInteractEvent;
import io.github.fourteenbrush.npc.versioned.NPC_Reflection;
import io.github.fourteenbrush.npc.versioned.NPC_v1_16_R3;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.Validate;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class NPCManager {

    private final NPCPlugin plugin;
    private final boolean useReflection;
    private final Set<NPC> registeredNPCs;
    private final Cache<Player, NPC> clickedNPCCache;

    public NPCManager(NPCPlugin plugin, boolean useReflection) {
        this.plugin = plugin;
        this.useReflection = useReflection;
        registeredNPCs = new HashSet<>();
        clickedNPCCache = CacheBuilder.newBuilder().expireAfterWrite(1L, TimeUnit.SECONDS).build();
        ProtocolLibrary.getProtocolManager().addPacketListener(
                new PacketAdapter(plugin, PacketType.Play.Client.USE_ENTITY) {
                    @Override
                    public void onPacketReceiving(PacketEvent event) {
                        EnumWrappers.EntityUseAction useAction = event.getPacket().getEntityUseActions().read(0);
                        int entityId = event.getPacket().getIntegers().read(0);
                        handleEntityClick(event.getPlayer(), entityId, NPCClickAction.fromProtocolLibAction(useAction));
                    }
                }
        );
    }

    private void handleEntityClick(Player player, int entityId, NPCClickAction action) {
        registeredNPCs.stream()
                .filter(npc -> npc.getId() == entityId)
                .forEach(npc -> Bukkit.getServer().getScheduler().runTaskLater(plugin, () -> {
                    NPC previouslyClickedNPC = clickedNPCCache.getIfPresent(player);
                    if (previouslyClickedNPC != null && previouslyClickedNPC.equals(npc)) return; // If they've clicked this same NPC in the last 0.5 seconds ignore this click
                    clickedNPCCache.put(player, npc);

                    NPCInteractEvent event = new NPCInteractEvent(npc, player, action);
                    Bukkit.getPluginManager().callEvent(event);
                }, 2));
    }

    public NPC newNPC(NPCOptions options) {
        ServerVersion serverVersion = NMSHelper.getInstance().getServerVersion();
        NPC npc = null;
        if (useReflection) {
            serverVersion = ServerVersion.REFLECTED;
        }
        switch (serverVersion) {
            case REFLECTED:
                npc = new NPC_Reflection(plugin, options);
                break;
            case v1_16_R3:
                npc = new NPC_v1_16_R3(plugin, options);
                break;
        }
        Validate.validState(npc != null, "Invalid server version " + serverVersion + ". This plugin needs to be updated!");
        registeredNPCs.add(npc);
        return npc;
    }

    public Optional<NPC> findNPC(String name) {
        return registeredNPCs.stream().filter(npc -> npc.getName().equalsIgnoreCase(name)).findFirst();
    }

    public void deleteNPC(NPC npc) {
        npc.delete();
        registeredNPCs.remove(npc);
    }

    public void deleteAllNPCs() {
        // copy the set to prevent concurrent modification exception
        Set<NPC> npcsCopy = new HashSet<>(registeredNPCs);
        npcsCopy.forEach(this::deleteNPC);
    }
}
