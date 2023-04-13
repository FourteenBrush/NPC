package io.github.fourteenbrush;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface NPC {
    String getName();
    Location getLocation();
    int getId();
    void addToWorld(Location location);
    void showTo(Player target);
    void hideFrom(Player target);
    void delete();
}
