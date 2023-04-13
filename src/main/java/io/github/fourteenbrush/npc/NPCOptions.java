package io.github.fourteenbrush.npc;

import org.bukkit.Location;

public class NPCOptions {

    private String name;
    private String texture;
    private String signature;
    private Location location;
    private boolean hideNametag;

    private NPCOptions() {}

    public static NPCOptions builder() {
        return new NPCOptions();
    }

    public NPCOptions setName(String name) {
        this.name = name;
        return this;
    }

    public NPCOptions setTexture(String texture) {
        this.texture = texture;
        return this;
    }

    public NPCOptions setSignature(String signature) {
        this.signature = signature;
        return this;
    }

    public NPCOptions setLocation(Location location) {
        this.location = location;
        return this;
    }

    public NPCOptions hideNametag(boolean hideNametag) {
        this.hideNametag = hideNametag;
        return this;
    }

    public String getName() {
        return name;
    }

    public String getTexture() {
        return texture;
    }

    public String getSignature() {
        return signature;
    }

    public Location getLocation() {
        return location;
    }

    public boolean isHideNametag() {
        return hideNametag;
    }
}
