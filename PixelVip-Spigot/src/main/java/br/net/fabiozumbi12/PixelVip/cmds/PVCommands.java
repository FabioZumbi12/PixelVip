package br.net.fabiozumbi12.PixelVip.cmds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import br.net.fabiozumbi12.PixelVip.PixelVip;
import br.net.fabiozumbi12.PixelVip.db.PVDataFile;
import br.net.fabiozumbi12.PixelVip.db.PVDataManager;
import br.net.fabiozumbi12.PixelVip.db.PVDataMysql;

public class PVCommands implements CommandExecutor, TabCompleter {

	private PixelVip plugin;
	
    public PVCommands(PixelVip plugin){
		this.plugin = plugin;
		
		plugin.getCommand("delkey").setExecutor(this);
		plugin.getCommand("newkey").setExecutor(this);
		plugin.getCommand("sendkey").setExecutor(this);
		plugin.getCommand("newitemkey").setExecutor(this);
		plugin.getCommand("additemkey").setExecutor(this);
		plugin.getCommand("listkeys").setExecutor(this);
		plugin.getCommand("usekey").setExecutor(this);
		plugin.getCommand("viptime").setExecutor(this);
		plugin.getCommand("removevip").setExecutor(this);
		plugin.getCommand("setactive").setExecutor(this);
		plugin.getCommand("addvip").setExecutor(this);
		plugin.getCommand("setvip").setExecutor(this);
		plugin.getCommand("pixelvip").setExecutor(this);	
		plugin.getCommand("listvips").setExecutor(this);
	}  
    
