package br.net.fabiozumbi12.PixelVip;

import java.text.SimpleDateFormat;
import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import com.earth2me.essentials.User;

import br.net.fabiozumbi12.PixelVip.db.PVDataFile;
import br.net.fabiozumbi12.PixelVip.db.PVDataManager;
import br.net.fabiozumbi12.PixelVip.db.PVDataMysql;

public class PVConfig {	
	
	private PixelVip plugin;
	private int delay = 0;
	private PVDataManager dataManager;
	
	public PVConfig(PixelVip plugin){
		this.plugin = plugin;				
				
		/*----------------------------------------------------------------------------------*/
		
		plugin.getConfig().options().header("=============== PixelVip Configuration Options ================\n"
				+ "\n"
				+ "This is the default configuration and some information about some configurations.\n"
				+ "\n"
				+ "In \"groups\" on \"commands\" and \"cmdChances\"(Lists) you can use this placeholders:\n"
				+ "- {p} = Players Name\n"
				+ "- {vip} = Vip Group\n"
				+ "- {playergroup} = Player Group before Vip activation\n"
				+ "- {days} = Days of activated Vip\n"
				+ "\n"
				+ "In \"groups\" > \"cmdChances\"(List) you can add commands to run based on a % chance. \n"
				+ "Use numbers below 0-100 like the example on \"vip1\".\n"
				+ "\n"
				+ "In \"configs\" > \"cmdOnRemoveVip\"(String) you can use this placeholders:\n"
				+ "- {p} = Player Name\n"
				+ "- {vip} = Name of Vip Removed\n"
				+ "\n"
				+ "In \"configs\" > \"commandsToRunOnChangeVip\"(List) you can use this placeholders:\n"
				+ "- {p} = Player Name\n"
				+ "- {newvip} = Name of Vip the player is changing to\n"
				+ "- {oldvip} = Name of Vip the player is changing from\n"
				+ "\n"
				+ "In \"configs\" > \"commandsToRunOnVipFinish\" and \"run-on-vip-finish\" (Lists) you can use this placeholders:\n"
				+ "- {p} = Player Name\n"
				+ "- {vip} = Name of Vip\n"
				+ "- {playergroup} = Player Group before Vip activation\n"
				+ "\n"
                + "On Vault options, you can use \"set\" to set the VIP group or \"add\" to add VIP group to player.\n"
                + "*Using Vault you don't need to set any permission plugin command to set groups, Vault will do all this jobs.\n"
                + "\n");
				
        if (!plugin.getConfig().contains("groups")){
        	plugin.getConfig().set("groups.vip1.essentials-kit", "vip1");
        	plugin.getConfig().set("groups.vip1.commands", Arrays.asList("broadcast &aThe player &6{p} &ahas acquired your &6{vip} &afor &6{days} &adays","give {p} minecraft:diamond 10", "eco give {p} 10000"));
        	plugin.getConfig().set("groups.vip1.cmdChances.50", Arrays.asList("give {p} minecraft:diamond_block 5"));
        	plugin.getConfig().set("groups.vip1.cmdChances.30", Arrays.asList("give {p} minecraft:mob_spawner 1"));
            plugin.getConfig().set("groups.vip1.run-on-vip-finish", Arrays.asList("broadcast [Example message from PixelVip on run-on-vip-finish] The vip of {p} (Vip {vip}) has ended and now is back to {playergroup}!"));
        } 
                
        //database
        plugin.getConfig().set("configs.database.type", getObj("file","configs.database.type"));
        plugin.getConfig().set("configs.database.mysql.host", getObj("jdbc:mysql://localhost:3306/","configs.database.mysql.host"));
        plugin.getConfig().set("configs.database.mysql.db-name", getObj("pixelvip","configs.database.mysql.db-name"));
        plugin.getConfig().set("configs.database.mysql.username", getObj("user","configs.database.mysql.username"));
        plugin.getConfig().set("configs.database.mysql.password", getObj("pass","configs.database.mysql.password"));

        plugin.getConfig().set("configs.database.mysql.keys.table-name", getObj("pixelvip_keys","configs.database.mysql.keys.table-name"));
        plugin.getConfig().set("configs.database.mysql.keys.columns.key", getObj("col_key","configs.database.mysql.keys.columns.key"));
        plugin.getConfig().set("configs.database.mysql.keys.columns.group", getObj("col_group","configs.database.mysql.keys.columns.group"));        
        plugin.getConfig().set("configs.database.mysql.keys.columns.duration", getObj("col_duration","configs.database.mysql.keys.columns.duration"));
        plugin.getConfig().set("configs.database.mysql.keys.columns.uses", getObj("col_uses","configs.database.mysql.keys.columns.uses"));
        plugin.getConfig().set("configs.database.mysql.keys.columns.cmds", getObj("col_cmds","configs.database.mysql.keys.columns.cmds"));        
        plugin.getConfig().set("configs.database.mysql.keys.columns.info", getObj("col_info","configs.database.mysql.keys.columns.info"));
        plugin.getConfig().set("configs.database.mysql.keys.columns.comments", getObj("col_comments","configs.database.mysql.keys.columns.comments"));
        
        plugin.getConfig().set("configs.database.mysql.vips.table-name", getObj("pixelvip_vips","configs.database.mysql.vips.table-name"));
        plugin.getConfig().set("configs.database.mysql.vips.columns.uuid", getObj("col_uuid","configs.database.mysql.vips.columns.uuid"));
        plugin.getConfig().set("configs.database.mysql.vips.columns.vip", getObj("col_vip","configs.database.mysql.vips.columns.vip"));
        plugin.getConfig().set("configs.database.mysql.vips.columns.playerGroup", getObj("col_playerGroup","configs.database.mysql.vips.columns.playerGroup"));
        plugin.getConfig().set("configs.database.mysql.vips.columns.duration", getObj("col_duration","configs.database.mysql.vips.columns.duration"));
        plugin.getConfig().set("configs.database.mysql.vips.columns.nick", getObj("col_nick","configs.database.mysql.vips.columns.nick"));
        plugin.getConfig().set("configs.database.mysql.vips.columns.expires-on-exact", getObj("col_expires","configs.database.mysql.vips.columns.expires-on-exact"));
        plugin.getConfig().set("configs.database.mysql.vips.columns.active", getObj("col_active","configs.database.mysql.vips.columns.active"));
        plugin.getConfig().set("configs.database.mysql.vips.columns.kits", getObj("col_kits","configs.database.mysql.vips.columns.kits"));
        plugin.getConfig().set("configs.database.mysql.vips.columns.comments", getObj("col_comments","configs.database.mysql.vips.columns.comments"));
        
        plugin.getConfig().set("configs.database.mysql.transactions.table-name", getObj("pixelvip_transactions","configs.database.mysql.transactions.table-name"));
        plugin.getConfig().set("configs.database.mysql.transactions.columns.idt", getObj("col_idt","configs.database.mysql.transactions.columns.idt"));
        plugin.getConfig().set("configs.database.mysql.transactions.columns.nick", getObj("col_nick","configs.database.mysql.transactions.columns.nick"));
        //end database
        
        try {
			plugin.serv.spigot();
			plugin.getConfig().set("configs.spigot.clickKeySuggest", getObj(true ,"configs.spigot.clickKeySuggest"));
		} catch (NoSuchMethodError e) {
			plugin.getConfig().set("configs.spigot.clickKeySuggest", getObj(false ,"configs.spigot.clickKeySuggest"));
		}
        plugin.getConfig().set("configs.spigot.clickSuggest", getObj("/usekey {key}", "configs.spigot.clickSuggest"));
        
        plugin.getConfig().set("configs.key-size", getObj(10,"configs.key-size"));

		plugin.getConfig().set("configs.Vault.use", getObj(true ,"configs.Vault.use"));
		plugin.getConfig().set("configs.Vault.mode", getObj("set" ,"configs.Vault.mode"));

        plugin.getConfig().set("configs.cmdToReloadPermPlugin", getObj("pex reload","configs.cmdToReloadPermPlugin"));
        plugin.getConfig().set("configs.cmdOnRemoveVip", getObj("","configs.cmdOnRemoveVip"));
        plugin.getConfig().set("configs.commandsToRunOnVipFinish", getObj(Arrays.asList("nick {p} off"),"configs.commandsToRunOnVipFinish"));
        plugin.getConfig().set("configs.commandsToRunOnChangeVip", getObj(new ArrayList<String>(),"configs.commandsToRunOnChangeVip"));
        plugin.getConfig().set("configs.queueCmdsForOfflinePlayers", getObj(false,"configs.queueCmdsForOfflinePlayers"));
        List<String> worlds = new ArrayList<>();
        for (World w:Bukkit.getWorlds()){
        	worlds.add(w.getName());
        }
        plugin.getConfig().set("configs.worldCmdsAllowed", getObj(worlds, "configs.worldCmdsAllowed"));
        plugin.getConfig().set("bungee.enableSync", getObj(false,"bungee.enableSync"));
        plugin.getConfig().set("bungee.serverID", getObj("server1","bungee.serverID"));
        
        
        plugin.getConfig().set("apis.pagseguro.use", getObj(false,"apis.pagseguro.use"));
        plugin.getConfig().set("apis.pagseguro.email", getObj("your@email.com","apis.pagseguro.email"));
        plugin.getConfig().set("apis.pagseguro.token", getObj("yourtoken","apis.pagseguro.token"));
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyy");
        sdf.format(Calendar.getInstance().getTime());
        plugin.getConfig().set("apis.pagseguro.ignoreOldest", getObj(sdf.format(Calendar.getInstance().getTime()),"apis.pagseguro.ignoreOldest"));
                
        plugin.getConfig().set("apis.paypal.use", getObj(false,"apis.paypal.use"));
        
        if (!plugin.getConfig().contains("apis.commandsIds")){
        	plugin.getConfig().set("apis.commandIds.1", getObj("darvip {p} Vip1 15","apis.commandIds.1"));
            plugin.getConfig().set("apis.commandIds.2", getObj("silk give {p} iron_golem 2","apis.commandIds.2"));
            plugin.getConfig().set("apis.commandIds.3", getObj("eco give {p} 10000","apis.commandIds.3"));
        }
        
        //strings
        plugin.getConfig().set("strings._pluginTag", getObj("&7[&6PixelVip&7] ","strings._pluginTag"));	
        plugin.getConfig().set("strings.noPlayersByName", getObj("&cTheres no players with this name!","strings.noPlayersByName"));	
        plugin.getConfig().set("strings.onlyPlayers", getObj("&cOnly players ca use this command!","strings.onlyPlayers"));	
        plugin.getConfig().set("strings.noKeys", getObj("&aTheres no available keys! Use &6/newkey &aor &6/newikey &ato generate one.","strings.noKeys"));
        plugin.getConfig().set("strings.listKeys", getObj("&aList of Keys:","strings.listKeys"));
        plugin.getConfig().set("strings.listItemKeys", getObj("&aList of Item Keys:","strings.listItemKeys"));
        plugin.getConfig().set("strings.vipInfoFor", getObj("&aVip info for ","strings.vipInfoFor"));
        plugin.getConfig().set("strings.playerNotVip", getObj("&cThis player(or you) is not VIP!","strings.playerNotVip"));
        plugin.getConfig().set("strings.moreThanZero", getObj("&cThis number need to be more than 0","strings.moreThanZero"));
        plugin.getConfig().set("strings.keyGenerated", getObj("&aGenerated a key with the following:","strings.keyGenerated"));
        plugin.getConfig().set("strings.keySendTo", getObj("&aYou received a key with the following:","strings.keySendTo"));        
        plugin.getConfig().set("strings.invalidKey", getObj("&cThis key is invalid or not exists!","strings.invalidKey"));
        plugin.getConfig().set("strings.vipActivated", getObj("&aVip activated with success:","strings.vipActivated"));
        plugin.getConfig().set("strings.usesLeftActivation", getObj("&bThis key can be used for more: &6{uses} &btimes.","strings.usesLeftActivation"));
        plugin.getConfig().set("strings.activeVip", getObj("&b- Vip: &6{vip}","strings.activeVip"));
        plugin.getConfig().set("strings.activeDays", getObj("&b- Days: &6{days} &bdays","strings.activeDays"));	
        plugin.getConfig().set("strings.timeLeft", getObj("&b- Time left: &6","strings.timeLeft"));	        
        plugin.getConfig().set("strings.totalTime", getObj("&b- Days: &6","strings.totalTime"));
        plugin.getConfig().set("strings.timeKey", getObj("&b- Key: &6","strings.timeKey"));	  
        plugin.getConfig().set("strings.hoverKey", getObj("&7&o(Click to get the Key)&r","strings.hoverKey"));	
        plugin.getConfig().set("strings.timeGroup", getObj("&b- Vip: &6","strings.timeGroup"));
        plugin.getConfig().set("strings.timeActive", getObj("&b- In Use: &6","strings.timeActive"));
        plugin.getConfig().set("strings.infoUses", getObj("&b- Uses left: &6","strings.infoUses"));
        plugin.getConfig().set("strings.activeVipSetTo", getObj("&aYour active VIP is ","strings.activeVipSetTo"));
        plugin.getConfig().set("strings.noGroups", getObj("&cNo groups with name &6","strings.noGroups"));
        plugin.getConfig().set("strings.days", getObj(" &bdays","strings.days"));
        plugin.getConfig().set("strings.hours", getObj(" &bhours","strings.hours"));
        plugin.getConfig().set("strings.minutes", getObj(" &bminutes","strings.minutes"));
        plugin.getConfig().set("strings.and", getObj(" &band","strings.and"));
        plugin.getConfig().set("strings.vipEnded", getObj(" &bYour vip &6{vip} &bhas ended. &eWe hope you enjoyed your Vip time &a:D","strings.vipEnded"));
        plugin.getConfig().set("strings.lessThan", getObj("&6Less than one minute to end your vip...","strings.lessThan"));
        plugin.getConfig().set("strings.vipsRemoved", getObj("&aVip(s) of player removed with success!","strings.vipsRemoved"));
        plugin.getConfig().set("strings.vipSet", getObj("&aVip set with success for this player!","strings.vipSet"));
        plugin.getConfig().set("strings.sync-groups", getObj("&aGroup configs send to all servers!","strings.sync-groups"));
        plugin.getConfig().set("strings.list-of-vips", getObj("&aList of active VIPs: ","strings.list-of-vips"));
        plugin.getConfig().set("strings.vipAdded", getObj("&aVip added with success for this player!","strings.vipAdded"));
        plugin.getConfig().set("strings.item", getObj("&a-- Item: &b","strings.item"));
        plugin.getConfig().set("strings.itemsGiven", getObj("&aGiven {items} item(s) using a key.","strings.itemsGiven"));
        plugin.getConfig().set("strings.itemsAdded", getObj("&aItem(s) added to key:","strings.itemsAdded"));
        plugin.getConfig().set("strings.keyRemoved", getObj("&aKey removed with success: &b","strings.keyRemoved"));
        plugin.getConfig().set("strings.noKeyRemoved", getObj("&cTheres no keys to remove!","strings.noKeyRemoved"));
        plugin.getConfig().set("strings.cmdNotAllowedWorld", getObj("&cThis command is not allowed in this world!","strings.cmdNotAllowedWorld"));
        plugin.getConfig().set("strings.true", getObj("&atrue","strings.true"));
        plugin.getConfig().set("strings.false", getObj("&cfalse","strings.false"));
        plugin.getConfig().set("strings.reload", getObj("&aPixelvip reloaded with success!","strings.reload"));
        
        plugin.getConfig().set("strings.pagseguro.waiting", getObj("&cPagSeguro: Your purchase has not yet been approved!","strings.pagseguro.waiting"));
        plugin.getConfig().set("strings.pagseguro.codeused", getObj("&cPagSeguro: This code has already been used!","strings.pagseguro.codeused"));
        plugin.getConfig().set("strings.pagseguro.expired", getObj("&cPagSeguro: This code has expired!","strings.pagseguro.expired"));
        plugin.getConfig().set("strings.pagseguro.noitems", getObj("&cPagSeguro: No items delivered. Contact an administrator to help you!","strings.pagseguro.noitems"));
        
        //init database
        reloadVips();
        
        /*---------------------------------------------------------*/
        //move vips to new file if is in config.yml
        
        if (plugin.getConfig().getConfigurationSection("activeVips") != null){
        	plugin.getPVLogger().warning("Active Vips moved to file 'vips.yml'");
        	plugin.getConfig().getConfigurationSection("activeVips").getKeys(false).forEach((group->{
        		plugin.getConfig().getConfigurationSection("activeVips."+group).getKeys(false).forEach((id)->{
        			dataManager.addRawVip(group, id, 
        					Arrays.asList(plugin.getConfig().getString("activeVips."+group+"."+id+".playerGroup").split(",")),
        					plugin.getConfig().getLong("activeVips."+group+"."+id+".duration"), 
        					plugin.getConfig().getString("activeVips."+group+"."+id+".nick"), 
        					plugin.getConfig().getString("activeVips."+group+"."+id+".expires-on-exact"));
        			dataManager.setVipActive(id, group, plugin.getConfig().getBoolean("activeVips."+group+"."+id+".active"));
        		});
        	}));
        	
        	plugin.getConfig().set("activeVips", null);
        	saveVips();
        }
        
        /*---------------------------------------------------------*/
        
        /*---------------------------------------------------------*/
        //move keys to new file if is in config.yml
        
        if (plugin.getConfig().getConfigurationSection("keys") != null){
        	plugin.getPVLogger().warning("keys moved to file 'keys.yml'");
        	plugin.getConfig().getConfigurationSection("keys").getKeys(false).forEach((key)->{
        		dataManager.addRawKey(key, 
        				plugin.getConfig().getString("keys."+key+".group"), 
        				plugin.getConfig().getLong("keys."+key+".duration"), 
        				plugin.getConfig().getInt("keys."+key+".uses"));
        	});
        	
        	plugin.getConfig().set("keys", null);
        	saveKeys();
        }
        
        if (plugin.getConfig().getConfigurationSection("itemKeys") != null){
        	plugin.getPVLogger().warning("itemKeys moved to file 'keys.yml'");
        	plugin.getConfig().getConfigurationSection("itemKeys").getKeys(false).forEach((key)->{
        		dataManager.addRawItemKey(key, plugin.getConfig().getStringList("itemKeys."+key+".cmds"));
        	});
        	
        	plugin.getConfig().set("itemKeys", null);
        	saveKeys();
        }
        
        /*---------------------------------------------------------*/
                
        plugin.saveConfig();
	}
	
