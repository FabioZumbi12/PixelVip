package br.net.fabiozumbi12.pixelvip.bukkit;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

public class PixelPHAPI extends PlaceholderExpansion {

    private PixelVip plugin;

    PixelPHAPI(PixelVip plugin) {
        this.plugin = plugin;
    }

    @Override
    public String onRequest(OfflinePlayer p, String arg) {
        String text = "--";
        String[] vipInfo = plugin.getPVConfig().getActiveVipInfo(p.getName());
        if (vipInfo[0] != null) {
            String exp = vipInfo[0];
            if (arg.equals("expiration_millis")) {
                text = exp;
            }
            if (arg.equals("expiration_date")) {
                text = plugin.getUtil().expiresOn(Long.parseLong(exp));
            }
            if (arg.equals("expiration_desc")) {
                text = plugin.getUtil().millisToMessage(Long.parseLong(exp) - plugin.getUtil().getNowMillis());
            }
            if (arg.equals("active_vip") && vipInfo[1] != null) {
                text = vipInfo[1];
            }
        }
        var list = plugin.getPVConfig().getVipInfo(p.getName());
        if (!list.isEmpty()){
            if (arg.equals("inactive")) {
                list.removeIf(v -> !v[3].equals("true"));
            }
            var b = new StringBuilder();
            var sep = "";
            for (var l : list){
                if (l[1] != null){
                    b.append(sep);
                    b.append(l[1]);
                    sep = ", ";
                }
            }
            text = b.toString();
        }
        return text;
    }

    @Override
    public String getIdentifier() {
        return "pixelvip";
    }

    @Override
    public String getAuthor() {
        return "FabioZumbi12";
    }

    @Override
    public String getVersion() {
        return plugin.pdf.getVersion();
    }

    @Override
    public boolean canRegister(){
        return true;
    }
    
    @Override
    public boolean persist() {
        return true;
    }
}
