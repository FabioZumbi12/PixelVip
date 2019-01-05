package br.net.fabiozumbi12.pixelvip.bukkit.db;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public interface PVDataManager {
    void saveVips();

    void saveKeys();

    void removeTrans(String trans);

    void addTras(String trans, String player);

    boolean transactionExist(String trans);

    HashMap<String, String> getAllTrans();

    void addRawVip(String group, String uuid, List<String> pgroup, long duration, String nick, String expires);

    void addRawVip(String group, String uuid, List<String> pgroup, long duration, String nick, String expires, boolean active);

    void addRawKey(String key, String group, long duration, int uses);

    void addRawItemKey(String key, List<String> cmds);

    HashMap<String, List<String[]>> getActiveVipList();

    HashMap<String, List<String[]>> getAllVipList();

    List<String[]> getVipInfo(String uuid);

    List<String> getItemKeyCmds(String key);

    void removeKey(String key);

    void removeItemKey(String key);

    Set<String> getListKeys();

    Set<String> getItemListKeys();

    String[] getKeyInfo(String key);

    void setKeyUse(String key, int uses);

    void setVipActive(String uuid, String vip, boolean active);

    void setVipDuration(String uuid, String vip, long duration);

    boolean containsVip(String uuid, String vip);

    void setVipKitCooldown(String uuid, String vip, long cooldown);

    void removeVip(String uuid, String vip);

    long getVipCooldown(String uuid, String vip);

    long getVipDuration(String uuid, String vip);

    boolean isVipActive(String uuid, String vip);

    String getVipUUID(String player);

    void closeCon();
}