    @Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {

    	if (cmd.getName().equalsIgnoreCase("newkey") && args.length == 1){
    		List<String> list = new ArrayList<String>();
    		list.addAll(plugin.getPVConfig().getGroupList());
			return list;
		}
    	
    	if (cmd.getName().equalsIgnoreCase("pixelvip") && args.length == 1){
			return Arrays.asList("reload","mysqlToFile","fileToMysql");
		}
    	
		return null;
	}
    
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player && !plugin.getPVConfig().worldAllowed(((Player)sender).getWorld())){
			sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","strings.cmdNotAllowedWorld")));
			return true;
		}
		
		if (cmd.getName().equalsIgnoreCase("delkey")){
			return delKey(sender, args);
		}
		
		if (cmd.getName().equalsIgnoreCase("newkey")){
			return newKey(sender, args, false);
		}
		
		if (cmd.getName().equalsIgnoreCase("sendkey")){
			return sendKey(sender, args);
		}
		
		if (cmd.getName().equalsIgnoreCase("newitemkey")){
			return newItemKey(sender, args);
		}
		
		if (cmd.getName().equalsIgnoreCase("additemkey")){
			return addItemKey(sender, args);
		}
		
		if (cmd.getName().equalsIgnoreCase("listkeys")){
			return listKeys(sender, args);
		}
		
		if (cmd.getName().equalsIgnoreCase("usekey")){
			return useKey(sender, args);
		}
		
		if (cmd.getName().equalsIgnoreCase("viptime")){
			return vipTime(sender, args);
		}
		
		if (cmd.getName().equalsIgnoreCase("removevip")){
			return removeVip(sender, args);
		}
		
		if (cmd.getName().equalsIgnoreCase("setactive")){
			return setActive(sender, args);
		}
		
		if (cmd.getName().equalsIgnoreCase("addvip")){
			return addVip(sender, args);
		}
		
		if (cmd.getName().equalsIgnoreCase("setvip")){
			return setVip(sender, args);
		}
		
		if (cmd.getName().equalsIgnoreCase("pixelvip")){
			return mainCommand(sender, args);
		}
		
		if (cmd.getName().equalsIgnoreCase("listvips")){
			return listVips(sender, args);
		}		
		return true;
	}
	
	private boolean listVips(CommandSender sender, String[] args) {  	
    	HashMap<String, List<String[]>> vips = plugin.getPVConfig().getVipList();    	
    	sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","list-of-vips")));  
    	sender.sendMessage(plugin.getUtil().toColor("&b---------------------------------------------"));
    	vips.forEach((uuid,vipinfolist)->{  	
    		vipinfolist.forEach((vipinfo)->{
    			String pname = plugin.getServer().getOfflinePlayer(UUID.fromString(uuid)).getName();
    			if (pname == null){
    				pname = vipinfo[4];
    			}
    			sender.sendMessage(plugin.getUtil().toColor("&7> Player &3"+pname+"&7:"));
    			sender.sendMessage(plugin.getUtil().toColor("  "+plugin.getPVConfig().getLang("timeGroup")+vipinfo[1]));
    			sender.sendMessage(plugin.getUtil().toColor("  "+plugin.getPVConfig().getLang("timeLeft")+plugin.getUtil().millisToMessage(Long.parseLong(vipinfo[0])-plugin.getUtil().getNowMillis())));
    		});
    	});
    	sender.sendMessage(plugin.getUtil().toColor("&b---------------------------------------------")); 
		return true;
	}

	private boolean mainCommand(CommandSender sender, String[] args) {
		if (args.length == 1){
			if (args[0].equalsIgnoreCase("reload")){
				plugin.reloadCmd();
				return true;
			}
		}
		if (sender instanceof ConsoleCommandSender){
			if (args.length == 1){
				if (args[0].equalsIgnoreCase("fileToMysql")){
					if (plugin.getConfig().getString("configs.database.type").equalsIgnoreCase("mysql")){
						sender.sendMessage(plugin.getUtil().toColor("&cYour database type is already Mysql. Use &4/pixelvip mysqlToFile &cif you want to convert to file."));
						return true;
					}
					plugin.getConfig().set("configs.database.type", "mysql");
					convertDB(new PVDataMysql(plugin));
					return true;
				}
				if (args[0].equalsIgnoreCase("mysqlToFile")){
					if (plugin.getConfig().getString("configs.database.type").equalsIgnoreCase("file")){
						sender.sendMessage(plugin.getUtil().toColor("&cYour database type is already File. Use &4/pixelvip fileToMysql &cif you want to convert to mysql."));
						return true;
					}
					plugin.getConfig().set("configs.database.type", "file");
					convertDB(new PVDataFile(plugin));
					return true;
				}
			}
		}		
		return false;
	}
    
	private void convertDB(PVDataManager dm){
		plugin.getPVConfig().getAllVips().forEach((uuid,vipInfo)->{
			vipInfo.forEach(vips->{
				dm.addRawVip(vips[1], uuid, vips[2], Long.valueOf(vips[0]), vips[4], plugin.getUtil().expiresOn(Long.valueOf(vips[0])), Boolean.parseBoolean(vips[3]));
			});
		});
		dm.saveVips();
		
		plugin.getPVConfig().getListKeys().forEach(key->{
			String[] keyInfo = plugin.getPVConfig().getKeyInfo(key);
			dm.addRawKey(key, keyInfo[0], Long.parseLong(keyInfo[1]), Integer.parseInt(keyInfo[2]));
		});
		
		plugin.getPVConfig().getItemListKeys().forEach(key->{
			dm.addRawItemKey(key, plugin.getPVConfig().getItemKeyCmds(key));
		});		
		dm.saveKeys();
		
		plugin.getPVConfig().getAllTrans().forEach((code,nick)->{
			dm.addTras(code, nick);
		});
		
		dm.closeCon();
		plugin.saveConfig();
		plugin.reloadCmd();
	}
	
	private List<String> fixArgs(String[] args){
		StringBuilder cmds = new StringBuilder();			
		for (String arg:args){
			cmds.append(arg+" ");
		}
		String[] cmdsSplit = cmds.toString().split(",");
		List<String> cmdList = new ArrayList<String>();	
		for (String cmd:cmdsSplit){
			cmd = cmd.replace("  ", " ");
			if (cmd.length() <= 0){
				continue;
			}
			if (cmd.startsWith(" ")){
				cmd = cmd.substring(1);
			} 
			if (cmd.endsWith(" ")){
				cmd = cmd.substring(0,cmd.length()-1);
			}
			cmdList.add(cmd);
		}
		return cmdList;
	}
	/**Command to generate new item key.
	 * 
	 * @return boolean
	 */
	private boolean addItemKey(CommandSender sender, String[] args) {
		if (args.length >= 2){
			String key = args[0].toUpperCase();
			args[0] = "";					
			
			List<String> cmds = fixArgs(args);
			plugin.getPVConfig().addItemKey(key, cmds);
			
			sender.sendMessage(plugin.getUtil().toColor("&b---------------------------------------------"));
			sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","itemsAdded")));			
			plugin.getUtil().sendHoverKey(sender, key);	 
			for (String cmd:cmds){
				sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("item"))+cmd);
			}
			sender.sendMessage(plugin.getUtil().toColor("&b---------------------------------------------"));
			return true;
		}
		
		return false;
	}
	
	/**Command to generate new item key.
	 * 
	 * @return boolean
	 */
	private boolean newItemKey(CommandSender sender, String[] args) {
		if (args.length >= 1){
			List<String> cmds = fixArgs(args);
			String key = plugin.getUtil().genKey(plugin.getPVConfig().getInt(10,"configs.key-size"));
			plugin.getPVConfig().addItemKey(key, cmds);
			
			sender.sendMessage(plugin.getUtil().toColor("&b---------------------------------------------"));
			sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","keyGenerated")));
			plugin.getUtil().sendHoverKey(sender, key);	 
			for (String cmd:cmds){
				sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("item"))+cmd);
			}
			sender.sendMessage(plugin.getUtil().toColor("&b---------------------------------------------"));
			return true;
		}
		
		return false;
	}
	
	private boolean delKey(CommandSender sender, String[] args) {
		if (args.length == 1){
			if (plugin.getPVConfig().delKey(args[0], 1) || plugin.getPVConfig().delItemKey(args[0])){
				sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","keyRemoved")+args[0]));
			} else {
				sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","noKeyRemoved")));
			}
			return true;
		}
		
		return false;
	}
	
	/**Command to generate new key.
	 * 
	 * @return boolean
	 */
	private boolean newKey(CommandSender sender, String[] args, boolean isSend) {
		if (args.length == 2){
			String group = args[0];
			long days = 0;
	    	
			try{
				days = Long.parseLong(args[1]);
			} catch (NumberFormatException ex){
				return false;
			}
			
	    	if (days <= 0){
	    		sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","moreThanZero")));
	    		return true;
	    	}
	    				    	
	    	if (!plugin.getPVConfig().groupExists(group)){
	    		sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","noGroups")+group));
	    		return true;
	    	}
	    	String key = plugin.getUtil().genKey(plugin.getPVConfig().getInt(10,"configs.key-size"));
	    	plugin.getPVConfig().addKey(key, group, plugin.getUtil().dayToMillis(days), 1);	
	    	if (isSend){
	    		sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","keySendTo")));
	    	} else {
	    		sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","keyGenerated")));
	    	}
	    	plugin.getUtil().sendHoverKey(sender, key);	    		    	
	    	sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("timeGroup")+group));
	    	sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("totalTime")+days)); 
	    	return true;
		}
		
		if (args.length == 3){
			String group = args[0];
			long days = 0;
	    	int uses = 0;
	    	
			try{
				days = Long.parseLong(args[1]);
		    	uses = Integer.parseInt(args[2]);
			} catch (NumberFormatException ex){
				return false;
			}
	    	
	    	
	    	if (days <= 0){
	    		sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","moreThanZero")));
	    		return true;
	    	}
	    		
	    	if (uses <= 0){
	    		sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","moreThanZero")));
	    		return true;
	    	}
	    	
	    	if (!plugin.getPVConfig().groupExists(group)){
	    		sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","noGroups")+group));
	    		return true;
	    	}
	    	String key = plugin.getUtil().genKey(plugin.getPVConfig().getInt(10,"configs.key-size"));
	    	plugin.getPVConfig().addKey(key, group, plugin.getUtil().dayToMillis(days), uses);	
	    	if (isSend){
	    		sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","keySendTo")));
	    	} else {
	    		sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","keyGenerated")));
	    	}	    	
	    	plugin.getUtil().sendHoverKey(sender, key);	 
	    	sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("timeGroup")+group));
	    	sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("totalTime")+days)); 
	    	sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("infoUses")+uses)); 
	    	return true;
		}
		return false;
	}
	
	/**Command to send a new key to a player.
	 * 
	 * @return boolean
	 */
	private boolean sendKey(CommandSender sender, String[] args) {
		if (args.length == 2){
			if (plugin.serv.getPlayer(args[0]) == null){
				return false;
			}
			if (!plugin.getPVConfig().getListKeys().contains(args[1])){
				return false;
			}
			Player play = plugin.serv.getPlayer(args[0]);
			String[] keyInfo = plugin.getPVConfig().getKeyInfo(args[1]);
			
			play.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","keySendTo")));	    	
	    	plugin.getUtil().sendHoverKey(play, args[1]);	 
	    	play.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("timeGroup")+keyInfo[0]));
	    	play.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("totalTime")+plugin.getUtil().millisToDay(keyInfo[1]))); 
	    	play.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("infoUses")+keyInfo[2])); 
	    	return true;
		}
		
		if (args.length == 3 || args.length == 4){
			String[] nargs = new String[args.length-1];
			String splay = args[0];
			if (plugin.serv.getPlayer(splay) == null){
				return false;
			}
			Player play = plugin.serv.getPlayer(splay);
			for (int i = 0; i < args.length; i++){
				if (i+1 == args.length){
					break;
				}
				nargs[i] = args[i+1];
			}
			return newKey(play, nargs, true);
		}
		return false;
	}
	
	/**Command to list all available keys, and key's info.
	 * 
	 * @return CommandSpec
	 */
	public boolean listKeys(CommandSender sender, String[] args) {		
		Collection<String> keys = plugin.getPVConfig().getListKeys();
		Collection<String> itemKeys = plugin.getPVConfig().getItemListKeys();
		int i = 0;
    	if (keys.size() > 0){
    		sender.sendMessage(plugin.getUtil().toColor("&b---------------------------------------------"));
    		sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","listKeys")));
    		for (Object key:keys){			    			
    			String[] keyinfo = plugin.getPVConfig().getKeyInfo(key.toString());
    			long days = plugin.getUtil().millisToDay(keyinfo[1]);
    			sender.sendMessage(plugin.getUtil().toColor("&b- Key: &6"+key.toString()+"&b | Group: &6"+keyinfo[0]+"&b | Days: &6"+days+"&b | Uses left: &6"+keyinfo[2]));
    			i++;
	    	}
    	}
    	   	
    	if (itemKeys.size() > 0){
    		sender.sendMessage(plugin.getUtil().toColor("&b---------------------------------------------"));
    		sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","listItemKeys")));
    		for (Object key:itemKeys){		
    			List<String> cmds = plugin.getPVConfig().getItemKeyCmds(key.toString());
    			plugin.getUtil().sendHoverKey(sender, key.toString());	 
    			for (String cmd:cmds){
    				sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("item"))+cmd);
    			}    			
    			i++;
	    	}
    	}
    	if (i == 0){
    		sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","noKeys")));
    	} else {
    		sender.sendMessage(plugin.getUtil().toColor("&b---------------------------------------------"));
    	}
    	return true;
	}
	
	/**Command to activate a vip using a key.
	 * 
	 * @return CommandSpec
	 */
	public boolean useKey(CommandSender sender, String[] args) {
		if (args.length == 1){
			if (sender instanceof Player){
	    		Player p = (Player) sender;
	    		String key = args[0].toUpperCase();
	    		if (plugin.getPagSeguro() != null){
	    			if (plugin.getPVConfig().transExist(key)){
						sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","pagseguro.codeused")));
						return true;
					}
	    			plugin.processTrans.add(key);
					if (plugin.getPagSeguro().checkTransaction(sender, key)){
						return true;
					}
					plugin.processTrans.remove(key);
	    		}	    		
	    		plugin.getPVConfig().activateVip(p, key, "", 0, p.getName());
	    	} else {
	    		sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","onlyPlayers")));
	    	}
			return true;
		}
		return false;
	}
	
	/**Command to check the vip time.
	 * 
	 * @return CommandSpec
	 */	
	public boolean vipTime(CommandSender sender, String[] args) {		
		if (sender instanceof Player && args.length == 0){
			plugin.getUtil().sendVipTime(sender, ((Player)sender).getUniqueId().toString(), ((Player)sender).getName());
			return true;			
		}		
		if (args.length == 1 && sender.hasPermission("pixelvip.cmd.player.others")){
			String uuid = plugin.getPVConfig().getVipUUID(args[0]);
			if (uuid != null){
    			plugin.getUtil().sendVipTime(sender, uuid, args[0]);			    			
    		} else {
    			sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","noPlayersByName")));	
    		}
			return true;
		}		   
		return false;
	}
	
	/**Command to remove a vip of player.
	 * 
	 * @return CommandSpec
	 */
	public boolean removeVip(CommandSender sender, String[] args) {
		if (args.length == 1){
			String uuid = plugin.getPVConfig().getVipUUID(args[0]);
			if (uuid != null){
				plugin.getPVConfig().removeVip(uuid, Optional.empty());
	    		sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","vipsRemoved")));	    		
	    	} else {
	    		sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","noPlayersByName")));	 
	    	}
	    	return true;
		}
		if (args.length == 2){
			Optional<String> group = Optional.of(args[1]);
			if (!plugin.getPVConfig().groupExists(group.get())){
				sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","noGroups")+args[1]));
				return true;
			} 
			
			String uuid = plugin.getPVConfig().getVipUUID(args[0]);
			if (uuid != null){
	    		plugin.getPVConfig().removeVip(uuid, group);
	    		sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","vipsRemoved")));	    		
	    	} else {
	    		sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","noPlayersByName")));	 
	    	}
	    	return true;
		}
		return false;    	   
	}
	
	/**Command to sets the active vip, if more than one key activated.
	 * @return 
	 * 
	 * @return CommandSpec
	 */
	public boolean setActive(CommandSender sender, String[] args) {
		if (args.length == 1){
			if (sender instanceof Player){				
	    		Player p = (Player) sender;
	    		String group = args[0];
		    	if (!plugin.getPVConfig().groupExists(group)){
		    		sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","noGroups")+group));
		    		return true;
		    	}
	    		
	    		List<String[]> vipInfo = plugin.getPVConfig().getVipInfo(p.getUniqueId().toString());
	    		
		    	if (vipInfo.size() > 0){				    		
		    		for (String[] vip:vipInfo){
		    			if (vip[1].equalsIgnoreCase(group)){				    				
		    				plugin.getPVConfig().setActive(p.getUniqueId().toString(), vip[1], vip[2]);
		    				p.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","activeVipSetTo")+vip[1]));
		    				return true;
		    			}
		    		}
		    	}
		    	sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","noGroups")+group));
		    	return true;
	    	} else {
	    		sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","onlyPlayers")));
	    		return true;
	    	}
		}
		if (args.length == 2 && sender.hasPermission("pixelvip.setactive")){	
			String uuid = plugin.getPVConfig().getVipUUID(args[1]);
			if (uuid == null){
				sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","noPlayersByName")));
	    		return true;
			}
    		String group = args[0];
	    	if (!plugin.getPVConfig().groupExists(group)){
	    		sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","noGroups")+group));
	    		return true;
	    	}
    		
    		List<String[]> vipInfo = plugin.getPVConfig().getVipInfo(uuid);
    		
	    	if (vipInfo.size() > 0){				    		
	    		for (String[] vip:vipInfo){
	    			if (vip[1].equalsIgnoreCase(group)){				    				
	    				plugin.getPVConfig().setActive(uuid, vip[1], vip[2]);
	    				sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","activeVipSetTo")+vip[1]));
	    				return true;
	    			}
	    		}
	    	}
	    	sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","noGroups")+group));
	    	return true;
		}
    	return false;
	}
	
	/**Command to add a vip for a player without key.
	 * @return 
	 * 
	 * @return CommandSpec
	 */
	@SuppressWarnings("deprecation")
	public boolean addVip(CommandSender sender, String[] args) {
		if (args.length == 3){
			String pname = args[0];
			OfflinePlayer p = Bukkit.getOfflinePlayer(pname);
	    	if (p.getName() != null){
	    		pname = p.getName();
	    	}
	    	String group = args[1];
	    	if (!plugin.getPVConfig().groupExists(group)){
	    		sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","noGroups")+group));
	    		return true;
	    	}
	    	long days = 0;
	    	try {
	    		days = Long.parseLong(args[2]);
	    	} catch (NumberFormatException ex){
	    		return false;
	    	}
	    	plugin.getPVConfig().activateVip(p, null, group, days, pname);
	    	sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","vipAdded")));
	    	return true;
		}
		return false;
	}
	
	/**Command to set a vip without activation and without key.
	 * @return 
	 * 
	 * @return CommandSpec
	 */
	@SuppressWarnings("deprecation")
	public boolean setVip(CommandSender sender, String[] args) {
		if (args.length == 3){			
			String pname = args[0];
			String uuid = plugin.getPVConfig().getVipUUID(pname);
			if (uuid == null){
				uuid = Bukkit.getOfflinePlayer(pname).getUniqueId().toString();
			}
	    	String group = args[1];
	    	if (!plugin.getPVConfig().groupExists(group)){
	    		sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","noGroups")+group));
	    		return true;
	    	}
	    	long days = 0;
	    	try {
	    		days = Long.parseLong(args[2]);
	    	} catch (NumberFormatException ex){
	    		return false;
	    	}
	    	plugin.getPVConfig().setVip(uuid, group, plugin.getUtil().dayToMillis(days), pname);		
	    	sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","vipSet")));
	    	return true;
		}
		return false;
	}	
}
