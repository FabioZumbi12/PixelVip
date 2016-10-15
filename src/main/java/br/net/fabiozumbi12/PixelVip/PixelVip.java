package br.net.fabiozumbi12.PixelVip;

import java.io.File;
import java.util.Optional;
import java.util.UUID;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import br.net.fabiozumbi12.PixelVip.cmds.PVCommands;

public class PixelVip extends JavaPlugin {
	
	public PixelVip plugin;
	public Server serv;
	public PluginDescriptionFile pdf;
	public String mainPath;
	private PVLogger logger;
	private int task;
	
	private PVUtil util;
	public PVUtil getUtil(){
		return this.util;
	}
	
	private Permission perms;
		
	private PVConfig config;		
	public PVConfig getPVConfig(){
		return this.config;
	}
	
	public void reloadCmd(CommandSender src){
		logger.info("Reloading config module...");			
		reloadConfig();
		saveConfig();
		reloadVipTask();
		
		logger.warning(util.toColor("We have "+config.getVipList().size()+" active Vips"));
		logger.sucess(util.toColor("PixelVip reloaded"));
	}
	
	private PermsAPI permApi;
	public PermsAPI getPerms(){
		return this.permApi;
	}
	
	public void onEnable(){		
		plugin = this;
		serv = getServer();
		logger = new PVLogger();
        pdf = getDescription();
        mainPath = "plugins" + File.separator + pdf.getName() + File.separator;

        logger.info("Init config module...");			
		this.config = new PVConfig(this, mainPath, new File(mainPath+"config.yml"));
        
        logger.info("Init utils module...");
		this.util = new PVUtil(this);
		
		logger.info("Init economy module...");
		if (checkVault()){
        	RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
            if (rsp == null) {
            	super.setEnabled(false);
            	logger.info("-> Vault not found. This plugin needs Vault to work! Disabling...");  
            	return;
            } else {
            	perms = rsp.getProvider();
            	logger.info("-> Vault found. Hooked.");                	
            }
            this.permApi = new PermsAPI(perms);
        } else {
        	super.setEnabled(false);
        	logger.info("-> Vault not found. This plugin needs Vault to work! Disabling...");  
        	return;
        }
		
		logger.info("Init commands module...");
		new PVCommands(this);
		
		logger.info("Init scheduler module...");	
		reloadVipTask();		
		
		logger.warning(util.toColor("We have "+config.getVipList().size()+" active Vips"));
		logger.sucess(util.toColor("PixelVip enabled!"));
		
	}
	
	public void onDisable() {
		saveConfig();
		Bukkit.getScheduler().cancelTasks(plugin);
		logger.severe(util.toColor("PixelVip disabled!"));
	}
		
	private void reloadVipTask(){
		logger.info("Reloading tasks...");
		if (task != 0){
			Bukkit.getScheduler().cancelTask(task);
			logger.info("-> Task stoped");
		}
				
		task = serv.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable(){
			
			@Override
			public void run() {						
				getPVConfig().getVipList().forEach((uuid,value)->{
					OfflinePlayer p = util.getUser(UUID.fromString(uuid));				
					getPVConfig().getVipList().get(uuid).forEach((vipInfo)->{
						long dur = new Long(vipInfo[0]);
						if (p != null){
							if (!permApi.getGroup(p).equals(vipInfo[1])){
								config.runChangeVipCmds(uuid, vipInfo[1], permApi.getGroup(p));
							}
							if (dur <= util.getNowMillis()){
								getPVConfig().removeVip(p, Optional.of(vipInfo[1]));
								if (p.isOnline()){
									p.getPlayer().sendMessage(util.toColor(config.getLang("_pluginTag","vipEnded").replace("{vip}", vipInfo[1])));
								}
								getLogger().info(util.toColor(config.getLang("_pluginTag")+"&bThe vip &6" + vipInfo[1] + "&b of player &6" + p.getName() + " &bhas ended!"));
							}
						}					
					});
				});
			}
			
		}, 0, 20*60);
		logger.info("-> Task started");
	}
	
	//check if plugin Vault is installed
    private boolean checkVault(){
    	Plugin pVT = Bukkit.getPluginManager().getPlugin("Vault");
    	if (pVT != null && pVT.isEnabled()){
    		return true;
    	}
    	return false;
    }
}

class PVLogger {
	public void sucess(String s) {
    	Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "PixelVip: [&a&l"+s+"&r]"));
    }
	
    public void info(String s) {
    	Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "PixelVip: ["+s+"]"));
    }
    
    public void warning(String s) {
    	Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "PixelVip: [&6"+s+"&r]"));
    }
    
    public void severe(String s) {
    	Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "PixelVip: [&c&l"+s+"&r]"));
    }
    
    public void log(String s) {
    	Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "PixelVip: ["+s+"]"));
    }
}