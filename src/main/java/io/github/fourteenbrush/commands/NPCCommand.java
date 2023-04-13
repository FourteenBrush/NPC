package io.github.fourteenbrush.commands;

import io.github.fourteenbrush.NPC;
import io.github.fourteenbrush.NPCPlugin;
import io.github.fourteenbrush.npc.NPCOptions;
import io.github.fourteenbrush.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NPCCommand implements CommandExecutor, TabCompleter {

    private final NPCPlugin plugin;

    public NPCCommand(NPCPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(Utils.colorize("&cThe console cannot execute this!"));
            return true;
        }
        Player player = (Player) commandSender;
        if (command.getLabel().equalsIgnoreCase("npc") && args.length == 1 && args[0].equalsIgnoreCase("spawn")) {
            NPC npc = plugin.getNpcManager().newNPC(NPCOptions.builder()
                    .setName("Technoblade")
                    .hideNametag(false)
                    // See https://sessionserver.mojang.com/session/minecraft/profile/b876ec32e396476ba1158438d83c67d4?unsigned=false
                    // This will give Technoblade's Texture & Signature
                    .setTexture("ewogICJ0aW1lc3RhbXAiIDogMTYxODM1Nzg1Mjc3NSwKICAicHJvZmlsZUlkIiA6ICJiODc2ZWMzMmUzOTY0NzZiYTExNTg0MzhkODNjNjdkNCIsCiAgInByb2ZpbGVOYW1lIiA6ICJUZWNobm9ibGFkZSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS83ODZjMDM5ZDk2OWQxODM5MTU1MjU1ZTM4ZTdiMDZhNjI2ZWE5ZjhiYWY5Y2I1NWUwYTc3MzExZWZlMThhM2UiCiAgICB9CiAgfQp9")
                    .setSignature("VYUQfmkBsHTXWf8tRRCiws/A/iwA+VIZ8wrbp4IdcM1CnYhZP+LTrVXSSl8bc88vQPbGxdL2Ks3Ow4cmBnGWe1ezpHWRO4vcyXRvh0AOle3XGYI31x7usryY9rr/37xLTdKqh7V7Ox4Dq9qt8Bmo8QBolpXBT6HlCbPPG6cu5AlycWTsoA6X0zvfWihLXH1suIU4LPeaORX1SpppzCGo1mz/SI0HaLM5vJIhktf8mJqP0DwUQetezb+b+LtJenoFp2lE/qRcrRF739NuwMw6tniea1dn3ftAWBH8l0r3p6uDzOAjJOxGnR5YBWfOewWF3x+k2UXkKqC01pPu1S8PbQDayP0++XsXw+28wvI/5G4U2otIoEU4lucViJPjWXmn2acE5LNq8eHaAm+5pBCmJ1TNGZkDlTHekivW1kaFh2NQCY3SyizUWjcPVE6aYZK8c2bltGOcKhgzJb7hYnjdbTX0S7KMD1csCN1bUduyv9byzvJkpVNka3LavCZCIPJ1ICpLFwQemdzaqXTp2x+5lnxKCMLu0EpDikX1Hcm86pJpW4qxXcZNRyCEwlulseIvRIgyfNzjDO2F8CYf94JqQVZ/pKonuRnJGTuWzur788JfaWcfrOv0hCUt8F5Yw1BCkBsucDhPaOwvQLPLET7+aPhuermXKsiw5UasB+OGhlA=")
                    .setLocation(player.getLocation()));
            npc.showTo(player);
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], Collections.singletonList("spawn"), new ArrayList<>());
        }
        return Collections.emptyList();
    }
}
