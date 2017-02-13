package br.net.fabiozumbi12.PixelVip;

import me.clip.placeholderapi.external.EZPlaceholderHook;

import org.bukkit.entity.Player;

public class PixelPHAPI extends EZPlaceholderHook{
	
	private PixelVip plugin;

	public PixelPHAPI(PixelVip plugin, String placeholderName) {
		super(plugin, "pixelvip");
		this.plugin = plugin;
	}
	
	@Override
	public String onPlaceholderRequest(Player arg0, String arg) {
		if (arg.startsWith("expiration_millis_")){
			String playName = arg.replace("expiration_millis_", "");			
			return plugin.getPVConfig().getActiveVipInfo(playName)[0];
		}
		if (arg.startsWith("expiration_desc_")){
			String playName = arg.replace("expiration_desc_", "");			
			return plugin.getUtil().expiresOn(Long.getLong(plugin.getPVConfig().getActiveVipInfo(playName)[0]));
		}
		if (arg.startsWith("active_vip_")){
			String playName = arg.replace("active_vip_", "");			
			return plugin.getPVConfig().getActiveVipInfo(playName)[1];
		}
		return null;
	}

}
