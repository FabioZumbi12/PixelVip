package br.net.fabiozumbi12.pixelvip.bukkit;

import br.net.fabiozumbi12.pixelvip.bukkit.bungee.SpigotText;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class PVUtil {
    private PixelVip plugin;

    public PVUtil(PixelVip plugin) {
        this.plugin = plugin;
    }

    public String toColor(String str) {
        return str.replaceAll("(&([Aa-fFkK-oOrR0-9]))", "\u00A7$2");
    }

    public String removeColor(String str) {
        return str.replaceAll("(&([Aa-fFkK-oOrR0-9]))", "");
    }

    public long getNowMillis() {
        Calendar cal = Calendar.getInstance();
        return cal.getTimeInMillis();
    }

    public long dayToMillis(Long days) {
        return TimeUnit.DAYS.toMillis(days);
    }

    public long millisToDay(String millis) {
        return TimeUnit.MILLISECONDS.toDays(Long.valueOf(millis));
    }

    public long millisToDay(Long millis) {
        return TimeUnit.MILLISECONDS.toDays(millis);
    }

    public void sendHoverKey(CommandSender sender, String key) {
        try {
            if (plugin.getPVConfig().getBoolean(true, "configs.spigot.clickKeySuggest") && sender instanceof Player) {
                SpigotText text = new SpigotText();
                text.setText(plugin.getUtil().toColor(plugin.getPVConfig().getLang("timeKey") + key + " " + plugin.getPVConfig().getLang("hoverKey")));
                text.setHover(plugin.getUtil().toColor(plugin.getPVConfig().getLang("hoverKey")));
                text.setClick(plugin.getPVConfig().getString("/usekey ", "configs.spigot.clickSuggest").replace("{key}", key));
                text.sendMessage(sender);
            } else {
                sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("timeKey") + key));
            }
        } catch (NoSuchMethodError e) {
            sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("timeKey") + key));
        }
    }

    public String genKey(int length) {
        char[] chartset = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
        Random random = new SecureRandom();
        char[] result = new char[length];
        for (int i = 0; i < result.length; i++) {
            int randomCharIndex = random.nextInt(chartset.length);
            result[i] = chartset[randomCharIndex];
        }
        return new String(result);
    }

    public boolean sendVipTime(CommandSender src, String UUID, String name) {
        List<String[]> vips = plugin.getPVConfig().getVipInfo(UUID);
        if (vips.size() > 0) {
            src.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "vipInfoFor") + name + ":"));
            src.sendMessage(plugin.getUtil().toColor("&b---------------------------------------------"));
            vips.stream().filter(v -> v.length == 5).forEach((vipInfo) -> {
                String time = plugin.getUtil().millisToMessage(Long.valueOf(vipInfo[0]));
                if (plugin.getPVConfig().isVipActive(vipInfo[1], UUID)) {
                    time = plugin.getUtil().millisToMessage(Long.valueOf(vipInfo[0]) - plugin.getUtil().getNowMillis());
                }
                src.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("timeLeft") + time));
                src.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("timeGroup") + plugin.getPVConfig().getVipTitle(vipInfo[1])));
                src.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("timeActive") + plugin.getPVConfig().getLang(vipInfo[3])));
                src.sendMessage(plugin.getUtil().toColor("&b---------------------------------------------"));
            });
            return true;
        } else {
            src.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "playerNotVip")));
            return false;
        }
    }

    public String millisToMessage(long millis) {
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        long hour = TimeUnit.MILLISECONDS.toHours(millis - TimeUnit.DAYS.toMillis(days));
        long min = TimeUnit.MILLISECONDS.toMinutes((millis - TimeUnit.DAYS.toMillis(days)) - TimeUnit.HOURS.toMillis(hour));
        StringBuilder msg = new StringBuilder();
        if (days > 0) {
            msg.append("&6").append(days).append(plugin.getPVConfig().getLang("days")).append(", ");
        }
        if (hour > 0) {
            msg.append("&6").append(hour).append(plugin.getPVConfig().getLang("hours")).append(", ");
        }
        if (min > 0) {
            msg.append("&6").append(min).append(plugin.getPVConfig().getLang("minutes")).append(", ");
        }
        try {
            msg = msg.replace(msg.lastIndexOf(","), msg.lastIndexOf(",") + 1, ".").replace(msg.lastIndexOf(","), msg.lastIndexOf(",") + 1, plugin.getPVConfig().getLang("and"));
        } catch (StringIndexOutOfBoundsException ex) {
            return plugin.getPVConfig().getLang("lessThan");
        }
        return msg.toString();
    }

    public String expiresOn(Long millis) {
        Date date = new Date(millis);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return sdf.format(date);
    }

    public void ExecuteCmd(String cmd, Player player) {
        if (cmd == null || cmd.isEmpty()) return;
        if (player != null) cmd = cmd.replace("{p}", player.getName());

        plugin.addLog("Running Command - \"" + cmd + "\"");
        String finalCmd = cmd;
        Bukkit.getScheduler().runTask(plugin, () -> plugin.serv.dispatchCommand(plugin.serv.getConsoleSender(), finalCmd));
    }

    public boolean paymentItems(HashMap<String, Integer> items, Player player, String payment, String transCode) {
        int log = 0;
        for (Map.Entry<String, Integer> item : items.entrySet()) {
            int multipl = item.getValue();
            String key = item.getKey();

            plugin.getPVLogger().severe("Value: " + item.getValue() + " | " + item.getKey());

            for (int i = 0; i < multipl; i++) {
                String cmd = "givepackage " + player.getName() + " " + key;
                ExecuteCmd(cmd, null);
                plugin.addLog("API:" + payment + " | " + player.getName() + " | Item Cmd:" + cmd + " | Transaction Code: " + transCode);
                log++;
            }
        }

        if (log == 0) {
            player.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "payment.noitems")
                    .replace("{payment}", payment)
                    .replace("{transaction}", transCode)));
            return false;
        }
        return true;
    }
}
