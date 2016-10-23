package br.net.fabiozumbi12.PixelVip.cmds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import br.net.fabiozumbi12.PixelVip.PixelVip;

public class PVCommands implements CommandExecutor, TabCompleter {

	private PixelVip plugin;
	
    public PVCommands(PixelVip plugin){
		this.plugin = plugin;
				
		plugin.getCommand("newkey").setExecutor(this);
		plugin.getCommand("listkeys").setExecutor(this);
		plugin.getCommand("usekey").setExecutor(this);
		plugin.getCommand("viptime").setExecutor(this);
		plugin.getCommand("removevip").setExecutor(this);
		plugin.getCommand("setactive").setExecutor(this);
		plugin.getCommand("addvip").setExecutor(this);
		plugin.getCommand("setvip").setExecutor(this);
		plugin.getCommand("pixelvip").setExecutor(this);		
	}  
    
    @Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {

    	if (cmd.getName().equalsIgnoreCase("newkey") && args.length == 1){
    		List<String> list = new ArrayList<String>();
    		list.addAll(plugin.getPVConfig().getGroupList());
			return list;
		}
    	
    	if (cmd.getName().equalsIgnoreCase("pixelvip") && args.length == 1){
			return Arrays.asList("reload");
		}
    	
		return null;
	}
    
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if (cmd.getName().equalsIgnoreCase("newkey")){
			return newKey(sender, args);
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
		
		return true;
	}
	
    private boolean mainCommand(CommandSender sender, String[] args) {
		if (args.length == 1 && args[0].equalsIgnoreCase("reload")){
			plugin.reloadCmd(sender);
			return true;
		}
		return false;
	}
    
	/**Command to generate new key.
	 * 
	 * @return boolean
	 */
	private boolean newKey(CommandSender sender, String[] args) {
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
	    	plugin.getPVConfig().addKey(key, group, plugin.getUtil().dayToMillis(days), 1, "");	
	    	sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","keyGenerated")));
	    	sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("timeKey")+key));
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
	    	plugin.getPVConfig().addKey(key, group, plugin.getUtil().dayToMillis(days), uses, "");	
	    	sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","keyGenerated")));
	    	sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("timeKey")+key));
	    	sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("timeGroup")+group));
	    	sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("totalTime")+days)); 
	    	sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("infoUses")+uses)); 
	    	return true;
		}
		return false;
	}
	
	
	/**Command to list all available keys, and key's info.
	 * 
	 * @return CommandSpec
	 */
	public boolean listKeys(CommandSender sender, String[] args) {		
		Collection<String> keys = plugin.getPVConfig().getListKeys();
    	if (keys.size() > 0){
    		sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","listKeys")));
    		for (Object key:keys){			    			
    			String[] keyinfo = plugin.getPVConfig().getKeyInfo(key.toString());
    			long days = plugin.getUtil().millisToDay(keyinfo[1]);
    			sender.sendMessage(plugin.getUtil().toColor("&b- Key: &6"+key.toString()+"&b | Group: &6"+keyinfo[0]+"&b | Days: &6"+days+"&b | Uses left: &6"+keyinfo[2]));
	    	}
    	} else {
    		sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","noKeys")));
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
	    		String key = args[0];
		    	plugin.getPVConfig().activateVip(p, key, "", 0);
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
	@SuppressWarnings("deprecation")
	public boolean vipTime(CommandSender sender, String[] args) {		
		if (args.length == 0){
			plugin.getUtil().sendVipTime(sender, ((Player)sender).getUniqueId().toString(), ((Player)sender).getName());
			return true;			
		}		
		if (args.length == 1 && sender.hasPermission("pixelvip.cmd.player.others")){
			OfflinePlayer optp = Bukkit.getOfflinePlayer(args[0]);
			if (optp != null){
    			plugin.getUtil().sendVipTime(sender, optp.getUniqueId().toString(), optp.getName());			    			
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
	@SuppressWarnings("deprecation")
	public boolean removeVip(CommandSender sender, String[] args) {
		if (args.length == 1){
			OfflinePlayer optp = Bukkit.getOfflinePlayer(args[0]);
	    	if (optp != null){
	    		plugin.getPVConfig().removeVip(optp, Optional.empty(), "");
	    		sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","vipsRemoved")));	    		
	    	} else {
	    		sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","noPlayersByName")));	 
	    	}
	    	return true;
		}
		if (args.length == 2){
			OfflinePlayer optp = Bukkit.getOfflinePlayer(args[0]);
			Optional<String> group = Optional.of(args[1]);
			if (!plugin.getPVConfig().groupExists(group.get())){
				sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","noGroups")+args[1]));
				return true;
			} 
			
	    	if (optp != null){
	    		plugin.getPVConfig().removeVip(optp, group, "");
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
		    				plugin.getPVConfig().setActiveCmd(p, vip[1], vip[2], "");
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
			OfflinePlayer p = Bukkit.getOfflinePlayer(args[0]);
	    	if (p == null){
	    		sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","noPlayersByName")));
	    		return true;
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
	    	plugin.getPVConfig().activateVip(p, "", group, days);
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
			OfflinePlayer p = Bukkit.getOfflinePlayer(args[0]);
	    	if (p == null){
	    		sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","noPlayersByName")));
	    		return true;
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
	    	plugin.getPVConfig().setVip(p, group, plugin.getUtil().dayToMillis(days), "");		
	    	sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","vipSet")));
	    	return true;
		}
		return false;
	}	
}
