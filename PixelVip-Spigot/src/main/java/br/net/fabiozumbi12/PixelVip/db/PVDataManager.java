package br.net.fabiozumbi12.PixelVip.db;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public interface PVDataManager {
	void saveVips();
	
	void saveKeys();
	
	void addRawVip(String group, String id, String pgroup, long duration, String nick, String expires);
	
	void addRawKey(String key, String group, long duration, int uses);
	
	void addRawItemKey(String key, List<String> cmds);
	
	HashMap<String,List<String[]>> getActiveVipList();
	
	HashMap<String,List<String[]>> getAllVipList();
	
	List<String[]> getVipInfo(String puuid);
	
	List<String> getItemKeyCmds(String key);
	
	void removeKey(String key);
	
	void removeItemKey(String key);
	
	Set<String> getListKeys();
	
	Set<String> getItemListKeys();
		
	String[] getKeyInfo(String key);
	
	void setKeyUse(String key, int uses);
	
	void setVipActive(String id, String vip, boolean active);
	
	void setVipDuration(String id, String vip, long duration);
	
	boolean containsVip(String id, String vip);
	
	void setVipKitCooldown(String id, String vip, long cooldown);
	
	void removeVip(String id, String vip);

	long getVipCooldown(String id, String vip);
	
	long getVipDuration(String id, String vip);
	
	boolean isVipActive(String id, String vip);

	void closeCon();
}
