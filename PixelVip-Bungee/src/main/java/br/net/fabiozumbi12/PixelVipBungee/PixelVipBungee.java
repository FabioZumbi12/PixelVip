package br.net.fabiozumbi12.PixelVipBungee;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class PixelVipBungee extends Plugin implements Listener {

	@Override
    public void onEnable() {
		getProxy().registerChannel("PixelVipBungee");
		getProxy().getPluginManager().registerListener(this, this);
        getLogger().info("PixelVip Bungee Bridge enabled!");        
    }
	
	@EventHandler	
	public void onPluginMessage(PluginMessageEvent e) {
		if (!e.getTag().equals("PixelVipBungee")){
			return;
		}
		
		ByteArrayInputStream stream = new ByteArrayInputStream(e.getData());
	    DataInputStream in = new DataInputStream(stream);
	    
	    String id;
	    String op;
	    try {
	    	id = in.readUTF();
	    	op = in.readUTF();
	    	
	    	if (op.equals("send")){
	    		sendMessage(id,"receive", in);
	    	}
	    } catch (IOException ex){
	    	ex.printStackTrace();
	    }	    		
	}
	
	private void sendMessage(String id, String op, DataInputStream in){	    
	    ByteArrayOutputStream stream = new ByteArrayOutputStream();
	    DataOutputStream out = new DataOutputStream(stream);
	    	    
	    try {
	    	out.writeUTF(op);
	    	
	    	while (in.available() > 0){
	    		out.writeUTF(in.readUTF());
	    	}
	    		    	
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    getLogger().info("Sender: "+id);
	    for (ServerInfo si:getProxy().getServers().values()){	    	
	    	if (si.getName().equalsIgnoreCase(id)){
	    		continue;
	    	}
	    	si.sendData("PixelVipBungee", stream.toByteArray());
	    }	    
	}
}
