package br.net.fabiozumbi12.PixelVip.db;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.configuration.file.YamlConfiguration;

import br.net.fabiozumbi12.PixelVip.PixelVip;

public class PVDataFile implements PVDataManager {
	private YamlConfiguration vipsFile;
	private YamlConfiguration keysFile;
	private YamlConfiguration transFile;
	private PixelVip plugin;
	
	public PVDataFile(PixelVip plugin){
		this.plugin = plugin;
		
		File fileVips = new File(plugin.getDataFolder(),"vips.yml");
		if (!fileVips.exists()){
			try {
				fileVips.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		File fileKeys = new File(plugin.getDataFolder(),"keys.yml");
		if (!fileKeys.exists()){
			try {
				fileKeys.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		File fileTrans = new File(plugin.getDataFolder(),"transactions.yml");
		if (!fileTrans.exists()){
			try {
				fileTrans.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		keysFile = YamlConfiguration.loadConfiguration(fileKeys);
		vipsFile = YamlConfiguration.loadConfiguration(fileVips);
		transFile = YamlConfiguration.loadConfiguration(fileTrans);
	}
	
	@Override
	public boolean transactionExist(String trans){
		return this.transFile.contains(trans);
	}
	
	@Override
	public void addTras(String trans, String player) {
		this.transFile.set(trans, player);	
		saveTrans();
	}
	
	@Override
	public void removeTrans(String trans){
		this.transFile.set(trans, null);
		saveTrans();
	}
	
	@Override
	public HashMap<String, String> getAllTrans(){
		HashMap<String, String> trans = new HashMap<String, String>();
		for (String code:this.transFile.getKeys(false)){
			trans.put(code, this.transFile.getString(code));
		}
		return trans;
	}
	
	private void saveTrans(){
		try {
			this.transFile.save(new File(plugin.getDataFolder(),"transactions.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void saveKeys() {
		File fileKeys = new File(plugin.getDataFolder(),"keys.yml");
		try {
			keysFile.save(fileKeys);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void saveVips() {
		File fileVips = new File(plugin.getDataFolder(),"vips.yml");
		try {
			vipsFile.save(fileVips);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public HashMap<String,List<String[]>> getActiveVipList(){
		HashMap<String,List<String[]>> vips = new HashMap<String,List<String[]>>();		
		plugin.getPVConfig().getGroupList().stream().filter(group->vipsFile.getConfigurationSection("activeVips."+group) != null).forEach(group -> {
			vipsFile.getConfigurationSection("activeVips."+group).getKeys(false).forEach(uuid -> {	
				List<String[]> vipInfo = getVipInfo(uuid);
				List<String[]> activeVips = new ArrayList<String[]>();
				vipInfo.stream().filter(v->v[3] != null && v[3].equals("true")).forEach(active -> {
					activeVips.add(active);					
				});						
				if (activeVips.size() > 0){
					vips.put(uuid, activeVips);
				}
			});			
		});
		return vips;
	}
	
	@Override
	public HashMap<String,List<String[]>> getAllVipList(){
		HashMap<String,List<String[]>> vips = new HashMap<String,List<String[]>>();		
		plugin.getPVConfig().getGroupList().stream().filter(group->vipsFile.getConfigurationSection("activeVips."+group) != null).forEach(group -> {
			vipsFile.getConfigurationSection("activeVips."+group).getKeys(false).forEach(uuid -> {				
				List<String[]> vipInfo = getVipInfo(uuid);
				vips.put(uuid, vipInfo);
			});			
		});
		return vips;
	}
	
	@Override
	public List<String[]> getVipInfo(String puuid){
		List<String[]> vips = new ArrayList<String[]>();
		plugin.getPVConfig().getGroupList().stream().filter(k->vipsFile.contains("activeVips."+k.toString()+"."+puuid)).forEach(key ->{
			vips.add(new String[]{
					vipsFile.getString("activeVips."+key+"."+puuid+".duration"),
					key,
					vipsFile.getString("activeVips."+key+"."+puuid+".playerGroup"),
					vipsFile.getString("activeVips."+key+"."+puuid+".active"),
					vipsFile.getString("activeVips."+key+"."+puuid+".nick")});
		});				
		return vips;
	}
	
	@Override
	public List<String> getItemKeyCmds(String key) {
		return keysFile.getStringList("keys.itemKeys."+key+".cmds");
	}
	
	@Override
	public void removeKey(String key){
		keysFile.set("keys.keys."+key, null);
	}
	
	@Override
	public void removeItemKey(String key){
		keysFile.set("keys.itemKeys."+key, null);
	}
	
	@Override
	public Set<String> getListKeys() {
		if (keysFile.getConfigurationSection("keys.keys") != null){
			return keysFile.getConfigurationSection("keys.keys").getKeys(false);
		}
		return new HashSet<String>();
	}
	
	@Override
	public Set<String> getItemListKeys() {
		if (keysFile.getConfigurationSection("keys.itemKeys") != null){
			return keysFile.getConfigurationSection("keys.itemKeys").getKeys(false);
		}
		return new HashSet<String>();
	}

	@Override
	public void addRawVip(String group, String id, String pgroup, long duration, String nick, String expires, boolean active) {
		id = id.toLowerCase();
		vipsFile.set("activeVips."+group+"."+id+".active", active);
		addRawVip(group, id, pgroup, duration, nick, expires);
	}
	
	@Override
	public void addRawVip(String group, String id, String pgroup, long duration, String nick, String expires) {
		id = id.toLowerCase();
		vipsFile.set("activeVips."+group+"."+id+".playerGroup", pgroup);
		vipsFile.set("activeVips."+group+"."+id+".duration", duration);
		vipsFile.set("activeVips."+group+"."+id+".nick", nick);
		vipsFile.set("activeVips."+group+"."+id+".expires-on-exact", expires);
	}

	@Override
	public void addRawKey(String key, String group, long duration, int uses) {
		keysFile.set("keys.keys."+key+".group", group);
		keysFile.set("keys.keys."+key+".duration", duration);
		keysFile.set("keys.keys."+key+".uses", uses);
	}

	@Override
	public void addRawItemKey(String key, List<String> cmds) {
		keysFile.set("keys.itemKeys."+key+".cmds", cmds);
	}
	
	@Override
	public String[] getKeyInfo(String key){	
		if (getListKeys().contains(key)){
			return new String[]{keysFile.getString("keys.keys."+key+".group"),
					keysFile.getString("keys.keys."+key+".duration"),
					keysFile.getString("keys.keys."+key+".uses")};
		}
		return new String[0];
	}
	
	@Override
	public void setKeyUse(String key, int uses){
		keysFile.set("keys.keys."+key+".uses", uses);
	}
	
	@Override
	public void setVipActive(String id, String vip, boolean active){
		vipsFile.set("activeVips."+vip+"."+id.toLowerCase()+".active", active);
	}
	
	@Override
	public void setVipDuration(String id, String vip, long duration){
		vipsFile.set("activeVips."+vip+"."+id.toLowerCase()+".duration", duration);
	}
	
	@Override
	public boolean containsVip(String id, String vip){
		return vipsFile.isConfigurationSection("activeVips."+vip+"."+id);
	}
	
	@Override
	public void setVipKitCooldown(String id, String vip, long cooldown){
		vipsFile.set("activeVips."+vip+"."+id+".kit-cooldown", cooldown);
	}
	
	@Override
	public void removeVip(String id, String vip){
		vipsFile.set("activeVips."+vip+"."+id, null);
	}
	
	@Override
	public long getVipCooldown(String id, String vip){
		return vipsFile.getLong("activeVips."+vip+"."+id+".kit-cooldown", 0);
	}
	
	@Override
	public long getVipDuration(String id, String vip){
		return vipsFile.getLong("activeVips."+vip+"."+id+".duration");
	}
	
	@Override
	public boolean isVipActive(String uuid, String vip){
		return vipsFile.getBoolean("activeVips."+vip+"."+uuid+".active", true);
	}

	@Override
	public void closeCon() {
		//for mysql
	}

	@Override
	public String getVipUUID(String player) {
		Iterator<String> it = vipsFile.getKeys(true).stream().filter(key->key.contains(".nick")).iterator();
		while(it.hasNext()){
			String key = it.next();
			if (vipsFile.getString(key).equals(player)){
				Pattern pairRegex = Pattern.compile("\\p{XDigit}{8}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{12}");
			    Matcher matcher = pairRegex.matcher(key);
			    while (matcher.find()) {
			        return matcher.group(0).toLowerCase();
			    }
			}
		}
		return null;
	}
}
