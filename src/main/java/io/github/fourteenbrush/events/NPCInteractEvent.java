package io.github.fourteenbrush.events;

import io.github.fourteenbrush.NPC;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class NPCInteractEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final NPC clicked;
    private final Player player;
    private final NPCClickAction action;

    public NPCInteractEvent(NPC clicked, Player player, NPCClickAction action) {
        this.clicked = clicked;
        this.player = player;
        this.action = action;
    }

    public NPC getClicked() {
        return clicked;
    }

    public Player getPlayer() {
        return player;
    }

    public NPCClickAction getAction() {
        return action;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