	public void reloadVips() {
		if (dataManager != null){
			dataManager.closeCon();
		}
		if (plugin.getConfig().getString("configs.database.type").equalsIgnoreCase("mysql")){
        	dataManager = new PVDataMysql(plugin);
        } else {
        	dataManager = new PVDataFile(plugin);
        }
	}	
	
	public boolean transExist(String trans){
		return dataManager.transactionExist(trans);
	}
	
	public void addTrans(String trans, String player){
		dataManager.addTras(trans, player);
	}
	
	public void removeTrans(String trans){
		dataManager.removeTrans(trans);
	}
	
	public HashMap<String, String> getAllTrans(){
		return dataManager.getAllTrans();
	}
	
	public boolean worldAllowed(World w){
		return plugin.getConfig().getStringList("configs.worldCmdsAllowed").contains(w.getName());
	}
	
	public List<String> getItemKeyCmds(String key){
		return dataManager.getItemKeyCmds(key);
	}

	public void saveKeys(){
		dataManager.saveKeys();
	}
	
	public void saveVips(){
		dataManager.saveVips();
	}
	
	public void closeCon(){
		if (dataManager != null){
			dataManager.closeCon();
		}
	}
	
	public boolean isVipActive(String vip, String id){
		return dataManager.isVipActive(id, vip);
	}
	
