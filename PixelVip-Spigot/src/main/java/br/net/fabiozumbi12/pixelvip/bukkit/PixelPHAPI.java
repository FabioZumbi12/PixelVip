package br.net.fabiozumbi12.pixelvip.bukkit;

import me.clip.placeholderapi.external.EZPlaceholderHook;

import org.bukkit.entity.Player;

public class PixelPHAPI extends EZPlaceholderHook{
	
	private PixelVip plugin;

	public PixelPHAPI(PixelVip plugin) {
		super(plugin, "pixelvip");
		this.plugin = plugin;
	}
	
	@Override
	public String onPlaceholderRequest(Player p, String arg) {
		String text = "--";
		String[] vipInfo = plugin.getPVConfig().getActiveVipInfo(p.getName());
		if (vipInfo[0] != null){
			String exp = vipInfo[0];
			if (arg.equals("expiration_millis")){		
				text = exp;
			}
			if (arg.equals("expiration_date")){		
				text = plugin.getUtil().expiresOn(new Long(exp));
			}
			if (arg.equals("expiration_desc")){		
				text = plugin.getUtil().millisToMessage(new Long(exp)-plugin.getUtil().getNowMillis());
			}
			if (arg.equals("active_vip")){		
				text = vipInfo[1];
			}
		}		
		return text;
	}

}
