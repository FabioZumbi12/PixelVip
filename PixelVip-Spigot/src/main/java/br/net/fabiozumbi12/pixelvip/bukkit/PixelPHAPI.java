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
                text = plugin.getUtil().expiresOn(new Long(exp));
            }
            if (arg.equals("expiration_desc")) {
                text = plugin.getUtil().millisToMessage(new Long(exp) - plugin.getUtil().getNowMillis());
            }
            if (arg.equals("active_vip")) {
                text = vipInfo[1];
            }
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
}
