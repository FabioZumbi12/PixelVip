package br.net.fabiozumbi12.PixelVip;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import br.net.fabiozumbi12.Bungee.PixelVipBungee;
import br.net.fabiozumbi12.PixelVip.cmds.PVCommands;

import com.earth2me.essentials.Essentials;

public class PixelVip extends JavaPlugin implements Listener {
	
	public PixelVip plugin;
	public Server serv;
	public PluginDescriptionFile pdf;
	public String mainPath;
	private PVLogger logger;
	public Essentials ess;
	private int task = 0;
	
	private PVUtil util;
	public PVUtil getUtil(){
		return this.util;
	}
	
	public PVLogger getPVLogger(){
		return this.logger;
	}
	
	private Permission perms;
		
	private PVConfig config;		
	public PVConfig getPVConfig(){
		return this.config;
	}
	
	public void reloadCmd(){
		logger.info("Reloading config module...");		
		reloadConfig();
		config.closeCon();
		this.config = new PVConfig(this, mainPath, new File(mainPath+"config.yml"));
		reloadVipTask();
		saveConfig();
				
		logger.warning(util.toColor("We have "+config.getVipList().size()+" active Vips"));
		logger.sucess(util.toColor("PixelVip reloaded"));
	}
	
	private PermsAPI permApi;	
	public PermsAPI getPerms(){
		return this.permApi;
	}
	
	private PixelVipBungee pvBungee;
	public PixelVipBungee getPVBungee(){
		return this.pvBungee;
	}
	
	public void onEnable(){		
		plugin = this;
		serv = getServer();
		serv.getPluginManager().registerEvents(this, this);
		
		//register bungee
		pvBungee = new PixelVipBungee(this);
		serv.getPluginManager().registerEvents(pvBungee, this);
		serv.getMessenger().registerOutgoingPluginChannel(this,"PixelVipBungee");
		serv.getMessenger().registerIncomingPluginChannel(this, "PixelVipBungee", pvBungee);
		
		logger = new PVLogger();
        pdf = getDescription();
        mainPath = "plugins" + File.separator + pdf.getName() + File.separator;

        logger.info("Init config module...");			
		this.config = new PVConfig(this, mainPath, new File(mainPath+"config.yml"));
        
        logger.info("Init utils module...");
		this.util = new PVUtil(this);
		
		logger.info("Init essentials module...");
		Plugin essPl = Bukkit.getServer().getPluginManager().getPlugin("Essentials");
		if (essPl != null && essPl.isEnabled()){
			logger.info(util.toColor("Essentials found. Hooked!"));
			ess = (Essentials) essPl;
		}
		
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
					OfflinePlayer p = Bukkit.getOfflinePlayer(UUID.fromString(uuid));				
					getPVConfig().getVipList().get(uuid).forEach((vipInfo)->{
						long dur = new Long(vipInfo[0]);
						if (p.getName() != null && permApi.getGroups(p) != null && !Arrays.asList(permApi.getGroups(p)).contains(vipInfo[1])){
							config.runChangeVipCmds(p, vipInfo[1], permApi.getGroup(p));
						}
						if (dur <= util.getNowMillis()){
							getPVConfig().removeVip(p, Optional.of(vipInfo[1]));
							if (p.isOnline()){
								p.getPlayer().sendMessage(util.toColor(config.getLang("_pluginTag","vipEnded").replace("{vip}", vipInfo[1])));
							}
							Bukkit.getConsoleSender().sendMessage(util.toColor(config.getLang("_pluginTag")+"&bThe vip &6" + vipInfo[1] + "&b of player &6" + vipInfo[4] + " &bhas ended!"));
						}					
					});
				});
			}			
		}, 0, 20*60);
		logger.info("-> Task started");
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e){
		Player p = e.getPlayer();
		
		if (getPVConfig().queueCmds()){
			plugin.serv.getScheduler().runTaskLater(plugin, new Runnable(){
				@Override
				public void run() {	
					List<String> qcmds = getPVConfig().getQueueCmds(p.getUniqueId().toString());
					qcmds.forEach((cmd)->{
						plugin.serv.getScheduler().runTaskLater(plugin, new Runnable(){
							@Override
							public void run() {
								plugin.serv.dispatchCommand(plugin.serv.getConsoleSender(), cmd);
							}
						}, 10);
					});						
				}
			}, 60);			
		}
	}
	
	//check if plugin Vault is installed
    private boolean checkVault(){
    	Plugin pVT = Bukkit.getPluginManager().getPlugin("Vault");
    	if (pVT != null && pVT.isEnabled()){
    		return true;
    	}
    	return false;
    }
    
    public void addLog(String log){
    	String timeStamp = new SimpleDateFormat("dd.MM.yyyy - HH:mm:ss").format(Calendar.getInstance().getTime());
    	try {
    		File folder = new File(this.getDataFolder()+File.separator+"logs");
    		if (!folder.exists()){
    			folder.mkdir();
    		}
    		File logs = new File(folder+File.separator+"logs.log");    		
    		
			FileWriter fw = new FileWriter(logs,true);
			fw.append(timeStamp+" - PixelVip Log: "+log);
			fw.append("\n");
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
    }
}