	public boolean bungeeSyncEnabled(){
		return getBoolean(false, "bungee.enableSync");
	}
	
	public boolean queueCmds(){
		return getBoolean(false, "configs.queueCmdsForOfflinePlayers");
	}
	
	/*
	 * For BungeeCord
	 */
	public void setVipActive(String uuid, String vip, boolean active){
		dataManager.setVipActive(uuid, vip, active);
	}
	
	public List<String> getQueueCmds(String uuid){
		List<String> cmds = new ArrayList<>();
		if (plugin.getConfig().contains("joinCmds."+uuid+".cmds")){
			cmds.addAll(plugin.getConfig().getStringList("joinCmds."+uuid+".cmds"));
		}
		if (plugin.getConfig().contains("joinCmds."+uuid+".chanceCmds")){
			cmds.addAll(plugin.getConfig().getStringList("joinCmds."+uuid+".chanceCmds"));
		}
		plugin.getConfig().set("joinCmds."+uuid, null);
		plugin.saveConfig();
		return cmds;
	}
	
	private void setJoinCmds(String uuid, List<String> cmds, List<String> chanceCmds) {
		plugin.getConfig().set("joinCmds."+uuid+".cmds", cmds);
		plugin.getConfig().set("joinCmds."+uuid+".chanceCmds", chanceCmds);
		plugin.saveConfig();
	}
	
