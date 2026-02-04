package br.net.fabiozumbi12.pixelvip.bukkit;

import br.net.fabiozumbi12.pixelvip.compat.TaskHandle;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class PermsAPI {

    public TaskHandle task;
    private final Permission perms;
    private final PixelVip plugin;
    private int delay = 10;

    public PermsAPI(Permission perms, PixelVip plugin) {
        this.perms = perms;
        this.plugin = plugin;
        task = plugin.getScheduler().runSyncTimer(() -> {
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
        plugin.getScheduler().runSyncLater(() -> {
            Player p = Bukkit.getPlayer(UUID.fromString(uuid));
            if (p != null) {
                plugin.getScheduler().runEntity(p, () -> perms.playerAddGroup(null, p, group));
            } else {
                OfflinePlayer off = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
                if (off.getName() != null) {
                    plugin.getScheduler().runSync(() -> perms.playerAddGroup(null, off, group));
                }
            }
        }, (1 + delay) * 10L);
    }

    public void setGroup(String uuid, String group) {
        delay = 10;
        plugin.getScheduler().runSyncLater(() -> {
            Player p = Bukkit.getPlayer(UUID.fromString(uuid));
            if (p != null) {
                plugin.getScheduler().runEntity(p, () -> {
                    String[] groups = getGroups(p);
                    for (String pGroup : groups) {
                        perms.playerRemoveGroup(null, p, pGroup);
                    }
                    perms.playerAddGroup(null, p, group);
                });
            } else {
                OfflinePlayer off = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
                if (off.getName() != null) {
                    plugin.getScheduler().runSync(() -> {
                        String[] groups = getGroups(off);
                        for (String pGroup : groups) {
                            perms.playerRemoveGroup(null, off, pGroup);
                        }
                        perms.playerAddGroup(null, off, group);
                    });
                }
            }
        }, (1 + delay) * 10L);
    }

    public void removeGroup(String uuid, String group) {
        if (plugin.getPVConfig().getGroupList(true).contains(group)) {
            plugin.getScheduler().runSyncLater(() -> {
                Player p = Bukkit.getPlayer(UUID.fromString(uuid));
                if (p != null) {
                    plugin.getScheduler().runEntity(p, () -> perms.playerRemoveGroup(null, p, group));
                } else {
                    OfflinePlayer off = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
                    if (off.getName() != null) {
                        plugin.getScheduler().runSync(() -> perms.playerRemoveGroup(null, off, group));
                    }
                }
            }, (1 + delay) * 10L);
        }
    }
}
