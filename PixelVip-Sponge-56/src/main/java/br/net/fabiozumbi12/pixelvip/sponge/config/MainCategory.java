package br.net.fabiozumbi12.pixelvip.sponge.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.*;

@ConfigSerializable
public class MainCategory {
    @Setting(comment = "Put your permissions plugin vip group names here. Case sensitive!")
    public Map<String, VipsCategory> groups = defaultVips();
    @Setting
    public PaymentCategory apis = new PaymentCategory();
    @Setting
    public ConfigsCat configs = new ConfigsCat();
    @Setting
    public Map<String, JoinCmdsCat> joinCmds;
    @Setting
    public Map<String, String> strings = defStrings();

    public MainCategory() {
    }

    private Map<String, VipsCategory> defaultVips() {
        Map<String, VipsCategory> map = new HashMap<>();
        map.put("vip-demo", new VipsCategory());
        return map;
    }

    private Map<String, String> defStrings() {
        Map<String, String> map = new HashMap<>();
        map.put("_pluginTag", "&7[&6PixelVip&7] ");
        map.put("activeDays", "&b- Days: &6{days} &bdays");
        map.put("activeVip", "&b- Vip: &6{vip}");
        map.put("activeVipSetTo", "&aYour active VIP is ");
        map.put("and", " &band");
        map.put("confirmUsekey", "&4Warning: &cMake sure you have free space on your inventory to use this key for your vip or items. &6Use the same command again to confirm!");
        map.put("days", " &bdays");
        map.put("hours", " &bhours");
        map.put("infoUses", "&b- Uses left: &6");
        map.put("invalidKey", "&cThis key is invalid or not exists!");
        map.put("item", "&a-- Item: &b");
        map.put("itemsAdded", "&aItem(s) added to key: ");
        map.put("itemsGiven", "&aGiven {items} item(s) using a key.");
        map.put("keyGenerated", "&aGenerated a key with the following: ");
        map.put("keyRemoved", "&aKey removed with success: &b");
        map.put("lessThan", "&6Less than one minute to end your vip...");
        map.put("list-of-vips", "&aList of active VIPs: ");
        map.put("listItemKeys", "&aList of Item Keys: ");
        map.put("listKeys", "&aList of Keys: ");
        map.put("minutes", " &bminutes");
        map.put("moreThanZero", "&cThe days need to be more than 0");
        map.put("noGroups", "&cTheres no groups with name ");
        map.put("noKeyRemoved", "&cTheres no groups with name ");
        map.put("noKeys", "&aTheres no available keys! Use &6/newkey &ato generate one.");
        map.put("noPlayersByName", "&cTheres no players with this name!");
        map.put("onlyPlayers", "&cOnly players ca use this command!");
        map.put("playerNotVip", "&cThis player(or you) is not VIP!");
        map.put("sync-groups", "&aGroup configs send to all servers!");
        map.put("timeActive", "&b- In Use: &6");
        map.put("timeGroup", "&b- Vip: &6");
        map.put("timeKey", "&b- Key: &6");
        map.put("timeLeft", "&b- Time left: &6");
        map.put("totalTime", "&b- Days: &6");
        map.put("usesLeftActivation", "&bThis key can be used for more: &6{uses} &btimes.");
        map.put("vipActivated", "&aVip activated with success: ");
        map.put("vipAdded", "&aVip added with success for this player!");
        map.put("vipEnded", " &bYour vip &6{vip} &bhas ended. &eWe hope you enjoyed your Vip time &a:D");
        map.put("vipInfoFor", "&aVip info for ");
        map.put("vipSet", "&aVip set with success for this player!");
        map.put("vipsRemoved", "&aVip(s) of player removed with success!");
        map.put("wait-cmd", "&cWait before use a pixelvip command again!");

        map.put("pay-waiting", "&c{payment}: Your purchase has not yet been approved!");
        map.put("pay-codeused", "&c{payment}: This code has already been used!");
        map.put("pay-expired", "&c{payment}: This code has expired!");
        map.put("pay-noitems", "&c{payment}: No items delivered. Code: {transaction} - Print this message and send to an Administrator!");
        return map;
    }

    @ConfigSerializable
    public static class VipsCategory {

        @Setting(comment = "Add the commands to run when the player use the key for activation \n" +
                "You can use the variables:\n" +
                "{p} = Player name, {vip} = Vip group, {days} = Vip days, {playergroup} = Player group before activate vip")
        public List<String> commands = Arrays.asList(
                "broadcast &aThe player &6{p} &ahas acquired your &6{vip} &afor &6{days} &adays",
                "give {p} minecraft:diamond 10",
                "eco add {p} 10000");

        @Setting(comment = "Add commands here to give items to players based on chances.\n" +
                "Use 1 - 100 for add chance commands.")
        public Map<String, List<String>> cmdChances = cmdChanges();
        @Setting(value = "run-on-vip-end")
        public List<String> run_on_vip_end = new ArrayList<>();

        private Map<String, List<String>> cmdChanges() {
            Map<String, List<String>> map = new HashMap<>();
            map.put("30", Collections.singletonList("give {p} minecraft:mob_spawner 1"));
            map.put("50", Collections.singletonList("give {p} minecraft:diamond_block 5"));
            return map;
        }
    }

    @ConfigSerializable
    public static class ConfigsCat {

        @Setting(comment = "Command to run when a vip is removed by command.")
        public String cmdOnRemoveVip = "lp user {p} parent remove {vip}";

        @Setting(comment = "Command to reload the permissions plugin after some action.")
        public String cmdToReloadPermPlugin = "";

        @Setting(comment = "Run this commands on player change your vip to other.\n" +
                "Variables: {p} get the player name, {newvip} get the new vip, {oldvip} get the vip group before change.")
        public List<String> commandsToRunOnChangeVip = Arrays.asList("lp user {p} parent set {newvip}",
                "lp user {p} parent remove {oldvip}");

        @Setting(comment = "Run this commands when the vip of a player finish.\n" +
                "Variables: {p} get the player name, {vip} get the actual vip, {playergroup} get the group before the player activate your vip.")
        public List<String> commandsToRunOnVipFinish = Arrays.asList("lp user {p} parent remove {vip}",
                "lp user {p} parent set {playergroup}");

        @Setting(value = "key-size", comment = "Sets the length of your vip keys.")
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

    @ConfigSerializable
    public static class JoinCmdsCat {
        @Setting
        public List<String> cmds;
        @Setting
        public List<String> chanceCmds;

        public JoinCmdsCat() {
        }
        public JoinCmdsCat(List<String> cmds, List<String> chanceCmds) {
            this.cmds = cmds;
            this.chanceCmds = chanceCmds;
        }
    }
}


