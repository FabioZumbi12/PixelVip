package br.net.fabiozumbi12.pixelvip.sponge.db;

import br.net.fabiozumbi12.pixelvip.sponge.PixelVip;
import br.net.fabiozumbi12.pixelvip.sponge.config.DataCategories.ActiveVipsCategory;
import br.net.fabiozumbi12.pixelvip.sponge.config.DataCategories.KeysCategory;
import br.net.fabiozumbi12.pixelvip.sponge.config.DataCategories.TransactionsCategory;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapperFactory;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PVDataFile implements PVDataManager {

    private final PixelVip plugin;
    private ActiveVipsCategory vipRoot;
    private CommentedConfigurationNode vipConfig;
    private ConfigurationLoader<CommentedConfigurationNode> vipLoader;
    private KeysCategory keysRoot;
    private CommentedConfigurationNode keysConfig;
    private ConfigurationLoader<CommentedConfigurationNode> keysLoader;
    private TransactionsCategory transRoot;
    private CommentedConfigurationNode transConfig;
    private ConfigurationLoader<CommentedConfigurationNode> transLoader;

    public PVDataFile(PixelVip plugin, ObjectMapperFactory factory) {
        this.plugin = plugin;

        try {
            //vips file
            File vipFile = new File(PixelVip.get().configDir(), "vips.conf");
            if (!vipFile.exists()) {
                vipFile.createNewFile();
            }
            vipLoader = HoconConfigurationLoader.builder().setFile(vipFile).build();
            vipConfig = vipLoader.load(ConfigurationOptions.defaults().setObjectMapperFactory(factory).setShouldCopyDefaults(true));
            vipRoot = vipConfig.getValue(TypeToken.of(ActiveVipsCategory.class), new ActiveVipsCategory());

            //keys file
            File keysFile = new File(PixelVip.get().configDir(), "keys.conf");
            if (!keysFile.exists()) {
                keysFile.createNewFile();
            }
            keysLoader = HoconConfigurationLoader.builder().setFile(keysFile).build();
            keysConfig = keysLoader.load(ConfigurationOptions.defaults().setObjectMapperFactory(factory).setShouldCopyDefaults(true));
            keysRoot = keysConfig.getValue(TypeToken.of(KeysCategory.class), new KeysCategory());

            //transactions file
            File transFile = new File(PixelVip.get().configDir(), "transactions.conf");
            if (!transFile.exists()) {
                transFile.createNewFile();
            }
            transLoader = HoconConfigurationLoader.builder().setFile(transFile).build();
            transConfig = transLoader.load(ConfigurationOptions.defaults().setObjectMapperFactory(factory).setShouldCopyDefaults(true));
            transRoot = transConfig.getValue(TypeToken.of(TransactionsCategory.class), new TransactionsCategory());
        } catch (IOException | ObjectMappingException e) {
            e.printStackTrace();
        }
    }

    private void saveTrans() {
        try {
            transConfig.setValue(TypeToken.of(TransactionsCategory.class), transRoot);
            transLoader.save(transConfig);
        } catch (IOException | ObjectMappingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveVips() {
        try {
            vipConfig.setValue(TypeToken.of(ActiveVipsCategory.class), vipRoot);
            vipLoader.save(vipConfig);
        } catch (IOException | ObjectMappingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveKeys() {
        try {
            keysConfig.setValue(TypeToken.of(KeysCategory.class), keysRoot);
            keysLoader.save(keysConfig);
        } catch (IOException | ObjectMappingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeTrans(String payment, String trans) {
        if (transRoot.transactions.containsKey(payment))
            transRoot.transactions.get(payment).payment.remove(trans);
        saveTrans();
    }

    @Override
    public void addTras(String payment, String trans, String player) {
        transRoot.transactions.putIfAbsent(payment, new TransactionsCategory.Transactions(new HashMap<String, String>() {{
            put(trans, player);
        }}));
        saveTrans();
    }

    @Override
    public boolean transactionExist(String payment, String trans) {
        return transRoot.transactions.containsKey(payment) && transRoot.transactions.get(payment).payment.containsKey(trans);
    }

    @Override
    public HashMap<String, Map<String, String>> getAllTrans() {
        HashMap<String, Map<String, String>> trans = new HashMap<>();
        for (String payment : transRoot.transactions.keySet()) {
            trans.put(payment, transRoot.transactions.get(payment).payment);
        }
        return trans;
    }

    @Override
    public void addRawVip(String group, String uuid, List<String> pgroup, long duration, String nick, String expires) {
        addRawVip(group, uuid, pgroup, duration, nick, expires, true);
    }

    @Override
    public void addRawVip(String group, String uuid, List<String> pgroup, long duration, String nick, String expires, boolean active) {
        uuid = uuid.toLowerCase();
        if (!vipRoot.activeVips.containsKey(group)){
            vipRoot.activeVips.put(group, new HashMap<>());
        }
        vipRoot.activeVips.get(group).put(uuid, new ActiveVipsCategory.VipInfo(active, duration, expires, nick, pgroup));
        saveVips();
    }

    @Override
    public void addRawKey(String key, String group, long duration, int uses) {
        keysRoot.keys.putIfAbsent(key, new KeysCategory.KeysCat(duration, group, uses));
        saveKeys();
    }

    @Override
    public void addRawItemKey(String key, List<String> cmds) {
        keysRoot.itemKeys.put(key, cmds);
        saveKeys();
    }

    @Override
    public HashMap<String, List<String[]>> getActiveVipList() {
        HashMap<String, List<String[]>> vips = new HashMap<>();
        plugin.getConfig().getGroupList(true).stream().filter(group -> vipRoot.activeVips.containsKey(group)).forEach(group ->
                vipRoot.activeVips.get(group).forEach((uuid, value) -> {
                    List<String[]> vipInfo = getVipInfo(uuid);
                    List<String[]> activeVips = new ArrayList<>();
                    vipInfo.stream().filter(v -> v[3] != null && v[3].equals("true")).forEach(activeVips::add);
                    if (activeVips.size() > 0) {
                        vips.put(uuid, activeVips);
                    }
                }));
        return vips;
    }

    @Override
    public HashMap<String, List<String[]>> getAllVipList() {
        HashMap<String, List<String[]>> vips = new HashMap<>();
        plugin.getConfig().getGroupList(true).stream().filter(group -> vipRoot.activeVips.containsKey(group)).forEach(group ->
                vipRoot.activeVips.get(group).forEach((uuid, value) -> {
                    List<String[]> vipInfo = getVipInfo(uuid);
                    vips.put(uuid, vipInfo);
                }));
        return vips;
    }

    @Override
    public List<String[]> getVipInfo(String uuid) {
        List<String[]> vips = new ArrayList<>();
        plugin.getConfig().getGroupList(true).stream().filter(k -> vipRoot.activeVips.containsKey(k) && vipRoot.activeVips.get(k).containsKey(uuid)).forEach(key -> {
            StringBuilder builder = new StringBuilder();
            for (String str : vipRoot.activeVips.get(key).get(uuid).playerGroup) {
                builder.append(str).append(",");
            }

            String pgroup = vipRoot.activeVips.get(key).get(uuid).playerGroup.isEmpty() ? "" : vipRoot.activeVips.get(key).get(uuid).playerGroup.get(0);
            if (builder.toString().length() > 0) {
                pgroup = builder.toString().substring(0, builder.toString().length() - 1);
            }
            vips.add(new String[]{
                    String.valueOf(vipRoot.activeVips.get(key).get(uuid).duration),
                    key,
                    pgroup,
                    String.valueOf(vipRoot.activeVips.get(key).get(uuid).active),
                    vipRoot.activeVips.get(key).get(uuid).nick});
        });
        return vips;
    }

    @Override
    public List<String> getItemKeyCmds(String key) {
        return keysRoot.itemKeys.getOrDefault(key, new ArrayList<>());
    }

    @Override
    public void removeKey(String key) {
        keysRoot.keys.remove(key);
        saveKeys();
    }

    @Override
    public void removeItemKey(String key) {
        keysRoot.itemKeys.remove(key);
        saveKeys();
    }

    @Override
    public Set<String> getListKeys() {
        return keysRoot.keys.keySet();
    }

    @Override
    public Set<String> getItemListKeys() {
        return keysRoot.itemKeys.keySet();
    }

    @Override
    public String[] getKeyInfo(String key) {
        if (getListKeys().contains(key)) {
            return new String[]{keysRoot.keys.get(key).group,
                    String.valueOf(keysRoot.keys.get(key).duration),
                    String.valueOf(keysRoot.keys.get(key).uses)};
        }
        return new String[0];
    }

    @Override
    public void setKeyUse(String key, int uses) {
        keysRoot.keys.get(key).uses = uses;
        saveKeys();
    }

    @Override
    public void setVipActive(String uuid, String vip, boolean active) {
        Map<String, ActiveVipsCategory.VipInfo> play = vipRoot.activeVips.get(vip);
        ActiveVipsCategory.VipInfo vipInfo = play.get(uuid);
        vipInfo.active = active;
        saveVips();
    }

    @Override
    public void setVipDuration(String uuid, String vip, long duration) {
        vipRoot.activeVips.get(vip).get(uuid).duration = duration;
        saveVips();
    }

    @Override
    public boolean containsVip(String uuid, String vip) {
        return vipRoot.activeVips.containsKey(vip) && vipRoot.activeVips.get(vip).containsKey(uuid);
    }

    @Override
    public void setVipKitCooldown(String uuid, String vip, long cooldown) {
        vipRoot.activeVips.get(vip).get(uuid).kit_cooldown = cooldown;
        saveVips();
    }

    @Override
    public void removeVip(String uuid, String vip) {
        vipRoot.activeVips.get(vip).remove(uuid);
        saveVips();
    }

    @Override
    public long getVipCooldown(String uuid, String vip) {
        try {
            return vipRoot.activeVips.get(vip).get(uuid).kit_cooldown;
        } catch (Exception ignored) {
            return 0;
        }
    }

    @Override
    public long getVipDuration(String uuid, String vip) {
        try {
            return vipRoot.activeVips.get(vip).get(uuid).duration;
        } catch (Exception ignored) {
            return 0;
        }
    }

    @Override
    public boolean isVipActive(String uuid, String vip) {
        try {
            return vipRoot.activeVips.get(vip).get(uuid).active;
        } catch (Exception ignored) {
            return true;
        }
    }

    @Override
    public String getVipUUID(String player) {
        for (Map<String, ActiveVipsCategory.VipInfo> gp : vipRoot.activeVips.values()) {
            for (Map.Entry<String, ActiveVipsCategory.VipInfo> gpl : gp.entrySet()) {
                if (gpl.getValue().nick.equals(player)) {
                    Pattern pairRegex = Pattern.compile("\\p{XDigit}{8}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{12}");
                    Matcher matcher = pairRegex.matcher(gpl.getKey());
                    if (matcher.find()) {
                        return matcher.group(0).toLowerCase();
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void changeUUID(String oldUUID, String newUUID) {
        vipRoot.activeVips.forEach((key, value) -> {
            if (value.containsKey(oldUUID)) {
                ActiveVipsCategory.VipInfo vipInfo = value.get(oldUUID);
                vipRoot.activeVips.put(key, new HashMap<String, ActiveVipsCategory.VipInfo>(){{put(newUUID, vipInfo);}});
                value.remove(oldUUID);
            }
        });
        saveVips();
    }

    @Override
    public void closeCon() {
        //for mysql
    }
}