	public void addKey(String key, String group, long millis, int uses){
		dataManager.addRawKey(key, group, millis, uses);
		saveConfigAll();
	}
	
	public void addItemKey(String key, List<String> cmds){
		cmds.addAll(dataManager.getItemKeyCmds(key));
		dataManager.addRawItemKey(key, cmds);
		saveConfigAll();
	}
	
	private void saveConfigAll(){
		saveVips();
		saveKeys();
		plugin.saveConfig();
		plugin.getPVBungee().sendBungeeSync();
	}
	
	public boolean delItemKey(String key){	
		if (dataManager.getItemListKeys().contains(key)){
			dataManager.removeItemKey(key);
			saveConfigAll();
			return true;
		} else {
			return false;
		}
	}
	
	public boolean delKey(String key, int uses){	
		if (dataManager.getListKeys().contains(key)){
			if (uses <= 1){
				dataManager.removeKey(key);
			} else {
				dataManager.setKeyUse(key, uses-1);
			}
			saveConfigAll();
			return true;
		} else {
			return false;
		}
	}
	
	public boolean activateVip(OfflinePlayer p, String key, String group, long days, String pname) {
		boolean hasItemkey = key != null && dataManager.getItemListKeys().contains(key);
		if (hasItemkey){		
			StringBuilder cmdsBuilder = new StringBuilder();
			List<String> cmds = dataManager.getItemKeyCmds(key);
			for (String cmd:cmds){
				cmdsBuilder.append(cmd).append(", ");
				plugin.serv.getScheduler().runTaskLater(plugin, () -> {
                    String cmdf = cmd.replace("{p}", p.getName());
                    if (p.isOnline()){
plugin.getUtil().ExecuteCmd(cmdf);
                    }
                }, delay*2);
				delay++;
			}		
			dataManager.removeItemKey(key);
			saveConfigAll();
			
			p.getPlayer().sendMessage(plugin.getUtil().toColor(getLang("_pluginTag","itemsGiven").replace("{items}", cmds.size()+"")));
			
			String cmdBuilded = cmdsBuilder.toString();
			plugin.addLog("ItemKey | "+p.getName()+" | "+key+" | Cmds: "+cmdBuilded.substring(0, cmdBuilded.length()-2));
		}
		if (dataManager.getKeyInfo(key).length == 3){
			String[] keyinfo = dataManager.getKeyInfo(key);
			int uses = Integer.parseInt(keyinfo[2]);
			
			delKey(key, uses);
			
			p.getPlayer().sendMessage(plugin.getUtil().toColor("&b---------------------------------------------"));
			if (uses-1 > 0){				
				p.getPlayer().sendMessage(plugin.getUtil().toColor(getLang("_pluginTag","usesLeftActivation").replace("{uses}", ""+(uses-1))));
			}			
			enableVip(p, keyinfo[0], new Long(keyinfo[1]), pname, key);				
			return true;
		} else if (!group.equals("")){			
			enableVip(p, group, plugin.getUtil().dayToMillis(days), pname, key);
			return true;
		} else {
			if (!hasItemkey){
				p.getPlayer().sendMessage(plugin.getUtil().toColor(getLang("_pluginTag","invalidKey")));	
				return false;
			}
			return true;
		}		
	}
	
