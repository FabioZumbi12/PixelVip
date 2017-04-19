package br.net.fabiozumbi12.PixelVip;

import java.util.UUID;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

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

	public void addGroup(OfflinePlayer p, String group){
		perms.playerAddGroup(null, p, group);
	}
	
	public void setGroup(String uuid, String group){
		OfflinePlayer p = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
		if (p.getName() != null){
			perms.playerAddGroup(null, p, group);
		}		
	}
	
	public void removeGroup(String uuid, String group) {
		OfflinePlayer p = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
		if (p.getName() != null){
			perms.playerRemoveGroup(null, p, group);
		}
		
	}
}