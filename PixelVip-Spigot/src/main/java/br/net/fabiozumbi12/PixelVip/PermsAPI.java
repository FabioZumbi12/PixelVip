package br.net.fabiozumbi12.PixelVip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PermsAPI {

	private Permission perms;

	public PermsAPI(Permission perms){
		this.perms = perms;
	}
	
	public String getGroup(OfflinePlayer player){
		return perms.getPrimaryGroup(null, player);
	}
	
	public String[] getGroups(OfflinePlayer player){		
		return perms.getPlayerGroups(null, player);	
	}	

	public void addGroup(String uuid, String group){
        if (Bukkit.getPlayer(uuid) != null && Bukkit.getPlayer(uuid).isOnline()){
            Player p = Bukkit.getPlayer(uuid);
            List<String> groups = new ArrayList<>(Arrays.asList(getGroups(p)));
            groups.add(group);
            for (String pGroup:groups){
                if (!perms.playerInGroup(p, pGroup)) perms.playerAddGroup(p, group);
            }
        } else {
            OfflinePlayer p = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
            if (p.getName() != null){
                List<String> groups = new ArrayList<>(Arrays.asList(getGroups(p)));
                groups.add(group);
                for (String pGroup:groups){
                    if (!perms.playerInGroup(null, p, pGroup)) perms.playerAddGroup(null, p, group);
                }
            }
        }
	}
	
	public void setGroup(String uuid, String group){
		if (Bukkit.getPlayer(uuid) != null && Bukkit.getPlayer(uuid).isOnline()){
            Player p = Bukkit.getPlayer(uuid);
            List<String> groups = new ArrayList<>(Arrays.asList(getGroups(p)));
            for (String pGroup:groups){
                if (perms.playerInGroup(p, pGroup)) perms.playerRemoveGroup(p, pGroup);
            }
            perms.playerAddGroup(p, group);
        } else {
            OfflinePlayer p = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
            if (p.getName() != null){
                List<String> groups = new ArrayList<>(Arrays.asList(getGroups(p)));
                for (String pGroup:groups){
                    if (perms.playerInGroup(null, p, pGroup)) perms.playerRemoveGroup(null, p, pGroup);
                }
                perms.playerAddGroup(null, p, group);
            }
        }
	}
	
	public void removeGroup(String uuid, String group) {
        if (Bukkit.getPlayer(uuid) != null && Bukkit.getPlayer(uuid).isOnline()){
            perms.playerRemoveGroup(Bukkit.getPlayer(uuid), group);
        } else {
            OfflinePlayer p = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
            if (p.getName() != null){
                perms.playerRemoveGroup(null, p, group);
            }
        }
	}
}