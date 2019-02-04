package br.net.fabiozumbi12.pixelvip.sponge;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class PVUtil {
    private PixelVip plugin;

    public PVUtil(PixelVip plugin) {
        this.plugin = plugin;
    }

    public Text toText(String str) {
        return TextSerializers.FORMATTING_CODE.deserialize(str);
    }

    public String toColor(String str) {
        return str.replaceAll("(&([a-fk-or0-9]))", "\u00A7$2");
    }

    public long getNowMillis() {
        Calendar cal = Calendar.getInstance();
        return cal.getTimeInMillis();
    }

    public long dayToMillis(Long days) {
        return TimeUnit.DAYS.toMillis(days);
    }

    public long millisToDay(String millis) {
        return TimeUnit.MILLISECONDS.toDays(new Long(millis));
    }

    public long millisToDay(Long millis) {
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

    public Optional<User> getUser(String name) {
        UserStorageService uss = Sponge.getGame().getServiceManager().provide(UserStorageService.class).get();
        return uss.get(name);
    }

    public Optional<User> getUser(UUID uuid) {
        UserStorageService uss = Sponge.getGame().getServiceManager().provide(UserStorageService.class).get();
        return uss.get(uuid);
    }

    public Text sendVipTime(CommandSource src, String UUID, String name) {
        List<String[]> vips = plugin.getConfig().getVipInfo(UUID);
        if (vips.size() > 0) {
            src.sendMessage(plugin.getUtil().toText(plugin.getConfig().getLang("_pluginTag", "vipInfoFor") + name + ":"));
            src.sendMessage(plugin.getUtil().toText("&b---------------------------------------------"));
            vips.stream().filter(v -> v.length == 5).forEach((vipInfo) -> {
                String time = plugin.getUtil().millisToMessage(new Long(vipInfo[0]));
                if (plugin.getConfig().isVipActive(vipInfo[1], UUID)) {
                    time = plugin.getUtil().millisToMessage(new Long(vipInfo[0]) - plugin.getUtil().getNowMillis());
                }
                src.sendMessage(plugin.getUtil().toText(plugin.getConfig().getLang("timeLeft") + time));
                src.sendMessage(plugin.getUtil().toText(plugin.getConfig().getLang("timeGroup") + vipInfo[1]));
                src.sendMessage(plugin.getUtil().toText(plugin.getConfig().getLang("timeActive") + vipInfo[3]));
                src.sendMessage(plugin.getUtil().toText("&b---------------------------------------------"));
            });
            return Text.of();
        } else {
            return plugin.getUtil().toText(plugin.getConfig().getLang("_pluginTag", "playerNotVip"));
        }
    }

    public String millisToMessage(long millis) {
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        long hour = TimeUnit.MILLISECONDS.toHours(millis - TimeUnit.DAYS.toMillis(days));
        long min = TimeUnit.MILLISECONDS.toMinutes((millis - TimeUnit.DAYS.toMillis(days)) - TimeUnit.HOURS.toMillis(hour));
        StringBuilder msg = new StringBuilder();
        if (days > 0) {
            msg.append("&6" + days + plugin.getConfig().getLang("days") + ", ");
        }
        if (hour > 0) {
            msg.append("&6" + hour + plugin.getConfig().getLang("hours") + ", ");
        }
        if (min > 0) {
            msg.append("&6" + min + plugin.getConfig().getLang("minutes") + ", ");
        }
        try {
            msg = msg.replace(msg.lastIndexOf(","), msg.lastIndexOf(",") + 1, ".").replace(msg.lastIndexOf(","), msg.lastIndexOf(",") + 1, plugin.getConfig().getLang("and"));
        } catch (StringIndexOutOfBoundsException ex) {
            return plugin.getConfig().getLang("lessThan");
        }
        return msg.toString();
    }

    public String expiresOn(Long millis) {
        Date date = new Date(millis);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        return sdf.format(date);
    }

    public void sendHoverKey(CommandSource src, String key) {
        Text text = Text.builder()
                .append(Text.of(plugin.getUtil().toColor(plugin.getConfig().getLang("timeKey") + key + " " + plugin.getConfig().getLang("hoverKey"))))
                .onHover(TextActions.showText(Text.of(plugin.getUtil().toColor(plugin.getConfig().getLang("hoverKey")))))
                .onClick(TextActions.suggestCommand(plugin.getConfig().root().configs.clickSuggest.replace("{key}", key))).toText();
        src.sendMessage(text);
    }

    public boolean paymentItems(HashMap<Integer, String> items, Player player, String payment, String transCode) {
        int log = 0;
        for (Map.Entry<Integer, String> item : items.entrySet()) {
            int multipl = item.getKey();
            String key = item.getValue();

            for (int i = 0; i < multipl; i++) {
                String cmd = "givepackage " + player.getName() + " " + key;
                ExecuteCmd(cmd, null);
                plugin.addLog("API:" + payment + " | " + player.getName() + " | Item Cmd:" + cmd + " | Transaction Code: " + transCode);
                log++;
            }
        }

        if (log == 0) {
            player.sendMessage(toText(plugin.getUtil().toColor(plugin.getConfig().getLang("_pluginTag", "pay-noitems")
                    .replace("{payment}", payment)
                    .replace("{transaction}", transCode))));
            return false;
        }
        return true;
    }

    public void ExecuteCmd(String cmd, Player player) {
        if (cmd == null || cmd.isEmpty()) return;
        if (player != null) cmd = cmd.replace("{p}", player.getName());

        plugin.addLog("Running Command - \"" + cmd + "\"");
        String finalCmd = cmd;
        Sponge.getScheduler().createTaskBuilder().execute(() -> Sponge.getCommandManager().process(Sponge.getServer().getConsole(), finalCmd))
                .submit(plugin);
    }
}