	/*
	 * For bungeecord
	 */
	public void addVip(String group, String uuid, String pgroup, long duration, String nick, String expires){
		dataManager.addRawVip(group, uuid, new ArrayList<>(Arrays.asList(pgroup.split(","))), duration, nick, expires);
	}
	
	/** Return the key info: <p>
	 * [0] = Vip Group | [1] = Duration in millis | [2] = Uses 
	 * @param key - The key to get info
	 * @return {@code String[]} - Arrays with the key info.
	 */
	public String[] getKeyInfo(String key){
		return dataManager.getKeyInfo(key);
	}
	
	private void enableVip(OfflinePlayer p, String group, long durMillis, String pname, String key){		
		int count = 0;
		long durf = durMillis;	
		for (String[] k:getVipInfo(p.getUniqueId().toString())){
			if (k[1].equals(group)){	
				durMillis += new Long(k[0]);
				count++;
				break;
			}
		}			
		
		if (count == 0){
			durMillis += plugin.getUtil().getNowMillis();
		}
		
		List<String> pGroups = plugin.getPerms().getGroupsList(p);
        List<String> pdGroup = pGroups;
		List<String[]> vips = getVipInfo(p.getUniqueId().toString());
		if (!vips.isEmpty()){
			pGroups = new ArrayList<>(Arrays.asList(vips.get(0)[2].split(",")));
		}
		
		
		List<String> normCmds = new ArrayList<>();
		List<String> chanceCmds = new ArrayList<>();

		//run command from vip
		plugin.getConfig().getStringList("groups."+group+".commands").forEach((cmd)->{
			plugin.serv.getScheduler().runTaskLater(plugin, () -> {
                String cmdf = cmd.replace("{p}", p.getName())
                        .replace("{vip}", group)
                        .replace("{playergroup}", pdGroup.get(0))
                        .replace("{days}", String.valueOf(plugin.getUtil().millisToDay(durf)));
                if (p.isOnline()){
                    plugin.getUtil().ExecuteCmd(cmdf);
                } else {
                    normCmds.add(cmdf);
                }
            }, delay*2);
			delay++;
		});		
		
		//run command chances from vip		
		getCmdChances(group).forEach((chanceString)->{
			
			int chance = Integer.parseInt(chanceString);
			double rand = Math.random() * 100;
			
			//test chance
			if (rand <= chance){
				plugin.getConfig().getStringList("groups."+group+".cmdChances."+chanceString).forEach((cmd)->{
					plugin.serv.getScheduler().runTaskLater(plugin, () -> {
                        String cmdf = cmd.replace("{p}", p.getName())
                                .replace("{vip}", group)
                                .replace("{playergroup}", pdGroup.get(0))
                                .replace("{days}", String.valueOf(plugin.getUtil().millisToDay(durf)));
                        if (p.isOnline()){
                            plugin.getUtil().ExecuteCmd(cmdf);
                        } else {
                            chanceCmds.add(cmdf);
                        }
                    }, delay*2);
					delay++;
				});
			}						
		});
		
		if (queueCmds() && (normCmds.size() > 0 || chanceCmds.size() > 0)){	
			plugin.serv.getScheduler().runTaskLater(plugin, () -> {
                plugin.getLogger().info("Queued cmds for player "+p.getName()+" to run on join.");
                setJoinCmds(p.getUniqueId().toString(), normCmds, chanceCmds);
            }, delay*2);
		}		
		
		delay = 0;
		
		dataManager.addRawVip(group, p.getUniqueId().toString(), 
				pGroups,
				durMillis, 
				pname, 
				plugin.getUtil().expiresOn(durMillis));
				
		setActive(p.getUniqueId().toString(),group,pdGroup);
		
		if (p.isOnline()){
			p.getPlayer().sendMessage(plugin.getUtil().toColor(getLang("_pluginTag","vipActivated")));
			p.getPlayer().sendMessage(plugin.getUtil().toColor(getLang("activeVip").replace("{vip}", group)));
			p.getPlayer().sendMessage(plugin.getUtil().toColor(getLang("activeDays").replace("{days}", String.valueOf(plugin.getUtil().millisToDay(durf)))));
			p.getPlayer().sendMessage(plugin.getUtil().toColor("&b---------------------------------------------"));
		}		
		plugin.addLog("EnableVip | key: "+key+" | "+p.getName()+" | "+group+" | Expires on: "+plugin.getUtil().expiresOn(durMillis));
	}
	
