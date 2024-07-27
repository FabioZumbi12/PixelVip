package br.net.fabiozumbi12.pixelvip.bukkit;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class PermsAPI {

    public int taskId;
    private final Permission perms;
    private final PixelVip plugin;
    private int delay = 10;

    public PermsAPI(Permission perms, PixelVip plugin) {
        this.perms = perms;
        this.plugin = plugin;
        taskId = plugin.serv.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (delay > 10) delay = 10;
        }, 20, 20);

    }

    public String getGroup(Player player) {
        return perms.getPrimaryGroup(player);
    }

    public String[] getGroups(OfflinePlayer player) {
        return perms.getPlayerGroups(null, player);
    }

    public List<String> getGroupsList(OfflinePlayer player) {
        return new ArrayList<>(Arrays.asList(perms.getPlayerGroups(null, player)));
    }

    public void addGroup(String uuid, String group) {
        delay = 10;
        plugin.serv.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            if (Bukkit.getPlayer(UUID.fromString(uuid)) != null) {
                Player p = Bukkit.getPlayer(UUID.fromString(uuid));
                perms.playerAddGroup(null, p, group);
            } else {
                OfflinePlayer p = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
                if (p.getName() != null) {
                    perms.playerAddGroup(null, p, group);
                }
            }
        }, (1 + delay) * 10L);
    }

    public void setGroup(String uuid, String group) {
        delay = 10;
        plugin.serv.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            if (Bukkit.getPlayer(UUID.fromString(uuid)) != null) {
                Player p = Bukkit.getPlayer(UUID.fromString(uuid));
                String[] groups = getGroups(p);
                for (String pGroup : groups) {
                    perms.playerRemoveGroup(null, p, pGroup);
                }
                perms.playerAddGroup(null, p, group);
            } else {
                OfflinePlayer p = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
                if (p.getName() != null) {
                    String[] groups = getGroups(p);
                    for (String pGroup : groups) {
                        perms.playerRemoveGroup(null, p, pGroup);
                    }
                    perms.playerAddGroup(null, p, group);
                }
            }
        }, (1 + delay) * 10L);
    }

    public void removeGroup(String uuid, String group) {
        if (plugin.getPVConfig().getGroupList(true).contains(group)) {
            plugin.serv.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                if (Bukkit.getPlayer(UUID.fromString(uuid)) != null) {
                    perms.playerRemoveGroup(null, Bukkit.getPlayer(UUID.fromString(uuid)), group);
                } else {
                    OfflinePlayer p = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
                    if (p.getName() != null) {
                        perms.playerRemoveGroup(null, p, group);
                    }
                }
            }, (1 + delay) * 10L);
        }
    }
}