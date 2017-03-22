package br.net.fabiozumbi12.pixelvip;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;

import br.net.fabiozumbi12.pixelvip.cmds.PVCommands;

import com.google.inject.Inject;

@Plugin(id = "pixelvip", 
name = "PixelVip", 
version = "1.0.5",
authors="FabioZumbi12", 
description="Plugin to give VIP to your players.")
public class PixelVip {
	
	@Inject private Logger logger;
	public Logger getLogger(){	
		return logger;
	}
	
	@Inject
	@ConfigDir(sharedRoot = true)
	private Path configDir;

	@Inject
	@DefaultConfig(sharedRoot = false)
	private File defConfig;
	
	@Inject
	@DefaultConfig(sharedRoot = true)
	private ConfigurationLoader<CommentedConfigurationNode> configManager;
	public ConfigurationLoader<CommentedConfigurationNode> getCfManager(){
		return configManager;
	}
	
	private PVConfig config;
	public PVConfig getConfig(){
		return config;
	}
	
	PermsAPI perms;
	public PermsAPI getPerms(){
		return this.perms;
	}
	
	@Inject private Game game;
	public Game getGame(){
		return this.game;
	}
	
	private PVUtil util;
	public PVUtil getUtil(){
		return this.util;
	}
	
	private PVCommands cmds;
	public PVCommands getCmds(){
		return this.cmds;
	}
	
	private Task task;
	
	@Listener
    public void onServerStart(GameStartedServerEvent event) {		
		logger.info("Init utils module...");
		this.util = new PVUtil(this);
		
		logger.info("Init config module...");
		configManager = HoconConfigurationLoader.builder().setFile(defConfig).build();				
		this.config = new PVConfig(this, configDir, defConfig);
		
		logger.info("Init perms module...");
		this.perms = new PermsAPI(game);
		
		logger.info("Init commands module...");
		this.cmds = new PVCommands(this);
		CommandSpec spongevip = CommandSpec.builder()
			    .description(Text.of("Use to see the plugin info and reload."))
			    .permission("pixelvip.cmd.reload")
			    .arguments(GenericArguments.optional(GenericArguments.string(Text.of("reload"))))
			    .executor((src, args) -> { {
			    	if (args.hasAny("reload")){			    		
			    		this.config = new PVConfig(this, configDir, defConfig);
			    		this.cmds.reload();
			    		reloadVipTask();
			    		src.sendMessage(util.toText("&aPixelVip reloaded"));
			    	} else {
			    		src.sendMessage(util.toText("&a> PixelVip by &6FabioZumbi12"));
			    	}
			    	return CommandResult.success();
			    }			    	
			    })
			    .build();
		Sponge.getCommandManager().register(this, spongevip, "pixelvip");
						
		logger.info("Init scheduler module...");	
		reloadVipTask();		
		
		logger.info(util.toColor("We have &6"+config.getVipList().size()+" &ractive Vips"));
		logger.info(util.toColor("&aPixelVip enabled!&r"));
	}
	
	public void reloadCmd(CommandSource src){
		logger.info("Reloading config module...");
		config.reloadVips();
		reloadVipTask();
				
		logger.info(util.toColor("We have "+config.getVipList().size()+" active Vips"));
		logger.info(util.toColor("PixelVip reloaded"));
	}
	
	@Listener
	public void onStopServer(GameStoppingServerEvent e) {
		task.cancel();
		config.saveConfigAll();
		logger.info(util.toColor("&aPixelVip disabled!&r"));
	}
	
	@Listener
    public void onReloadPlugins(GameReloadEvent event) {		
		this.config = new PVConfig(this, configDir, defConfig);
		this.cmds.reload();
		reloadVipTask();
		logger.info(util.toColor("&aPixelVip reloaded"));
	}
	
	@Listener
	public void onPlayerJoin(ClientConnectionEvent.Login e){
		User p = e.getTargetUser();
		
		if (getConfig().queueCmds()){
			game.getScheduler().createSyncExecutor(this).schedule(new Runnable(){
				@Override
				public void run() {	
					List<String> qcmds = getConfig().getQueueCmds(p.getUniqueId().toString());
					qcmds.forEach((cmd)->{
						game.getScheduler().createSyncExecutor(this).schedule(new Runnable(){
							@Override
							public void run() {
								Sponge.getCommandManager().process(Sponge.getServer().getConsole(), cmd);
							}
						}, 500, TimeUnit.MILLISECONDS);
					});						
				}
			}, 3, TimeUnit.SECONDS);			
		}
	}
		
	private void reloadVipTask(){
		logger.info("Reloading tasks...");
		if (task != null){
			task.cancel();
			logger.info("-> Task stoped");
		}
				
		task = game.getScheduler().createTaskBuilder().interval(60, TimeUnit.SECONDS).execute(t -> {						
			getConfig().getVipList().forEach((uuid,value)->{
				Optional<User> p = util.getUser(UUID.fromString(uuid));				
				getConfig().getVipList().get(uuid).forEach((vipInfo)->{
					long dur = new Long(vipInfo[0]);										
					if (p.isPresent()){
						if (!perms.getGroup(p.get()).equals(vipInfo[1])){
							config.runChangeVipCmds(uuid, vipInfo[1], perms.getGroup(p.get()));
						}						
						if (dur <= util.getNowMillis()){
							getConfig().removeVip(p.get(), Optional.of(vipInfo[1]));
							if (p.get().isOnline()){
								p.get().getPlayer().get().sendMessage(util.toText(config.getLang("_pluginTag","vipEnded").replace("{vip}", vipInfo[1])));
							}
							getLogger().info(util.toColor(config.getLang("_pluginTag")+"&bThe vip &6" + vipInfo[1] + "&b of player &6" + p.get().getName() + " &bhas ended!"));
						}
					}					
				});
			});
		}).submit(this);
		logger.info("-> Task started");
	}
	
	public void addLog(String log){
    	String timeStamp = new SimpleDateFormat("dd.MM.yyyy - HH:mm:ss").format(Calendar.getInstance().getTime());
    	try {
    		File folder = new File(this.configDir+File.separator+"logs");
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