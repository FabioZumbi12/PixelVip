package br.net.fabiozumbi12.pixelvip.sponge;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;

import com.google.common.reflect.TypeToken;

public class PVConfig {	
		
	private CommentedConfigurationNode config;
	
	private CommentedConfigurationNode vipConfig;
	private ConfigurationLoader<CommentedConfigurationNode> vipManager;
	
	private PixelVip plugin;
	private int delay = 0;
	private Path defDir;
	
	public PVConfig(PixelVip plugin, Path defDir, File defConfig){
		this.defDir = defDir;
		this.plugin = plugin;
		try {
			Files.createDirectories(defDir);
			if (!defConfig.exists()){
				plugin.getLogger().info("Creating config file...");
				defConfig.createNewFile();
			}
						
			//--------- Create the vips file -------
			reloadVips();
			//--------------- loaded ---------------
			
	        config = plugin.getCfManager().load();	
	        
	        config.getNode("groups").setComment("Put your PEX(or your permission plugin) vip group names here. Case sensitive!");
	        if (!config.getNode("groups").hasMapChildren()){
	        	config.getNode("groups","vip1","commands").setComment(
	        			"Add the commands to run when the player use the key for activation \n"
		        		+ "You can use the variables:\n"
		        		+ "{p} = Player name, {vip} = Vip group, {days} = Vip days, {playergroup} = Player group before activate vip");
	        	config.getNode("groups","vip1","commands").setValue(Arrays.asList("broadcast &aThe player &6{p} &ahas acquired your &6{vip} &afor &6{days} &adays","give {p} minecraft:diamond 10", "eco give {p} 10000"));
	        	
	        	config.getNode("groups","vip1","cmdChances").setComment("Add commands here to give items to players based on chances.\n"
	        			+ "Use 1 - 100 for add chance commands.");
	        	config.getNode("groups","vip1","cmdChances","50").setValue(Arrays.asList("give {p} minecraft:diamond_block 5"));
	        	config.getNode("groups","vip1","cmdChances","30").setValue(Arrays.asList("give {p} minecraft:mob_spawner 1"));   
	        }
	        	        
	        config.getNode("configs","key-size").setComment("Sets the length of your vip keys.");
	        config.getNode("configs","key-size").setValue(getInt(10,"configs","key-size"));
	        
	        config.getNode("configs","cmdToReloadPermPlugin").setComment("Command to reload the permissions plugin after some action.");
	        config.getNode("configs","cmdToReloadPermPlugin").setValue(getString("pex reload","configs","cmdToReloadPermPlugin"));
	        
	        config.getNode("configs","cmdOnRemoveVip").setComment("Command to run when a vip is removed by command.");
	        config.getNode("configs","cmdOnRemoveVip").setValue(getString("pex user {p} parent delete group {vip}","configs","cmdOnRemoveVip"));
	        	
	        config.getNode("configs","commandsToRunOnVipFinish").setComment(
	        		"Run this commands when the vip of a player finish.\n"
	        		+ "Variables: {p} get the player name, {vip} get the actual vip, {playergroup} get the group before the player activate your vip.");
	        config.getNode("configs","commandsToRunOnVipFinish").setValue(getListString("configs","commandsToRunOnVipFinish"));
	        
	        config.getNode("configs","commandsToRunOnChangeVip").setComment(
	        		"Run this commands on player change your vip to other.\n"
	        		+ "Variables: {p} get the player name, {newvip} get the new vip, {oldvip} get the vip group before change.");
	        config.getNode("configs","commandsToRunOnChangeVip").setValue(getListString("configs","commandsToRunOnChangeVip"));
	        
	        config.getNode("keys").setComment("All available keys will be here.");
	        if (!config.getNode("keys").hasMapChildren()){
	        	config.getNode("keys").setValue(new ArrayList<String>());
	        }
	        
	        config.getNode("itemKeys").setComment("All available item keys will be here.");
	        if (!config.getNode("itemKeys").hasMapChildren()){
	        	config.getNode("itemKeys").setValue(new ArrayList<String>());
	        }
	        	        
	        if (getListString("configs","commandsToRunOnVipFinish").size() == 0){	        	
	        	config.getNode("configs","commandsToRunOnVipFinish")
	        	.setValue(Arrays.asList("pex user {p} parent delete group {vip}","pex user {p} parent add group {playergroup}"));
	        }   
	        
	        if (getListString("configs","commandsToRunOnChangeVip").size() == 0){	        	
	        	config.getNode("configs","commandsToRunOnChangeVip")
	        	.setValue(Arrays.asList("pex user {p} parent add group {newvip}","pex user {p} parent delete group {oldvip}"));
	        }
	        
	        //strings
	        config.getNode("strings","_pluginTag").setValue(getString("&7[&6PixelVip&7] ","strings","_pluginTag"));	
	        config.getNode("strings","noPlayersByName").setValue(getString("&cTheres no players with this name!","strings","noPlayersByName"));	
	        config.getNode("strings","onlyPlayers").setValue(getString("&cOnly players ca use this command!","strings","onlyPlayers"));	
	        config.getNode("strings","noKeys").setValue(getString("&aTheres no available keys! Use &6/newkey &ato generate one.","strings","noKeys"));
	        config.getNode("strings","listKeys").setValue(getString("&aList of Keys:","strings","listKeys"));
	        config.getNode("strings","listItemKeys").setValue(getString("&aList of Item Keys:","strings","listItemKeys"));
	        config.getNode("strings","vipInfoFor").setValue(getString("&aVip info for ","strings","vipInfoFor"));
	        config.getNode("strings","playerNotVip").setValue(getString("&cThis player(or you) is not VIP!","strings","playerNotVip"));
	        config.getNode("strings","moreThanZero").setValue(getString("&cThe days need to be more than 0","strings","moreThanZero"));
	        config.getNode("strings","noGroups").setValue(getString("&cTheres no groups with name ","strings","noGroups"));
	        config.getNode("strings","keyGenerated").setValue(getString("&aGenerated a key with the following:","strings","keyGenerated"));
	        config.getNode("strings","invalidKey").setValue(getString("&cThis key is invalid or not exists!","strings","invalidKey"));
	        config.getNode("strings","vipActivated").setValue(getString("&aVip activated with success:","strings","vipActivated"));
	        config.getNode("strings","usesLeftActivation").setValue(getString("&bThis key can be used for more: &6{uses} &btimes.","strings","usesLeftActivation"));
	        config.getNode("strings","activeVip").setValue(getString("&b- Vip: &6{vip}","strings","activeVip"));
	        config.getNode("strings","activeDays").setValue(getString("&b- Days: &6{days} &bdays","strings","activeDays"));	
	        config.getNode("strings","timeLeft").setValue(getString("&b- Time left: &6","strings","timeLeft"));	        
	        config.getNode("strings","totalTime").setValue(getString("&b- Days: &6","strings","totalTime"));
	        config.getNode("strings","timeKey").setValue(getString("&b- Key: &6","strings","timeKey"));	        	        
	        config.getNode("strings","timeGroup").setValue(getString("&b- Vip: &6","strings","timeGroup"));
	        config.getNode("strings","timeActive").setValue(getString("&b- In Use: &6","strings","timeActive"));
	        config.getNode("strings","infoUses").setValue(getString("&b- Uses left: &6","strings","infoUses"));
	        config.getNode("strings","activeVipSetTo").setValue(getString("&aYour active VIP is ","strings","activeVipSetTo"));
	        config.getNode("strings","noGroups").setValue(getString("&cNo groups with name &6","strings","noGroups"));
	        config.getNode("strings","days").setValue(getString(" &bdays","strings","days"));
	        config.getNode("strings","hours").setValue(getString(" &bhours","strings","hours"));
	        config.getNode("strings","minutes").setValue(getString(" &bminutes","strings","minutes"));
	        config.getNode("strings","and").setValue(getString(" &band","strings","and"));
	        config.getNode("strings","vipEnded").setValue(getString(" &bYour vip &6{vip} &bhas ended. &eWe hope you enjoyed your Vip time &a:D","strings","vipEnded"));
	        config.getNode("strings","lessThan").setValue(getString("&6Less than one minute to end your vip...","strings","lessThan"));
	        config.getNode("strings","vipsRemoved").setValue(getString("&aVip(s) of player removed with success!","strings","vipsRemoved"));
	        config.getNode("strings","vipSet").setValue(getString("&aVip set with success for this player!","strings","vipSet"));	        
	        config.getNode("strings","sync-groups").setValue(getString("&aGroup configs send to all servers!","strings","sync-groups"));
	        config.getNode("strings","list-of-vips").setValue(getString("&aList of active VIPs: ","strings","list-of-vips"));
	        config.getNode("strings","vipAdded").setValue(getString("&aVip added with success for this player!","strings","vipAdded"));
	        config.getNode("strings","item").setValue(getString("&a-- Item: &b","strings","item"));
	        config.getNode("strings","itemsGiven").setValue(getString("&aGiven {items} item(s) using a key.","strings","itemsGiven"));
	        config.getNode("strings","itemsAdded").setValue(getString("&aItem(s) added to key:","strings","itemsAdded"));
	        config.getNode("strings","keyRemoved").setValue(getString("&aKey removed with success: &b","strings","keyRemoved"));
	        config.getNode("strings","noKeyRemoved").setValue(getString("&cTheres no keys to remove!","strings","noKeyRemoved"));
			
	        /*---------------------------------------------------------*/
	        //move vips to new file if is in config.conf
	        vipConfig.getNode("activeVips").setComment("Your active vips will be listed here!");
	        if (config.getNode("activeVips").hasMapChildren()){
	        	vipConfig.getNode("activeVips").mergeValuesFrom(config.getNode("activeVips"));
	        	config.removeChild("activeVips");
	        	saveVips();
	        }
	        /*---------------------------------------------------------*/
	        
	        plugin.getCfManager().save(config);
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}		
	}
	
