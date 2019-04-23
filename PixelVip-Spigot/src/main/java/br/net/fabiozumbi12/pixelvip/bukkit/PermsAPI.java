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
    private Permission perms;
    private PixelVip plugin;
    private int delay = 10;

    public PermsAPI(Permission perms, PixelVip plugin) {
        this.perms = perms;
        this.plugin = plugin;
        taskId = plugin.serv.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (delay > 10) delay = 10;
        }, 20, 20);

    }

    public String getGroup(OfflinePlayer player) {
        return perms.getPrimaryGroup(null, player);
    }

    public String[] getGroups(OfflinePlayer player) {
        return perms.getPlayerGroups(null, player);
    }

    public List<String> getGroupsList(OfflinePlayer player) {
        return new ArrayList<>(Arrays.asList(perms.getPlayerGroups(null, player)));
    }

    public void addGroup(String uuid, String group) {
        delay = 10;
        if (Bukkit.getPlayer(UUID.fromString(uuid)) != null) {
            Player p = Bukkit.getPlayer(UUID.fromString(uuid));
            if (plugin.getPVConfig().getRoot().getBoolean("configs.luckpermsfix")) plugin.serv.getScheduler().runTaskLaterAsynchronously(plugin, () -> perms.playerAddGroup(null, p, group), (1 + delay) * 10);
            else plugin.serv.getScheduler().runTaskLater(plugin, () -> perms.playerAddGroup(null, p, group), (1 + delay) * 10);
            delay++;
        } else {
            OfflinePlayer p = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
            if (p.getName() != null) {
                if (plugin.getPVConfig().getRoot().getBoolean("configs.luckpermsfix")) plugin.serv.getScheduler().runTaskLaterAsynchronously(plugin, () -> perms.playerAddGroup(null, p, group), (1 + delay) * 10);
                else plugin.serv.getScheduler().runTaskLater(plugin, () -> perms.playerAddGroup(null, p, group), (1 + delay) * 10);
                delay++;
            }
        }
    }

    public void setGroup(String uuid, String group) {
        delay = 10;
        if (Bukkit.getPlayer(UUID.fromString(uuid)) != null) {
            Player p = Bukkit.getPlayer(UUID.fromString(uuid));
            String[] groups = getGroups(p);
            for (String pGroup : groups) {
                if (plugin.getPVConfig().getRoot().getBoolean("configs.luckpermsfix")) plugin.serv.getScheduler().runTaskLaterAsynchronously(plugin, () -> perms.playerRemoveGroup(null, p, pGroup), (1 + delay) * 10);
                else plugin.serv.getScheduler().runTaskLater(plugin, () -> perms.playerRemoveGroup(null, p, pGroup), (1 + delay) * 10);
                delay++;
            }
            if (plugin.getPVConfig().getRoot().getBoolean("configs.luckpermsfix")) plugin.serv.getScheduler().runTaskLaterAsynchronously(plugin, () -> perms.playerAddGroup(null, p, group), (1 + delay) * 10);
            else plugin.serv.getScheduler().runTaskLater(plugin, () -> perms.playerAddGroup(null, p, group), (1 + delay) * 10);
            delay++;
        } else {
            OfflinePlayer p = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
            if (p.getName() != null) {
                String[] groups = getGroups(p);
                for (String pGroup : groups) {
                    if (plugin.getPVConfig().getRoot().getBoolean("configs.luckpermsfix")) plugin.serv.getScheduler().runTaskLaterAsynchronously(plugin, () -> perms.playerRemoveGroup(null, p, pGroup), (1 + delay) * 10);
                    else plugin.serv.getScheduler().runTaskLater(plugin, () -> perms.playerRemoveGroup(null, p, pGroup), (1 + delay) * 10);
                    delay++;
                }
                if (plugin.getPVConfig().getRoot().getBoolean("configs.luckpermsfix")) plugin.serv.getScheduler().runTaskLaterAsynchronously(plugin, () -> perms.playerAddGroup(null, p, group), (1 + delay) * 10);
                else plugin.serv.getScheduler().runTaskLater(plugin, () -> perms.playerAddGroup(null, p, group), (1 + delay) * 10);
                delay++;
            }
        }
    }

    public void removeGroup(String uuid, String group) {
        if (plugin.getPVConfig().getGroupList(true).contains(group)) {
            if (Bukkit.getPlayer(UUID.fromString(uuid)) != null) {
                if (plugin.getPVConfig().getRoot().getBoolean("configs.luckpermsfix")) plugin.serv.getScheduler().runTaskLaterAsynchronously(plugin, () -> perms.playerRemoveGroup(null, Bukkit.getPlayer(UUID.fromString(uuid)), group), (1 + delay) * 10);
                else plugin.serv.getScheduler().runTaskLater(plugin, () -> perms.playerRemoveGroup(null, Bukkit.getPlayer(UUID.fromString(uuid)), group), (1 + delay) * 10);
                delay++;
            } else {
                OfflinePlayer p = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
                if (p.getName() != null) {
                    if (plugin.getPVConfig().getRoot().getBoolean("configs.luckpermsfix")) plugin.serv.getScheduler().runTaskLaterAsynchronously(plugin, () -> perms.playerRemoveGroup(null, p, group), (1 + delay) * 10);
                    else plugin.serv.getScheduler().runTaskLater(plugin, () -> perms.playerRemoveGroup(null, p, group), (1 + delay) * 10);
                    delay++;
                }
            }
        }
    }
}