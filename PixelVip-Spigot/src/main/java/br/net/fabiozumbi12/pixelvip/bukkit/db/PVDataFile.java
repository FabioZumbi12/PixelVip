package br.net.fabiozumbi12.pixelvip.bukkit.db;

import br.net.fabiozumbi12.pixelvip.bukkit.PixelVip;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PVDataFile implements PVDataManager {
    private final YamlConfiguration vipsFile;
    private final YamlConfiguration keysFile;
    private final YamlConfiguration transFile;
    private final PixelVip plugin;

    public PVDataFile(PixelVip plugin) {
        this.plugin = plugin;

        File fileVips = new File(plugin.getDataFolder(), "vips.yml");
        if (!fileVips.exists()) {
            try {
                fileVips.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File fileKeys = new File(plugin.getDataFolder(), "keys.yml");
        if (!fileKeys.exists()) {
            try {
                fileKeys.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File fileTrans = new File(plugin.getDataFolder(), "transactions.yml");
        if (!fileTrans.exists()) {
            try {
                fileTrans.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        keysFile = YamlConfiguration.loadConfiguration(fileKeys);
        vipsFile = YamlConfiguration.loadConfiguration(fileVips);
        transFile = YamlConfiguration.loadConfiguration(fileTrans);
    }

    @Override
    public boolean transactionExist(String payment, String trans) {
        return this.transFile.contains(payment + "." + trans);
    }

    @Override
    public void addTras(String payment, String trans, String player) {
        this.transFile.set(payment + "." + trans, player);
        saveTrans();
    }

    @Override
    public void removeTrans(String payment, String trans) {
        this.transFile.set(payment + "." + trans, null);
        saveTrans();
    }

    @Override
    public HashMap<String, Map<String, String>> getAllTrans() {
        HashMap<String, Map<String, String>> trans = new HashMap<>();
        //payment:
        for (String payment : this.transFile.getKeys(false)) {
            //trans: player
            for (Map<?, ?> tr : this.transFile.getMapList(payment)) {
                for (Map.Entry<?, ?> tv : tr.entrySet()) {
                    trans.put(payment, new HashMap<String, String>() {{
                        put(tv.getKey().toString(), tv.getValue().toString());
                    }});
                }
            }
        }
        return trans;
    }

    private void saveTrans() {
        plugin.serv.getScheduler().runTask(plugin, () ->{
            try {
                this.transFile.save(new File(plugin.getDataFolder(), "transactions.yml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void saveKeys() {
        plugin.serv.getScheduler().runTask(plugin, () ->{
            File fileKeys = new File(plugin.getDataFolder(), "keys.yml");
            try {
                keysFile.save(fileKeys);
            } catch (IOException e) {
                e.printStackTrace();
                plugin.getPVLogger().warning("Error on save file Keys.yml, backing up latest valid config...");
                try {
                    keysFile.load(fileKeys);
                } catch (IOException | InvalidConfigurationException e1) {
                    plugin.getPVLogger().severe("Error on load latest saved file Keys.yml. Check this file for null groups or invalid formats!");
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void saveVips() {
        File fileVips = new File(plugin.getDataFolder(), "vips.yml");
        plugin.serv.getScheduler().runTask(plugin, () ->{
            try {
                vipsFile.save(fileVips);
            } catch (IOException e) {
                e.printStackTrace();
                plugin.getPVLogger().warning("Error on save file Vips.yml, backing up latest valid config...");
                try {
                    vipsFile.load(fileVips);
                } catch (IOException | InvalidConfigurationException e1) {
                    plugin.getPVLogger().severe("Error on load latest saved file Vips.yml. Check this file for null groups or invalid formats!");
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public HashMap<String, List<String[]>> getActiveVipList() {
        HashMap<String, List<String[]>> vips = new HashMap<>();
        plugin.getPVConfig().getGroupList(true).stream().filter(group -> vipsFile.getConfigurationSection("activeVips." + group) != null).forEach(group -> {
            vipsFile.getConfigurationSection("activeVips." + group).getKeys(false).forEach(uuid -> {
                List<String[]> vipInfo = getVipInfo(uuid);
                List<String[]> activeVips = new ArrayList<>();
                vipInfo.stream().filter(v -> v[3] != null && v[3].equals("true")).forEach(activeVips::add);
                if (activeVips.size() > 0)
                    vips.put(uuid, activeVips);
            });
        });
        return vips;
    }

    @Override
    public HashMap<String, List<String[]>> getAllVipList() {
        HashMap<String, List<String[]>> vips = new HashMap<>();
        plugin.getPVConfig().getGroupList(true).stream().filter(group -> vipsFile.getConfigurationSection("activeVips." + group) != null).forEach(group -> {
            vipsFile.getConfigurationSection("activeVips." + group).getKeys(false).forEach(uuid -> {
                List<String[]> vipInfo = getVipInfo(uuid);
                vips.put(uuid, vipInfo);
            });
        });
        return vips;
    }

    @Override
    public List<String[]> getVipInfo(String puuid) {
        List<String[]> vips = new ArrayList<>();
        plugin.getPVConfig().getGroupList(true).stream().filter(k -> vipsFile.get("activeVips." + k + "." + puuid + ".active") != null).forEach(key -> {
            StringBuilder builder = new StringBuilder();
            for (String str : vipsFile.getStringList("activeVips." + key + "." + puuid + ".playerGroup").stream().filter(Objects::nonNull).collect(Collectors.toList())) {
                builder.append(str).append(",");
            }

            String pgroup = vipsFile.getString("activeVips." + key + "." + puuid + ".playerGroup", "");
            if (builder.toString().length() > 0) {
                pgroup = builder.toString().substring(0, builder.toString().length() - 1);
            }
            vips.add(new String[]{
                    vipsFile.getString("activeVips." + key + "." + puuid + ".duration"),
                    key,
                    pgroup,
                    vipsFile.getString("activeVips." + key + "." + puuid + ".active"),
                    vipsFile.getString("activeVips." + key + "." + puuid + ".nick")});
        });
        return vips;
    }

    @Override
    public List<String> getItemKeyCmds(String key) {
        return keysFile.getStringList("keys.itemKeys." + key + ".cmds");
    }

    @Override
    public void removeKey(String key) {
        keysFile.set("keys.keys." + key, null);
        saveKeys();
    }

    @Override
    public void removeItemKey(String key) {
        keysFile.set("keys.itemKeys." + key, null);
        saveKeys();
    }

    @Override
    public Set<String> getListKeys() {
        if (keysFile.getConfigurationSection("keys.keys") != null) {
            return keysFile.getConfigurationSection("keys.keys").getKeys(false);
        }
        return new HashSet<>();
    }

    @Override
    public Set<String> getItemListKeys() {
        if (keysFile.getConfigurationSection("keys.itemKeys") != null) {
            return keysFile.getConfigurationSection("keys.itemKeys").getKeys(false);
        }
        return new HashSet<>();
    }

    @Override
    public void addRawVip(String group, String id, List<String> pgroup, long duration, String nick, String expires, boolean active) {
        id = id.toLowerCase();
        vipsFile.set("activeVips." + group + "." + id + ".active", active);
        addRawVip(group, id, pgroup, duration, nick, expires);
    }

    @Override
    public void addRawVip(String group, final String id, List<String> pgroup, long duration, String nick, String expires) {
        List<String> plGroup = pgroup.stream().filter(Objects::nonNull).collect(Collectors.toList());
        vipsFile.set("activeVips." + group + "." + id.toLowerCase() + ".playerGroup", plGroup);
        vipsFile.set("activeVips." + group + "." + id.toLowerCase() + ".duration", duration);
        vipsFile.set("activeVips." + group + "." + id.toLowerCase() + ".nick", nick);
        vipsFile.set("activeVips." + group + "." + id.toLowerCase() + ".expires-on-exact", expires);
        vipsFile.set("activeVips." + group + "." + id.toLowerCase() + ".active", true);

        // Set player last groups
        vipsFile.getConfigurationSection("activeVips").getKeys(true)
                .stream().filter(k -> k.contains(id) && k.contains("playerGroup")).forEach(k -> {
                    vipsFile.set("activeVips." + k, new ArrayList<>(plGroup));
                });
        saveVips();
    }

    @Override
    public void addRawKey(String key, String group, long duration, int uses, boolean unique) {
        keysFile.set("keys.keys." + key + ".group", group);
        keysFile.set("keys.keys." + key + ".duration", duration);
        keysFile.set("keys.keys." + key + ".uses", uses);
        keysFile.set("keys.keys." + key + ".unique", unique);
        saveKeys();
    }

    @Override
    public void addRawItemKey(String key, List<String> cmds) {
        keysFile.set("keys.itemKeys." + key + ".cmds", cmds);
        saveKeys();
    }

    @Override
    public String[] getKeyInfo(String key) {
        if (getListKeys().contains(key)) {
            return new String[]{keysFile.getString("keys.keys." + key + ".group"),
                    keysFile.getString("keys.keys." + key + ".duration"),
                    keysFile.getString("keys.keys." + key + ".uses"),
                    keysFile.getString("keys.keys." + key + ".unique", "false")};
        }
        return new String[0];
    }

    @Override
    public void setKeyUse(String key, int uses) {
        keysFile.set("keys.keys." + key + ".uses", uses);
    }

    @Override
    public void setVipActive(String id, String vip, boolean active) {
        vipsFile.set("activeVips." + vip + "." + id.toLowerCase() + ".active", active);
        saveVips();
    }

    @Override
    public void setVipDuration(String id, String vip, long duration) {
        vipsFile.set("activeVips." + vip + "." + id.toLowerCase() + ".duration", duration);
        saveVips();
    }

    @Override
    public boolean containsVip(String id, String vip) {
        return vipsFile.isConfigurationSection("activeVips." + vip + "." + id);
    }

    @Override
    public void setVipKitCooldown(String id, String vip, long cooldown) {
        vipsFile.set("activeVips." + vip + "." + id + ".kit-cooldown", cooldown);
        saveVips();
    }

    @Override
    public void removeVip(String id, String vip) {
        vipsFile.set("activeVips." + vip + "." + id, null);
        saveVips();
    }

    @Override
    public long getVipCooldown(String id, String vip) {
        return vipsFile.getLong("activeVips." + vip + "." + id + ".kit-cooldown", 0);
    }

    @Override
    public long getVipDuration(String id, String vip) {
        return vipsFile.getLong("activeVips." + vip + "." + id + ".duration");
    }

    @Override
    public boolean isVipActive(String uuid, String vip) {
        return vipsFile.getBoolean("activeVips." + vip + "." + uuid + ".active", true);
    }

    @Override
    public void closeCon() {
        //for mysql
    }

    @Override
    public void changeUUID(String oldUUID, String newUUID) {
        plugin.getPVConfig().getGroupList(true).stream().filter(k -> vipsFile.contains("activeVips." + k + "." + oldUUID)).forEach(key -> {
            ConfigurationSection config = vipsFile.getConfigurationSection("activeVips." + key + "." + oldUUID);
            vipsFile.set("activeVips." + key + "." + oldUUID, null);
            vipsFile.set("activeVips." + key + "." + newUUID, config);
        });
    }

    @Override
    public String getVipUUID(String player) {
        Iterator<String> it = vipsFile.getKeys(true).stream().filter(key -> key.contains(".nick")).iterator();
        while (it.hasNext()) {
            String key = it.next();
            if (Objects.equals(vipsFile.getString(key), player)) {
                Pattern pairRegex = Pattern.compile("\\p{XDigit}{8}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{12}");
                Matcher matcher = pairRegex.matcher(key);
                if (matcher.find()) {
                    return matcher.group(0).toLowerCase();
                }
            }
        }
        return null;
    }
}
