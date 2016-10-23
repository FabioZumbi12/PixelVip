package br.net.fabiozumbi12.Bungee;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
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
				OfflinePlayer p = null;
				String thisID = plugin.getPVConfig().getString("server1", "bungee.serverID");
						
				try {
					String op = in.readUTF();
					
					try {
						p = plugin.serv.getOfflinePlayer(UUID.fromString(in.readUTF()));
					} catch (IllegalArgumentException|IOException ex){}
					
					if (p != null && op.equalsIgnoreCase("setvip")){	
						String a = in.readUTF();
						String b = in.readUTF();
						String id = in.readUTF();
						if (!id.equals(thisID)){
							plugin.getPVConfig().setVip(p, a, Long.parseLong(b), id);
							plugin.getLogger().info("Bungee Request: "+op);
						}				
					}			
					if (op.equalsIgnoreCase("addkey")){	
						String a = in.readUTF();
						String b = in.readUTF();
						String c = in.readUTF();
						String d = in.readUTF();
						String id = in.readUTF();
						if (!id.equals(thisID)){
							plugin.getPVConfig().addKey(a, b, Long.parseLong(c), Integer.parseInt(d), id);
							plugin.getLogger().info("Bungee Request: "+op);
						}				
					}
					if (op.equalsIgnoreCase("delkey")){	
						String a = in.readUTF();
						String b = in.readUTF();
						String id = in.readUTF();
						if (!id.equals(thisID)){
							plugin.getPVConfig().delKey(a, Integer.parseInt(b), id);
							plugin.getLogger().info("Bungee Request: "+op);
						}				
					}
					if (p != null && op.equalsIgnoreCase("enablevip")){	
						String a = in.readUTF();
						String b = in.readUTF();
						String id = in.readUTF();
						if (!id.equals(thisID)){
							plugin.getPVConfig().enableVip(p, a, Long.parseLong(b), id);
							plugin.getLogger().info("Bungee Request: "+op);
						}				
					}
					if (p != null && op.equalsIgnoreCase("removevip")){	
						String a = in.readUTF();
						String id = in.readUTF();
						if (!id.equals(thisID)){					
							if (a.equals("!")){
								plugin.getPVConfig().removeVip(p, Optional.empty(), id);
							} else {
								plugin.getPVConfig().removeVip(p, Optional.of(a), id);
							}
							plugin.getLogger().info("Bungee Request: "+op);
						}				
					}
					if (p != null && op.equalsIgnoreCase("setactive")){		
						String a = in.readUTF();
						String b = in.readUTF();
						String id = in.readUTF();
						if (!id.equals(thisID)){
							plugin.getPVConfig().setActive(p, a, b);
							plugin.getLogger().info("Bungee Request: "+op);
						}				
					}
					
				} catch (IOException e) {
					e.printStackTrace();
				}	
				
			}
		}, 40);
		
				
	}
	
	public void sendBungeeMessage(OfflinePlayer p, String[] info, String operation, String bungeeID){
		if (!plugin.getPVConfig().bungeeSyncEnabled()){
			return;
		}
		
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF(operation);
		if (p != null){
			out.writeUTF(p.getUniqueId().toString());
		} else {
			out.writeUTF("!");
		}		
		
		for (String s:info){
			out.writeUTF(s);
		}        	
		
		if (bungeeID.equals("")){
			out.writeUTF(plugin.getPVConfig().getString("server1", "bungee.serverID"));
		} else {
			out.writeUTF(bungeeID);
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