	public void setVip(String uuid, String group, long durMillis, String pname){
		int count = 0;
		for (String[] k:getVipInfo(uuid)){
			if (k[1].equals(group)){					
				durMillis += new Long(k[0]);
				count++;
				break;
			}
		}		
		
		if (count == 0){
			durMillis += plugin.getUtil().getNowMillis();
		}
		
		List<String> pGroups = new ArrayList<>();
		OfflinePlayer p = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
		if (p.getName() != null){
			pGroups = plugin.getPerms().getGroupsList(p);
		}
		List<String[]> vips = getVipInfo(uuid);
		if (!vips.isEmpty()){
			pGroups = new ArrayList<>(Collections.singletonList(vips.get(0)[2]));
		}				
		
		dataManager.addRawVip(group, uuid, 
				pGroups,
				durMillis, 
				pname, 
				plugin.getUtil().expiresOn(durMillis));
		setActive(uuid,group,pGroups);
		
		plugin.addLog("SetVip | "+p.getName()+" | "+group+" | Expires on: "+plugin.getUtil().expiresOn(durMillis));
	}
	
	public void setActive(String uuid, String group, List<String> pgroup){
		String newVip = group;
		String oldVip = pgroup.stream().anyMatch(str -> getGroupList().contains(str)) ? pgroup.stream().filter(str -> getGroupList().contains(str)).findFirst().get() : "";
		for (String glist:getGroupList()){			
			if (dataManager.containsVip(uuid, glist)){
				if (glist.equals(group)){
					if (!dataManager.isVipActive(uuid, glist)){
						newVip = glist;
						long total = dataManager.getVipDuration(uuid, glist)+plugin.getUtil().getNowMillis();
						dataManager.setVipDuration(uuid, glist, total);
					}
					dataManager.setVipActive(uuid, glist, true);					
				} else {	
					if (dataManager.isVipActive(uuid, glist)){
						oldVip = glist;
						long total = dataManager.getVipDuration(uuid, glist)-plugin.getUtil().getNowMillis();
						dataManager.setVipDuration(uuid, glist, total);
					}
					dataManager.setVipActive(uuid, glist, false);	
				}
			}
		}	
		//change kits
		changeVipKit(uuid, oldVip, newVip);
		
		OfflinePlayer p = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
		if (p.getName() != null){
			runChangeVipCmds(p, newVip, oldVip);			
		}		
		saveConfigAll();
	}
		
