package br.net.fabiozumbi12.pixelvip.sponge.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.*;

@ConfigSerializable
public class MainCategory {
    public MainCategory() {}

    @Setting(comment = "Put your permissions plugin vip group names here. Case sensitive!")
    public Map<String, VipsCategory> groups = new HashMap<>();
    @Setting
    public PaymentCategory apis = new PaymentCategory();
    @Setting
    public ConfigsCat configs = new ConfigsCat();
    @Setting
    public Map<String, JoinCmdsCat> joinCmds;

    @Setting
    public StringsCat strings = new StringsCat();
    @ConfigSerializable
    public static class StringsCat {
        @Setting
        public String _pluginTag = "&7[&6PixelVip&7] ";
        @Setting
        public String activeDays = "&b- Days: &6{days} &bdays";
        @Setting
        public String activeVip = "&b- Vip: &6{vip}";
        @Setting
        public String activeVipSetTo = "&aYour active VIP is ";
        @Setting
        public String and = " &band";
        @Setting(value = "true")
        public String _true = "&atrue";
        @Setting(value = "false")
        public String _false = "&cfalse";
        @Setting
        public String confirmUsekey = "&4Warning: &cMake sure you have free space on your inventory to use this key for your vip or items. &6Use the same command again to confirm!";
        @Setting
        public String days = " &bdays";
        @Setting
        public String hours = " &bhours";
        @Setting
        public String infoUses = "&b- Uses left: &6";
        @Setting
        public String invalidKey = "&cThis key is invalid or not exists!";
        @Setting
        public String item = "&a-- Item: &b";
        @Setting
        public String itemsAdded = "&aItem(s) added to key: ";
        @Setting
        public String itemsGiven = "&aGiven {items} item(s) using a key.";
        @Setting
        public String keyGenerated = "&aGenerated a key with the following: ";
        @Setting
        public String keyRemoved = "&aKey removed with success: &b";
        @Setting
        public String lessThan = "&6Less than one minute to end your vip...";
        @Setting(value = "list-of-vips")
        public String list_of_vips = "&aList of active VIPs: ";
        @Setting
        public String listItemKeys = "&aList of Item Keys: ";
        @Setting
        public String listKeys = "&aList of Keys: ";
        @Setting
        public String minutes = " &bminutes";
        @Setting
        public String moreThanZero = "&cThe days need to be more than 0";
        @Setting
        public String noGroups = "&cTheres no groups with name ";
        @Setting
        public String noKeyRemoved = "&cTheres no groups with name ";
        @Setting
        public String noKeys = "&aTheres no available keys! Use &6/newkey &ato generate one.";
        @Setting
        public String noPlayersByName = "&cTheres no players with this name!";
        @Setting
        public String onlyPlayers = "&cOnly players can use this command!";
        @Setting
        public String playerNotVip = "&cThis player(or you) is not VIP!";
        @Setting
        public String playerNotGroup = "&cYou don't have this vip activated!";
        @Setting(value = "sync-groups")
        public String sync_groups = "&aGroup configs send to all servers!";
        @Setting
        public String timeActive = "&b- In Use: &6";
        @Setting
        public String timeGroup = "&b- Vip: &6";
        @Setting
        public String timeKey = "&b- Key: &6";
        @Setting
        public String timeLeft = "&b- Time left: &6";
        @Setting
        public String totalTime = "&b- Days: &6";
        @Setting
        public String usesLeftActivation = "&bThis key can be used for more: &6{uses} &btimes.";
        @Setting
        public String vipActivated = "&aVip activated with success: ";
        @Setting
        public String vipAdded = "&aVip added with success for this player!";
        @Setting
        public String vipEnded = " &bYour vip &6{vip} &bhas ended. &eWe hope you enjoyed your Vip time &a:D";
        @Setting
        public String vipInfoFor = "&aVip info for ";
        @Setting
        public String vipSet = "&aVip set with success for this player!";
        @Setting
        public String vipsRemoved = "&aVip(s) of player removed with success!";
        @Setting(value = "wait-cmd")
        public String wait_cmd = "&cWait before use a pixelvip command again!";
        @Setting
        public String hoverKey = "&7&o(Click to get the Key)&r";

        @Setting(value = "pay-waiting")
        public String pay_waiting = "&c{payment}: Your purchase has not yet been approved!";
        @Setting(value = "pay-codeused")
        public String pay_codeused = "&c{payment}: This code has already been used!";
        @Setting(value = "pay-expired")
        public String pay_expired = "&c{payment}: This code has expired!";
        @Setting(value = "pay-noitems")
        public String pay_noitems = "&c{payment}: No items delivered. Code: {transaction} - Print this message and send to an Administrator!";
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

        @Setting
        public String title = "&4Vip Example";

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


