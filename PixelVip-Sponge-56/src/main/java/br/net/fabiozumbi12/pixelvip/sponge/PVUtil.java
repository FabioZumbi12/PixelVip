package br.net.fabiozumbi12.pixelvip.sponge;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
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

    public CommandResult sendVipTime(CommandSource src, String UUID, String name) throws CommandException {
        List<String[]> vips = plugin.getConfig().getVipInfo(UUID);
        if (vips.size() > 0) {
            src.sendMessage(plugin.getUtil().toText(plugin.getConfig().getLang("_pluginTag", "vipInfoFor") + name + ":"));
            src.sendMessage(plugin.getUtil().toText("&b---------------------------------------------"));
            vips.stream().filter(v -> v.length == 5).forEach((vipInfo) -> {
                String time = plugin.getUtil().millisToMessage(new Long(vipInfo[0]));
                if (plugin.getConfig().getVipBoolean(true, "activeVips", vipInfo[1], UUID.toString(), "active")) {
                    time = plugin.getUtil().millisToMessage(new Long(vipInfo[0]) - plugin.getUtil().getNowMillis());
                }
                src.sendMessage(plugin.getUtil().toText(plugin.getConfig().getLang("timeLeft") + time));
                src.sendMessage(plugin.getUtil().toText(plugin.getConfig().getLang("timeGroup") + vipInfo[1]));
                src.sendMessage(plugin.getUtil().toText(plugin.getConfig().getLang("timeActive") + vipInfo[3]));
                src.sendMessage(plugin.getUtil().toText("&b---------------------------------------------"));
            });
            return CommandResult.success();
        } else {
            throw new CommandException(plugin.getUtil().toText(plugin.getConfig().getLang("_pluginTag", "playerNotVip")));
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
}