	public void runChangeVipCmds(OfflinePlayer p, String newVip, String oldVip){
		for (String cmd:plugin.getConfig().getStringList("configs.commandsToRunOnChangeVip")){
			if (p.getName() == null){break;}
			
			String cmdf = cmd.replace("{p}", p.getName());
			if (!oldVip.equals("") && cmdf.contains("{oldvip}")){
				plugin.serv.getScheduler().runTaskLater(plugin, () -> plugin.getUtil().ExecuteCmd(cmdf.replace("{oldvip}", oldVip)), delay*5);
				delay++;
			} else
			if (!newVip.equals("") && cmdf.contains("{newvip}")){
				plugin.serv.getScheduler().runTaskLater(plugin, () -> plugin.getUtil().ExecuteCmd(cmdf.replace("{newvip}", newVip)), delay*5);
				delay++;
			} else {
				plugin.serv.getScheduler().runTaskLater(plugin, () -> plugin.getUtil().ExecuteCmd(cmdf), delay*5);
				delay++;
			}
		}
		if (plugin.getConfig().getBoolean("configs.Vault.use")){
            plugin.serv.getScheduler().runTaskLater(plugin, () -> {
                if (oldVip != null && !oldVip.isEmpty() && !oldVip.equals(newVip)){
                    plugin.getPerms().removeGroup(p.getUniqueId().toString(), oldVip);
                }
                if (plugin.getConfig().getString("configs.Vault.mode").equalsIgnoreCase("set")){
                    plugin.getPerms().setGroup(p.getUniqueId().toString(), newVip);
                }
                if (plugin.getConfig().getString("configs.Vault.mode").equalsIgnoreCase("add")){
                    plugin.getPerms().addGroup(p.getUniqueId().toString(), newVip);
                }
            }, delay*2);
            delay++;
		} else {
			reloadPerms();
		}
	}
	
	private void changeVipKit(String uuid, String oldVip, String newVip){
		if (plugin.ess != null){
		    long now = System.currentTimeMillis();
			String oldKit = this.getString("", "groups."+oldVip+".essentials-kit");	
			User user = plugin.ess.getUser(UUID.fromString(uuid));
			if (!oldKit.isEmpty() && user != null){				
				long oldTime = user.getKitTimestamp(oldKit.toLowerCase(Locale.ENGLISH));
				dataManager.setVipKitCooldown(uuid, oldVip, now - oldTime);
			}
			
			String newKit = this.getString("", "groups."+newVip+".essentials-kit");			
			if (!newKit.isEmpty() && user != null){
				long newTime = dataManager.getVipCooldown(uuid, newVip);
				if (newTime > 0){
					user.setKitTimestamp(newKit.toLowerCase(Locale.ENGLISH), now - newTime);
				}
			}
		}
	}
	
	void removeVip(String uuid, String pname, String group){
		plugin.addLog("RemoveVip | "+pname+" | "+group);
		
		dataManager.removeVip(uuid, group);
		plugin.serv.getScheduler().runTaskLater(plugin, () -> plugin.getUtil().ExecuteCmd(getString("","configs.cmdOnRemoveVip").replace("{p}", Optional.<String>ofNullable(pname).get()).replace("{vip}", group)),delay*5);
		delay++;
		
		if (plugin.getConfig().getBoolean("configs.Vault.use")){
            plugin.serv.getScheduler().runTaskLater(plugin, () -> plugin.getPerms().removeGroup(uuid, group), delay*2);
            delay++;
		} 
	}
	
	public void removeVip(String uuid, Optional<String> optg){
		List<String[]> vipInfo = getVipInfo(uuid);		
		boolean id = false;
		String nick = "";
		List<String> oldGroup = new ArrayList<>();
        String vipGroup = "";
        if (vipInfo.size() > 0){
			for (String[] key:vipInfo){
				vipGroup = key[1];
				oldGroup = Arrays.asList(key[2].split(","));
				nick = key[4];
				if (vipInfo.size() > 1 ){
					if (optg.isPresent()){
						if (optg.get().equals(vipGroup)){
							removeVip(uuid, nick, vipGroup);
						} else if (!id){
							setActive(uuid, vipGroup, new ArrayList<>());
							id = true;
						}			    						
	    			} else {	    				
    					removeVip(uuid, nick, vipGroup);
	    			}
				} else {
					removeVip(uuid, nick, vipGroup);
				}		
			}			    			
		}

		//commands to run on vip finish
		if (getVipInfo(uuid).size() == 0){			
			for (String cmd:plugin.getConfig().getStringList("configs.commandsToRunOnVipFinish")){
				if (cmd == null || cmd.isEmpty() || cmd.contains("{vip}")){continue;}
				if (!oldGroup.isEmpty() && cmd.contains("{playergroup}")){
                    for (String group:oldGroup){
                        String cmdf = cmd.replace("{p}", nick).replace("{playergroup}", group);
                        plugin.serv.getScheduler().runTaskLater(plugin, () -> plugin.getUtil().ExecuteCmd(cmdf),1+delay*5);
                        delay++;
                    }
                } else {
                    String cmdf = cmd.replace("{p}", nick);
                    plugin.serv.getScheduler().runTaskLater(plugin, () -> plugin.getUtil().ExecuteCmd(cmdf),1+delay*5);
                    delay++;
                }
			}
		}

		//command to run from vip GROUP on finish
        for (String cmd:getCmdsToRunOnFinish(vipGroup)) {
            if (cmd == null || cmd.isEmpty()) continue;
            if (!oldGroup.isEmpty() && cmd.contains("{playergroup}")){
                for (String group:oldGroup){
                    String cmdf = cmd.replace("{p}", nick).replace("{playergroup}", group);
                    plugin.serv.getScheduler().runTaskLater(plugin, () -> plugin.getUtil().ExecuteCmd(cmdf),1+delay*5);
                    delay++;
                }
            } else {
                String cmdf = cmd.replace("{p}", nick);
                plugin.serv.getScheduler().runTaskLater(plugin, () -> plugin.getUtil().ExecuteCmd(cmdf),1+delay*5);
                delay++;
            }
        }

        //use vault to add back oldgroup
		if (plugin.getConfig().getBoolean("configs.Vault.use")){
            for (String group:oldGroup){
                plugin.serv.getScheduler().runTaskLater(plugin, () -> plugin.getPerms().addGroup(uuid, group),1+delay*5);
                delay++;
            }
		} else {
			reloadPerms();
		}
		saveConfigAll();
	}
	