	public CommentedConfigurationNode getVips(){
		return vipConfig;
	}
	
	public void reloadVips(){
		File vipFile = new File(defDir+File.separator+"pixelvip"+File.separator+"vips.conf");
		if (!vipFile.exists()){
			try {
				vipFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		vipManager = HoconConfigurationLoader.builder().setFile(vipFile).build();		
		try {
			vipConfig = vipManager.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		saveVips();
	}
	
	public void saveVips(){
		try {
			vipManager.save(vipConfig);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void saveConfigAll(){
		try {
			saveVips();
			plugin.getCfManager().save(config);
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	public boolean queueCmds(){
		return getBoolean(false, "configs.queueCmdsForOfflinePlayers");
	}
	
	public List<String> getQueueCmds(String uuid) {
		List<String> cmds = new ArrayList<String>();
		if (config.getNode("joinCmds",uuid,"cmds").getValue() != null){
			try {
				cmds.addAll(config.getNode("joinCmds",uuid,"cmds").getList(TypeToken.of(String.class), new ArrayList<String>()));
			} catch (ObjectMappingException e) {
				e.printStackTrace();
			}
		}
		if (config.getNode("joinCmds",uuid,"chanceCmds").getValue() != null){
			try {
				cmds.addAll(config.getNode("joinCmds",uuid,"chanceCmds").getList(TypeToken.of(String.class), new ArrayList<String>()));
			} catch (ObjectMappingException e) {
				e.printStackTrace();
			}
		}
		config.getNode("joinCmds").removeChild("uuid");
		saveConfigAll();
		return cmds;
	}
	
	private void setJoinCmds(String uuid, List<String> cmds, List<String> chanceCmds) {
		config.getNode("joinCmds",uuid, "cmds").setValue(cmds);
		config.getNode("joinCmds",uuid, "chanceCmds").setValue(chanceCmds);
		saveConfigAll();
	}
	
	public void addKey(String key, String group, long millis, int uses){		
		config.getNode("keys",key, "group").setValue(group);
		config.getNode("keys",key, "duration").setComment("Duration in days: "+plugin.getUtil().millisToDay(millis));
		config.getNode("keys",key, "duration").setValue(String.valueOf(millis));
		config.getNode("keys",key, "uses").setValue(uses);
		saveConfigAll();
	}
	
	public void addItemKey(String key, List<String> cmds){		
		try {
			cmds.addAll(config.getNode("itemKeys",key,"cmds").getList(TypeToken.of(String.class), new ArrayList<String>()));
			config.getNode("itemKeys",key,"cmds").setValue(cmds);
		} catch (ObjectMappingException e) {
			e.printStackTrace();
		}
		saveConfigAll();
	}
	
	public boolean delItemKey(String key){	
		boolean removed = config.getNode("itemKeys").removeChild(key);
		saveConfigAll();
		return removed;
	}
	
	public boolean delKey(String key){		
		boolean removed = config.getNode("keys").removeChild(key);
		saveConfigAll();
		return removed;
	}
	
	public String[] getKeyInfo(String key){	
		if (config.getNode("keys",key).hasMapChildren()){
			return new String[]{getString("","keys",key,"group"),getString("","keys",key,"duration"),getString("","keys",key,"uses")};
		}
		return new String[0];
	}
	
	public boolean delKey(String key, int uses){	
		if (config.getNode("keys",key).hasMapChildren()){
			if (uses <= 1){
				config.getNode("keys").removeChild(key);
			} else {
				config.getNode("keys",key,"uses").setValue(uses-1);
			}
			saveConfigAll();
			return true;
		} else {
			return false;
		}
	}
	
	public CommandResult activateVip(User p, String key, String group, long days, String pname) throws CommandException {
		boolean hasItemkey = key != null && config.getNode("itemKeys",key).hasMapChildren();
		if (hasItemkey){
			try {
				StringBuilder cmdsBuilder = new StringBuilder();
				List<String> cmds = config.getNode("itemKeys",key,"cmds").getList(TypeToken.of(String.class), new ArrayList<String>());
				for (String cmd:cmds){
					cmdsBuilder.append(cmd+", ");
					Sponge.getGame().getScheduler().createSyncExecutor(plugin).schedule(new Runnable(){
						@Override
						public void run() {
							String cmdf = cmd.replace("{p}", p.getName());
							if (p.isOnline()){
								Sponge.getCommandManager().process(Sponge.getServer().getConsole(), cmdf);							
							} 					
						}
					}, delay*100, TimeUnit.MILLISECONDS);
					delay++;
				}		
				config.getNode("itemKeys").removeChild(key);
				saveConfigAll();
				
				p.getPlayer().get().sendMessage(plugin.getUtil().toText(getLang("_pluginTag","itemsGiven").replace("{items}", new String(cmds.size()+""))));		
				
				String cmdBuilded = cmdsBuilder.toString();
				plugin.addLog("ItemKey | "+p.getName()+" | "+key+" | Cmds: "+cmdBuilded.substring(0, cmdBuilded.length()-2));
			} catch (ObjectMappingException e) {
				e.printStackTrace();
			}			
		}
		if (getKeyInfo(key).length == 3){
			String[] keyinfo = getKeyInfo(key);
			int uses = Integer.parseInt(keyinfo[2]);
			
			delKey(key, uses);
							
			p.getPlayer().get().sendMessage(plugin.getUtil().toText("&b---------------------------------------------"));
			if (uses-1 > 0){				
				p.getPlayer().get().sendMessage(plugin.getUtil().toText(getLang("_pluginTag","usesLeftActivation").replace("{uses}", ""+(uses-1))));
			}	
			enableVip(p, keyinfo[0], new Long(keyinfo[1]), pname);
			return CommandResult.success();
		} else if (!group.equals("")){			
			enableVip(p, group, plugin.getUtil().dayToMillis(days), pname);
			return CommandResult.success();
		} else {
			if (!hasItemkey){
				throw new CommandException(plugin.getUtil().toText(plugin.getConfig().getLang("_pluginTag","invalidKey")));	
			}
			return CommandResult.success();
		}		
	}
	
	private void enableVip(User p, String group, long durMillis, String pname){		
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
		
		String pGroup = plugin.getPerms().getGroup(p);
		String pdGroup = pGroup;
		List<String[]> vips = getVipInfo(p.getUniqueId().toString());
		if (!vips.isEmpty()){
			pGroup = vips.get(0)[2];
		}
				
		List<String> normCmds = new ArrayList<String>();
		List<String> chanceCmds = new ArrayList<String>();
		
		//run command from vip
		getListString("groups",group,"commands").forEach((cmd)->{
			plugin.getGame().getScheduler().createSyncExecutor(plugin).schedule(new Runnable() {
				@Override
				public void run() {
					String cmdf = cmd.replace("{p}", p.getName())
							.replace("{vip}", group)
							.replace("{playergroup}", pdGroup)
							.replace("{days}", String.valueOf(plugin.getUtil().millisToDay(durf)));
					if (p.isOnline()){
						plugin.getGame().getCommandManager().process(Sponge.getServer().getConsole(),cmdf);
					} else {
						normCmds.add(cmdf);
					}
				}	
			}, delay*100, TimeUnit.MILLISECONDS);			
			delay++;
		});		
		
		//run command chances from vip	
		getCmdChances(group).forEach((node)->{
			String chanceString = String.valueOf(node.getKey());
			int chance = Integer.parseInt(chanceString);
			double rand = Math.random() * 100;
			
			//test chance
			if (rand <= chance){
				getListString("groups."+group+".cmdChances."+chanceString).forEach((cmd)->{
					Sponge.getScheduler().createSyncExecutor(plugin).schedule(new Runnable(){
						@Override
						public void run() {
							String cmdf = cmd.replace("{p}", p.getName())
									.replace("{vip}", group)
									.replace("{playergroup}", pdGroup)
									.replace("{days}", String.valueOf(plugin.getUtil().millisToDay(durf)));
							if (p.isOnline()){
								Sponge.getCommandManager().process(Sponge.getServer().getConsole(), cmdf);
							} else {
								chanceCmds.add(cmdf);
							}
						}
					}, delay*100, TimeUnit.MILLISECONDS);			
					delay++;
				});
			}						
		});
		
		if (queueCmds() && (normCmds.size() > 0 || chanceCmds.size() > 0)){	
			Sponge.getScheduler().createSyncExecutor(plugin).schedule(new Runnable(){
				@Override
				public void run() {
					plugin.getLogger().info("Queued cmds for player "+p.getName()+" to run on join.");
					setJoinCmds(p.getUniqueId().toString(), normCmds, chanceCmds);
				}
			}, delay*100, TimeUnit.MILLISECONDS);				
		}		
		
		delay = 0;
		
		vipConfig.getNode("activeVips",group,p.getUniqueId().toString(),"playerGroup").setValue(pGroup);
		vipConfig.getNode("activeVips",group,p.getUniqueId().toString(),"duration").setValue(durMillis);
		vipConfig.getNode("activeVips",group,p.getUniqueId().toString(),"nick").setValue(pname);
		vipConfig.getNode("activeVips",group,p.getUniqueId().toString(),"expires-on-exact").setValue(plugin.getUtil().expiresOn(durMillis));
		
		setActive(p,group,pdGroup);	
		
		if (p.isOnline()){
			p.getPlayer().get().sendMessage(plugin.getUtil().toText(plugin.getConfig().getLang("_pluginTag","vipActivated")));
			p.getPlayer().get().sendMessage(plugin.getUtil().toText(plugin.getConfig().getLang("activeVip").replace("{vip}", group)));
			p.getPlayer().get().sendMessage(plugin.getUtil().toText(plugin.getConfig().getLang("activeDays").replace("{days}", String.valueOf(plugin.getUtil().millisToDay(durf)))));
			p.getPlayer().get().sendMessage(plugin.getUtil().toText("&b---------------------------------------------"));
		}
		plugin.addLog("EnableVip | "+p.getName()+" | "+group+" | Expires on: "+plugin.getUtil().expiresOn(durMillis));
	}
	
	public void setVip(User p, String group, long durMillis, String pname){
		int count = 0;
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
		
		String pGroup = plugin.getPerms().getGroup(p);
		List<String[]> vips = plugin.getConfig().getVipInfo(p.getUniqueId().toString());
		if (!vips.isEmpty()){
			pGroup = vips.get(0)[2];
		}					
		vipConfig.getNode("activeVips",group,p.getUniqueId().toString(),"playerGroup").setValue(pGroup);	
		vipConfig.getNode("activeVips",group,p.getUniqueId().toString(),"duration").setValue(durMillis);		
		vipConfig.getNode("activeVips",group,p.getUniqueId().toString(),"nick").setValue(pname);	
		vipConfig.getNode("activeVips",group,p.getUniqueId().toString(),"expires-on-exact").setValue(plugin.getUtil().expiresOn(durMillis));			
		setActive(p,group,pGroup);
		
		plugin.addLog("SetVip | "+p.getName()+" | "+group+" | Expires on: "+plugin.getUtil().expiresOn(durMillis));
	}
	
	public void setActive(User p, String group, String pgroup){
		String uuid = p.getUniqueId().toString();
		String newVip = group;
		String oldVip = pgroup;
		
		for (Object key:getGroupList()){
			if (vipConfig.getNode("activeVips",key,uuid).hasMapChildren()){
				try {
					if (key.toString().equals(group)){						
						if (!getVipBoolean(true, "activeVips",key.toString(),uuid,"active")){
							newVip = key.toString();
							long total = getVipLong(0,"activeVips",key.toString(),uuid,"duration")+plugin.getUtil().getNowMillis();
							vipConfig.getNode("activeVips",key,uuid,"duration").setValue(TypeToken.of(Long.class), total);
						}
						vipConfig.getNode("activeVips",key,uuid,"active").setValue(TypeToken.of(Boolean.class), true);						
					} else {	
						if (getVipBoolean(false, "activeVips",key.toString(),uuid,"active")){
							oldVip = key.toString();
							long total = getVipLong(0,"activeVips",key.toString(),uuid,"duration")-plugin.getUtil().getNowMillis();
							vipConfig.getNode("activeVips",key,uuid,"duration").setValue(TypeToken.of(Long.class), total);
						}
						vipConfig.getNode("activeVips",key,uuid,"active").setValue(TypeToken.of(Boolean.class), false);
					}					
				} catch (ObjectMappingException e) {
					e.printStackTrace();
				}
			}
		}			
		runChangeVipCmds(uuid, newVip, oldVip);		
		reloadPerms();
		saveConfigAll();
	}
	
	public void runChangeVipCmds(String puuid, String newVip, String oldVip){
		for (String cmd:plugin.getConfig().getListString("configs","commandsToRunOnChangeVip")){
			String cmdf = cmd.replace("{p}", plugin.getUtil().getUser(UUID.fromString(puuid)).get().getName());
			if (!oldVip.equals("") && cmdf.contains("{oldvip}")){
				plugin.getGame().getScheduler().createTaskBuilder().delay(delay*100, TimeUnit.MILLISECONDS).execute(t -> {
					plugin.getGame().getCommandManager().process(Sponge.getServer().getConsole(), cmdf.replace("{oldvip}", oldVip));
				}).submit(plugin);
				delay++;
			} else
			if (!newVip.equals("") && cmdf.contains("{newvip}")){
				plugin.getGame().getScheduler().createTaskBuilder().delay(delay*100, TimeUnit.MILLISECONDS).execute(t -> {
					plugin.getGame().getCommandManager().process(Sponge.getServer().getConsole(), cmdf.replace("{newvip}", newVip));
				}).submit(plugin);
				delay++;
			} else {
				plugin.getGame().getScheduler().createTaskBuilder().delay(delay*100, TimeUnit.MILLISECONDS).execute(t -> {
					plugin.getGame().getCommandManager().process(Sponge.getServer().getConsole(), cmdf);
				}).submit(plugin);
				delay++;
			}
		}
		reloadPerms();
	}
	
	private void removeVip(User p, String group){
		plugin.addLog("RemoveVip | "+p.getName()+" | "+group);
		
		vipConfig.getNode("activeVips",group).removeChild(p.getUniqueId().toString());
		plugin.getGame().getScheduler().createTaskBuilder().delay(delay*100, TimeUnit.MILLISECONDS).execute(t -> {
			Sponge.getCommandManager().process(Sponge.getServer().getConsole(), getString("","configs","cmdOnRemoveVip").replace("{p}", p.getName()).replace("{vip}", group));			
		}).submit(plugin);
		delay++;
	}
	
	public void removeVip(User p, Optional<String> optg){
		String uuid = p.getUniqueId().toString();
		List<String[]> vipInfo = plugin.getConfig().getVipInfo(uuid);
		boolean id = false;
		String oldGroup = "";
		if (vipInfo.size() > 0){			
			for (String[] key:vipInfo){
				String group = key[1];
				oldGroup = key[2];
				if (vipInfo.size() > 1 ){
					if (optg.isPresent()){
						if (optg.get().equals(group)){
							plugin.getConfig().removeVip(p, group);			    							
						} else if (!id){
							plugin.getConfig().setActive(p, group, "");
							id = true;
						}			    						
	    			} else {	    				
    					plugin.getConfig().removeVip(p, group);
	    			}
				} else {
					plugin.getConfig().removeVip(p, group);
				}		
			}			    			
		}
		if (plugin.getConfig().getVipInfo(uuid).size() == 0){			
			for (String cmd:getListString("configs","commandsToRunOnVipFinish")){
				if (cmd.contains("{vip}")){continue;}
				String cmdf = cmd.replace("{p}", p.getName()).replace("{playergroup}", oldGroup);
				plugin.getGame().getScheduler().createTaskBuilder().delay(delay*100, TimeUnit.MILLISECONDS).execute(t -> {
					plugin.getGame().getCommandManager().process(Sponge.getServer().getConsole(), cmdf);
				}).submit(plugin);		
				delay++;
			}
		}
		reloadPerms();
		saveConfigAll();
	}
	
	public void reloadPerms(){
		plugin.getGame().getScheduler().createTaskBuilder().delay(1+delay*100, TimeUnit.MILLISECONDS).execute(t -> {
			plugin.getGame().getCommandManager().process(Sponge.getServer().getConsole(), getString("","configs","cmdToReloadPermPlugin"));				
		}).submit(plugin);	
		delay=0;
	}
	
	public long getVipLong(int def, String... node){
		return vipConfig.getNode((Object[])node).getLong(def);
	}
	
	public int getVipInt(int def, String... node){
		return vipConfig.getNode((Object[])node).getInt(def);
	}
	
	public String getVipString(String def, String... node){
		return vipConfig.getNode((Object[])node).getString(def);
	}
	
	public boolean getVipBoolean(boolean def, String... node){
		return vipConfig.getNode((Object[])node).getBoolean(def);
	}
	
	public long getLong(int def, String...node){
		return config.getNode((Object[])node).getLong(def);
	}
	
	public int getInt(int def, String...node){
		return config.getNode((Object[])node).getInt(def);
	}
	
	public String getString(String def, String...node){
		return config.getNode((Object[])node).getString(def);
	}
	
	public boolean getBoolean(boolean def, String...node){
		return config.getNode((Object[])node).getBoolean(def);
	}
	
	public List<String> getListString(String...node){
		List<String> keyList = new ArrayList<String>();
		try {
			keyList.addAll(config.getNode((Object[])node).getList(TypeToken.of(String.class)));
			return keyList;
		} catch (ObjectMappingException e) {
			e.printStackTrace();
		}
		return new ArrayList<String>();
	}
	
	public String getLang(String...nodes){
		StringBuilder msg = new StringBuilder();
		for (String node:nodes){
			try {
				msg.append(config.getNode("strings",node).getValue(TypeToken.of(String.class)));
			} catch (ObjectMappingException e) {
				msg.append("No strings found for node &6"+node);
			}
		}
		return msg.toString();
	}

	public boolean groupExists(String group) {
		return config.getNode("groups",group).hasMapChildren();
	}
	
	public List<? extends CommentedConfigurationNode> getCmdChances(String vip) {		
		return config.getNode("groups",vip,"cmdChances").getChildrenList();
	}

	public Set<Object> getListKeys() {
		return config.getNode("keys").getChildrenMap().keySet();
	}
	
	public Set<Object> getItemListKeys() {
		return config.getNode("itemKeys").getChildrenMap().keySet();
	}
	
	public Set<Object> getGroupList(){		
		return config.getNode("groups").getChildrenMap().keySet();
	}
	
	public HashMap<String,List<String[]>> getVipList(){
		HashMap<String,List<String[]>> vips = new HashMap<String,List<String[]>>();		
		getGroupList().forEach(groupobj -> {
			vipConfig.getNode("activeVips",groupobj).getChildrenMap().keySet().forEach(uuidobj -> {
				String uuid = uuidobj.toString();				
				List<String[]> vipInfo = getVipInfo(uuid);
				List<String[]> activeVips = new ArrayList<String[]>();
				vipInfo.stream().filter(v->v[3].equals("true")).forEach(active -> {
					activeVips.add(active);					
				});				
				if (activeVips.size() > 0){
					vips.put(uuid, activeVips);
				}
			});			
		});
		return vips;
	}
	
	/**Return player's vip info.<p>
	 * [0] = Duration, [1] = Vip Group, [2] = Player Group, [3] = Is Active, [4] = Player Nick
	 * @param puuid Player UUID as string.
	 * @return {@code List<String[5]>}
	 */
	public List<String[]> getVipInfo(String puuid){
		List<String[]> vips = new ArrayList<String[]>();
		getGroupList().stream().filter(k->vipConfig.getNode("activeVips",k.toString(),puuid).hasMapChildren()).forEach(key ->{
			vips.add(new String[]{
					getVipString("","activeVips",key.toString(),puuid,"duration"),
					key.toString(), 
					getVipString("","activeVips",key.toString(),puuid,"playerGroup"), 
					getVipString("","activeVips",key.toString(),puuid,"active"),
					getVipString("","activeVips",key.toString(),puuid,"nick")});
		});				
		return vips;
	}
	
	/**Return player's vip info.<p>
	 * [0] = Duration, [1] = Vip Group, [2] = Player Group, [3] = Is Active, [4] = Player Nick
	 * @param puuid Player UUID as string or nickname.
	 * @return {@code String[5]}
	 */
	public String[] getActiveVipInfo(String playName){
		User offp = null;	
		UserStorageService uss = Sponge.getGame().getServiceManager().provide(UserStorageService.class).get();
		
		try{
			UUID puuid = UUID.fromString(playName);			
			if (uss.get(puuid).isPresent()){
				offp = uss.get(puuid).get();
			} 
		} catch (IllegalArgumentException ex){
			if (uss.get(playName).isPresent()){
				offp = uss.get(playName).get();	
			}						
		}
		if (offp != null){
			for (String[] vips:getVipInfo(offp.getUniqueId().toString())){
				if (vips[3].equals("true")){
					return vips;
				}
			}
		}
		return new String[5];
	}
	
	public HashMap<String, String> getCmdChoices(){
		HashMap<String, String> choices = new HashMap<String, String>();
		getGroupList().forEach((k)->{
			choices.put(k.toString(), k.toString());
		});
		return choices;
	}
}
