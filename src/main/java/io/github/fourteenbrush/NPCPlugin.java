package io.github.fourteenbrush;

import io.github.fourteenbrush.npc.NPCManager;
import io.github.fourteenbrush.utils.Utils;
import org.bukkit.plugin.java.JavaPlugin;

public class NPCPlugin extends JavaPlugin {

    private static NPCPlugin instance;
    private NPCManager npcManager;

    @Override
    public void onEnable() {
        long now = System.currentTimeMillis();
        Utils.logInfo("Initializing...");
        instance = this;
        npcManager = new NPCManager(this, false);
        Utils.logInfo("Done! (" + (System.currentTimeMillis() - now) + "ms)");
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    public static NPCPlugin getInstance() {
        return instance;
    }

    public NPCManager getNpcManager() {
        return npcManager;
    }
}