	public void reloadPerms(){
		plugin.serv.getScheduler().runTaskLater(plugin, () -> plugin.getUtil().ExecuteCmd(getString("","configs.cmdToReloadPermPlugin")), (1+delay)*10);
		delay=0;
	}
	
	public long getLong(int def, String node){
		return plugin.getConfig().getLong(node, def);
	}
	
	public int getInt(int def, String node){
		return plugin.getConfig().getInt(node, def);
	}
	
	public String getString(String def, String node){
		return plugin.getConfig().getString(node, def);
	}
	
	public boolean getBoolean(boolean def, String node){
		return plugin.getConfig().getBoolean(node, def);
	}
	
	public Object getObj(Object def, String node){
		return plugin.getConfig().get(node, def);
	}
	
	public String getLang(String... nodes){
		StringBuilder msg = new StringBuilder();
		for (String node:nodes){
			msg.append(getString("No strings with "+node, "strings."+node));
		}
		return msg.toString();
	}

	public boolean groupExists(String group) {
		return plugin.getConfig().contains("groups."+group);
	}

	public Set<String> getCmdChances(String vip) {
		if (plugin.getConfig().getConfigurationSection("groups."+vip+".cmdChances") != null){
			return plugin.getConfig().getConfigurationSection("groups."+vip+".cmdChances").getKeys(false);
		}
		return new HashSet<>();
	}

    public Set<String> getCmdsToRunOnFinish(String vip) {
        if (plugin.getConfig().getConfigurationSection("groups."+vip+".run-on-vip-end") != null){
            return plugin.getConfig().getConfigurationSection("groups."+vip+".run-on-vip-end").getKeys(false);
        }
        return new HashSet<>();
    }
	
	public Set<String> getListKeys() {
		return dataManager.getListKeys();
	}
	
	public Set<String> getItemListKeys() {
		return dataManager.getItemListKeys();
	}
	
	public Set<String> getGroupList(){
		if (plugin.getConfig().getConfigurationSection("groups") != null){
			return plugin.getConfig().getConfigurationSection("groups").getKeys(false);
		}
		return new HashSet<>();
	}
	
	public HashMap<String,List<String[]>> getVipList(){		
		return dataManager.getActiveVipList();
	}
	
	/**Return player's vip info.<p>
	 * [0] = Duration, [1] = Vip Group, [2] = Player Group, [3] = Is Active, [4] = Player Nick
	 * @param puuid Player UUID as string.
	 * @return {@code List<String[5]>} or a empty list if theres no vip for player.
	 */
	public List<String[]> getVipInfo(String puuid){			
		return dataManager.getVipInfo(puuid);
	}
	
	
	/**Return player's vip info.<p>
	 * [0] = Duration, [1] = Vip Group, [2] = Player Group, [3] = Is Active, [4] = Player Nick
	 * @param playName Player UUID as string or nickname.
	 * @return {@code String[5]}
	 */
	public String[] getActiveVipInfo(String playName){
		String uuid;
		try{
			UUID.fromString(playName);
			uuid = playName;	
		} catch (IllegalArgumentException ex){
			uuid = getVipUUID(playName);			
		}
		for (String[] vips:getVipInfo(uuid)){
			if (vips[3].equals("true")){
				return vips;
			}
		}
		return new String[5];
	}

	/**Return all vip info.<p>
	 * @return {@code HashMap<String,List<String[]>>}<p>
	 * Key: Existing Group Name.<p>
	 * Value: [0] = Duration, [1] = Vip Group, [2] = Player Group, [3] = Is Active, [4] = Player Nick
	 */
	public HashMap<String, List<String[]>> getAllVips() {
		return dataManager.getAllVipList();
	}

	public String getVipUUID(String string) {
		return dataManager.getVipUUID(string);
	}
}
