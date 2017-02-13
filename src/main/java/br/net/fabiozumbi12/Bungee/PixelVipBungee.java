package br.net.fabiozumbi12.Bungee;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;

import br.net.fabiozumbi12.PixelVip.PixelVip;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class PixelVipBungee implements PluginMessageListener, Listener {

	private PixelVip plugin;
	private List<byte[]> pendentBytes;
	
	public PixelVipBungee(PixelVip plugin){
		this.plugin = plugin;
		this.pendentBytes = new ArrayList<byte[]>();
	}
	
	@Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {		
		if (!channel.equals("PixelVipBungee")){
			return;
		}		
		if (!plugin.getPVConfig().bungeeSyncEnabled()){
			return;
		}
		
		plugin.serv.getScheduler().runTaskLater(plugin, new Runnable(){
			@Override
			public void run() {	
				
				DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
				try {
					String op = in.readUTF();					
					if (op.equals("receive")){						
						while (in.available() > 0){
							String a = in.readUTF();
							String b = in.readUTF();
							
							if (a.startsWith("activeVips.")){
								try {
									plugin.getPVConfig().getVips().set(a,Long.parseLong(b));
								} catch (NumberFormatException ex){
									if (b.equals("true") || b.equals("false")){
										plugin.getPVConfig().getVips().set(a,Boolean.valueOf(b));
									} else {
										plugin.getPVConfig().getVips().set(a,b);
									}								
								}
								plugin.getPVConfig().saveVips();
							} 
							if (a.startsWith("keys.")){
								try {
									plugin.getConfig().set(a,Long.parseLong(b));
								} catch (NumberFormatException ex){
									if (b.equals("true") || b.equals("false")){
										plugin.getConfig().set(a,Boolean.valueOf(b));
									} else {
										plugin.getConfig().set(a,b);
									}								
								}
								plugin.saveConfig();
							}							
						}						
					}
				} catch (IOException e) {
					e.printStackTrace();
				}	
				
			}
		}, 20);
	}
	
	public void sendBungeeSync(){
		if (!plugin.getPVConfig().bungeeSyncEnabled()){
			return;
		}		
		
		ByteArrayDataOutput out = ByteStreams.newDataOutput();		
		//this id
		out.writeUTF(plugin.getPVConfig().getString("server1", "bungee.serverID"));
		//operation
		out.writeUTF("send");
		
		for (String key:plugin.getConfig().getKeys(true)){
			if (key.startsWith("keys.") && !plugin.getConfig().isConfigurationSection(key)){
				out.writeUTF(key);
				out.writeUTF(plugin.getConfig().getString(key));
			}			
		}
		for (String key:plugin.getPVConfig().getVips().getKeys(true)){
			if (key.startsWith("activeVips.") && !plugin.getPVConfig().getVips().isConfigurationSection(key)){
				out.writeUTF(key);
				out.writeUTF(plugin.getPVConfig().getVips().getString(key));
			}			
		}
		sendPendentBungee(out.toByteArray());	
	}
	
	private void sendPendentBungee(final byte[] out){
		if (plugin.serv.getOnlinePlayers().size() > 0){
			Player play = Iterables.getFirst(plugin.serv.getOnlinePlayers(), null);
			play.sendPluginMessage(plugin, "PixelVipBungee", out);					
		} else {
			pendentBytes.add(out);
		}		
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e){
		if (!plugin.getPVConfig().bungeeSyncEnabled()){
			return;
		}
		Player p = e.getPlayer();
		plugin.serv.getScheduler().runTaskLater(plugin, new Runnable(){
			@Override
			public void run() {
				if (p.isOnline()){
					for (byte[] b:pendentBytes){
						p.sendPluginMessage(plugin, "PixelVipBungee", b);	
					}
					pendentBytes.clear();
				}
			}			
		}, 40);		
	}
}
