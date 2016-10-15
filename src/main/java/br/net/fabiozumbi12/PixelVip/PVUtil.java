package br.net.fabiozumbi12.PixelVip;

import java.security.SecureRandom;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class PVUtil {
	private PixelVip plugin;

	public PVUtil(PixelVip plugin){
		this.plugin = plugin;
	}
	/*
	public Text toColor(String str){
    	return TextSerializers.FORMATTING_CODE.deserialize(str);
    }
	*/
	public String toColor(String str){
    	return str.replaceAll("(&([a-fk-or0-9]))", "\u00A7$2"); 
    }
	
	public long getNowMillis(){
		Calendar cal = Calendar.getInstance();
		return cal.getTimeInMillis();
    }
	
	public long dayToMillis(Long days){
		return TimeUnit.DAYS.toMillis(days);
	}
	
	public long millisToDay(String millis){
		return TimeUnit.MILLISECONDS.toDays(new Long(millis));
	}
	
	public long millisToDay(Long millis){
		return TimeUnit.MILLISECONDS.toDays(millis);
	}
	
	public String genKey(int length) {
	    char[] chartset = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
		Random random = new SecureRandom();
	    char[] result = new char[length];
	    for (int i = 0; i < result.length; i++) {
	        // picks a random index out of character set > random character
	        int randomCharIndex = random.nextInt(chartset.length);
	        result[i] = chartset[randomCharIndex];
	    }
	    return new String(result);
	}
	
	@SuppressWarnings("deprecation")
	public OfflinePlayer getUser(String name){			
		return plugin.serv.getOfflinePlayer(name);		
	}
	
	public OfflinePlayer getUser(UUID uuid){
		return plugin.serv.getOfflinePlayer(uuid);		
	}
	
	public boolean sendVipTime(CommandSender src, String UUID, String name) {	
		List<String[]> vips = plugin.getPVConfig().getVipInfo(UUID);
		if (vips.size() > 0){
			src.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","vipInfoFor")+name+":"));
			src.sendMessage(plugin.getUtil().toColor("&b---------------------------------------------"));
			vips.stream().filter(v->v.length == 4).forEach((vipInfo)->{
				String time = plugin.getUtil().millisToMessage(new Long(vipInfo[0]));
				if (plugin.getPVConfig().getBoolean(true, "activeVips."+vipInfo[1]+"."+UUID.toString()+".active")){
					time = plugin.getUtil().millisToMessage(new Long(vipInfo[0])-plugin.getUtil().getNowMillis());
				}
		    	src.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("timeLeft")+time));
		    	src.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("timeGroup")+vipInfo[1]));		
		    	src.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("timeActive")+vipInfo[3]));	
		    	src.sendMessage(plugin.getUtil().toColor("&b---------------------------------------------"));
			});			
			return true;
		} else {
			src.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","playerNotVip")));	
			return false;
		}
	}
	
	public String millisToMessage(long millis){		
		long days = TimeUnit.MILLISECONDS.toDays(millis);
		long hour = TimeUnit.MILLISECONDS.toHours(millis-TimeUnit.DAYS.toMillis(days));
		long min = TimeUnit.MILLISECONDS.toMinutes((millis-TimeUnit.DAYS.toMillis(days))-TimeUnit.HOURS.toMillis(hour));
		StringBuilder msg = new StringBuilder();
		if (days > 0){
			msg.append("&6"+days+plugin.getPVConfig().getLang("days")+", ");
		}
		if (hour > 0 ){
			msg.append("&6"+hour+plugin.getPVConfig().getLang("hours")+", ");
		}
		if (min > 0){
			msg.append("&6"+min+plugin.getPVConfig().getLang("minutes")+", ");
		}
		try{
			msg = msg.replace(msg.lastIndexOf(","), msg.lastIndexOf(",")+1, ".").replace(msg.lastIndexOf(","), msg.lastIndexOf(",")+1, plugin.getPVConfig().getLang("and"));
		} catch(StringIndexOutOfBoundsException ex){
			return plugin.getPVConfig().getLang("lessThan");
		}		
		return msg.toString();
	}
}
