package br.net.fabiozumbi12.pixelvip.bukkit.bungee;

import br.net.fabiozumbi12.pixelvip.bukkit.PixelVip;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PixelVipBungee implements PluginMessageListener, Listener {

	private PixelVip plugin;
	private List<byte[]> pendentBytes;
	
	public PixelVipBungee(PixelVip plugin){
		this.plugin = plugin;
		this.pendentBytes = new ArrayList<>();
	}
	
	@Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {		
		if (!channel.equals("PixelVipBungee")){
			return;
		}		
		if (!plugin.getPVConfig().bungeeSyncEnabled()){
			return;
		}
		
		plugin.serv.getScheduler().runTaskLater(plugin, () -> {

            DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
            try {
                String id = in.readUTF();
                if (id.equals(plugin.getPVConfig().getString("", "bungee.serverID"))){
                    return;
                }

                //operation
                String op = in.readUTF();
                if (op.equals("receive")){

                    while (in.available() > 0){
                        String a = in.readUTF();

                        if (a.equals("vips")){
                            String uuid = in.readUTF();
                            String duration = in.readUTF();
                            String group = in.readUTF();
                            String pgroup = in.readUTF();
                            String nick = in.readUTF();
                            boolean active = Boolean.getBoolean(in.readUTF());
                            String expires = in.readUTF();
                            plugin.getPVConfig().addVip(group, uuid, pgroup, Long.parseLong(duration), nick, expires);
                            plugin.getPVConfig().setVipActive(uuid, group, active);
                            plugin.getPVConfig().saveVips();
                        }
                        if (a.startsWith("keys")){
                            String key = in.readUTF();
                            String group = in.readUTF();
                            long millis = Long.parseLong(in.readUTF());
                            int uses = Integer.parseInt(in.readUTF());
                            plugin.getPVConfig().addKey(key, group, millis, uses);
                            plugin.getPVConfig().saveKeys();
                        }
                        if (a.startsWith("itemkeys")){
                            String key = in.readUTF();
                            List<String> cmds = Arrays.asList(in.readUTF().split(","));
                            plugin.getPVConfig().addItemKey(key, cmds);
                            plugin.getPVConfig().saveKeys();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, 20);
	}
	
	public void sendBungeeSync(){
		if (!plugin.getPVConfig().bungeeSyncEnabled()){
			return;
		}
		
		for (String key:plugin.getPVConfig().getListKeys()){
			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			//operation
			out.writeUTF("send");			
			out.writeUTF("keys");
			
			out.writeUTF(key);
			String[] kinfo = plugin.getPVConfig().getKeyInfo(key);			
			out.writeUTF(kinfo[0]);
			out.writeUTF(kinfo[1]);
			out.writeUTF(kinfo[2]);
			sendPendentBungee(out.toByteArray());
		}		
		for (String key:plugin.getPVConfig().getItemListKeys()){
			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			//operation
			out.writeUTF("send");
			out.writeUTF("itemkeys");
			
			out.writeUTF(key);
			List<String> kinfo = plugin.getPVConfig().getItemKeyCmds(key);
			out.writeUTF(Arrays.toString(kinfo.toArray()));
			sendPendentBungee(out.toByteArray());
		}		
		for (String uuid:plugin.getPVConfig().getAllVips().keySet()){
			ByteArrayDataOutput out = ByteStreams.newDataOutput();			
			for (String[] vip:plugin.getPVConfig().getAllVips().get(uuid)){	
				//operation
				out.writeUTF("send");
				out.writeUTF("vips");
				out.writeUTF(uuid);
				for (String vipinfo:vip){
					out.writeUTF(vipinfo);
				}	
				sendPendentBungee(out.toByteArray());
			}			
		}		
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
