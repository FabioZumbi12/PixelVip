package br.net.fabiozumbi12.pixelvip.sponge.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.*;

@ConfigSerializable
public class MainCategory {
    public MainCategory() {}

    @Setting(comment = "Put your PEX(or your permission plugin) vip group names here. Case sensitive!")
    public Map<String, VipsCategory> groups = defaultVips();

    private Map<String, VipsCategory> defaultVips(){
        Map<String, VipsCategory> map = new HashMap<>();
        map.put("vip-demo", new VipsCategory());
        return map;
    }

    @Setting
    public PaymentCategory apis = new PaymentCategory();

    @Setting
    public ConfigsCat configs = new ConfigsCat();

    @ConfigSerializable
    public static class ConfigsCat {

        @Setting(comment = "Command to run when a vip is removed by command.")
        public String cmdOnRemoveVip = "pex user {p} parent delete group {vip}";

        @Setting(comment = "Command to reload the permissions plugin after some action.")
        public String cmdToReloadPermPlugin = "";

        @Setting(comment = "Run this commands on player change your vip to other.\n" +
                "Variables: {p} get the player name, {newvip} get the new vip, {oldvip} get the vip group before change.")
        public List<String> commandsToRunOnChangeVip = Arrays.asList("pex user {p} parent add group {newvip}",
                "pex user {p} parent delete group {oldvip}");

        @Setting(comment = "Run this commands when the vip of a player finish.\n" +
                "Variables: {p} get the player name, {vip} get the actual vip, {playergroup} get the group before the player activate your vip.")
        public List<String> commandsToRunOnVipFinish = Arrays.asList("pex user {p} parent delete group {vip}",
                "pex user {p} parent add group {playergroup}");

        @Setting(value = "key-size",comment = "Sets the length of your vip keys.")
        public int key_size = 10;

        @Setting(comment = "Should we alert the player about free inventory space before use the key?")
        public boolean useKeyWarning = true;

        @Setting
        public boolean queueCmdsForOfflinePlayers = false;

        @Setting
        public List<String> worldCmdsAllowed = new ArrayList<>();

        @Setting
        public DatabaseCategory database = new DatabaseCategory();

        @Setting
        public String clickSuggest = "/usekey {key}";
    }
    
    @Setting
    public Map<String, JoinCmdsCat> joinCmds;

    @ConfigSerializable
    public static class JoinCmdsCat {
        public JoinCmdsCat(){}

        public JoinCmdsCat(List<String> cmds, List<String> chanceCmds){
            this.cmds = cmds;
            this.chanceCmds = chanceCmds;
        }

        @Setting
        public List<String> cmds;
        @Setting
        public List<String> chanceCmds;
    }

    @Setting
    public Map<String, String> strings = defStrings();

    private Map<String, String> defStrings(){
        Map<String, String> map = new HashMap<>();
        map.put("_plugintag","&7[&6PixelVip&7] ");
        map.put("activeDays","&b- Days: &6{days} &bdays");
        map.put("activeVip","&b- Vip: &6{vip}");
        map.put("activeVipSetTo","&aYour active VIP is ");
        map.put("and"," &band");
        map.put("confirmUsekey","&4Warning: &cMake sure you have free space on your inventory to use this key for your vip or items. &6Use the same command again to confirm!");
        map.put("days"," &bdays");
        map.put("hours"," &bhours");
        map.put("infoUses","&b- Uses left: &6");
        map.put("invalidKey","&cThis key is invalid or not exists!");
        map.put("item","&a-- Item: &b");
        map.put("itemsAdded","&aItem(s) added to key: ");
        map.put("itemsGiven","&aGiven {items} item(s) using a key.");
        map.put("keyGenerated","&aGenerated a key with the following: ");
        map.put("keyRemoved","&aKey removed with success: &b");
        map.put("lessThan","&6Less than one minute to end your vip...");
        map.put("list_of_vips","&aList of active VIPs: ");
        map.put("listItemKeys","&aList of Item Keys: ");
        map.put("listKeys","&aList of Keys: ");
        map.put("minutes"," &bminutes");
        map.put("moreThanZero","&cThe days need to be more than 0");
        map.put("noGroups","&cTheres no groups with name ");
        map.put("noKeyRemoved","&cTheres no groups with name ");
        map.put("noKeys","&aTheres no available keys! Use &6/newkey &ato generate one.");
        map.put("noPlayersByName","&cTheres no players with this name!");
        map.put("onlyPlayers","&cOnly players ca use this command!");
        map.put("playerNotVip","&cThis player(or you) is not VIP!");
        map.put("sync_groups","&aGroup configs send to all servers!");
        map.put("timeActive","&b- In Use: &6");
        map.put("timeGroup","&b- Vip: &6");
        map.put("timeKey","&b- Key: &6");
        map.put("timeLeft","&b- Time left: &6");
        map.put("totalTime","&b- Days: &6");
        map.put("usesLeftActivation","&bThis key can be used for more: &6{uses} &btimes.");
        map.put("vipActivated","&aVip activated with success: ");
        map.put("vipAdded","&aVip added with success for this player!");
        map.put("vipEnded"," &bYour vip &6{vip} &bhas ended. &eWe hope you enjoyed your Vip time &a:D");
        map.put("vipInfoFor","&aVip info for ");
        map.put("vipSet","&aVip set with success for this player!");
        map.put("vipsRemoved","&aVip(s) of player removed with success!");
        map.put("wait-cmd","&cWait before use a pixelvip command again!");

        map.put("pay-waiting","&c{payment}: Your purchase has not yet been approved!");
        map.put("pay-codeused","&c{payment}: This code has already been used!");
        map.put("pay-expired","&c{payment}: This code has expired!");
        map.put("pay-noitems","&c{payment}: No items delivered. Contact an administrator to help you!");
        return map;
    }
}


