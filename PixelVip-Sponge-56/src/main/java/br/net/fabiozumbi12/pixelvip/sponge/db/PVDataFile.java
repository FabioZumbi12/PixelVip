package br.net.fabiozumbi12.pixelvip.sponge.db;

import br.net.fabiozumbi12.pixelvip.sponge.PixelVip;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PVDataFile implements PVDataManager  {

    private CommentedConfigurationNode vipConfig;
    private ConfigurationLoader<CommentedConfigurationNode> vipManager;
    private final PixelVip plugin;
    public PVDataFile(PixelVip plugin) {
        this.plugin = plugin;

        File vipFile = new File(PixelVip.get().configDir(),"vips.conf");
        if (!vipFile.exists()) {
            try {
                vipFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        vipManager = HoconConfigurationLoader.builder().setFile(vipFile).build();
        try {
            vipConfig = vipManager.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveVips() {

    }

    @Override
    public void saveKeys() {

    }

    @Override
    public void removeTrans(String payment, String trans) {

    }

    @Override
    public void addTras(String payment, String trans, String player) {

    }

    @Override
    public boolean transactionExist(String payment, String trans) {
        return false;
    }

    @Override
    public HashMap<String, Map<String, String>> getAllTrans() {
        return null;
    }

    @Override
    public void addRawVip(String group, String uuid, List<String> pgroup, long duration, String nick, String expires) {

    }

    @Override
    public void addRawVip(String group, String uuid, List<String> pgroup, long duration, String nick, String expires, boolean active) {

    }

    @Override
    public void addRawKey(String key, String group, long duration, int uses) {

    }

    @Override
    public void addRawItemKey(String key, List<String> cmds) {

    }

    @Override
    public HashMap<String, List<String[]>> getActiveVipList() {
        return null;
    }

    @Override
    public HashMap<String, List<String[]>> getAllVipList() {
        return null;
    }

    @Override
    public List<String[]> getVipInfo(String uuid) {
        return null;
    }

    @Override
    public List<String> getItemKeyCmds(String key) {
        return null;
    }

    @Override
    public void removeKey(String key) {

    }

    @Override
    public void removeItemKey(String key) {

    }

    @Override
    public Set<String> getListKeys() {
        return null;
    }

    @Override
    public Set<String> getItemListKeys() {
        return null;
    }

    @Override
    public String[] getKeyInfo(String key) {
        return new String[0];
    }

    @Override
    public void setKeyUse(String key, int uses) {

    }

    @Override
    public void setVipActive(String uuid, String vip, boolean active) {

    }

    @Override
    public void setVipDuration(String uuid, String vip, long duration) {

    }

    @Override
    public boolean containsVip(String uuid, String vip) {
        return false;
    }

    @Override
    public void setVipKitCooldown(String uuid, String vip, long cooldown) {

    }

    @Override
    public void removeVip(String uuid, String vip) {

    }

    @Override
    public long getVipCooldown(String uuid, String vip) {
        return 0;
    }

    @Override
    public long getVipDuration(String uuid, String vip) {
        return 0;
    }

    @Override
    public boolean isVipActive(String uuid, String vip) {
        return false;
    }

    @Override
    public String getVipUUID(String player) {
        return null;
    }

    @Override
    public void closeCon() {

    }
}
