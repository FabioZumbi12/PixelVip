package br.net.fabiozumbi12.pixelvip.bukkit.config;

import br.net.fabiozumbi12.pixelvip.bukkit.PixelVip;
import br.net.fabiozumbi12.pixelvip.bukkit.db.PVDataFile;
import br.net.fabiozumbi12.pixelvip.bukkit.db.PVDataManager;
import br.net.fabiozumbi12.pixelvip.bukkit.db.PVDataMysql;
import com.earth2me.essentials.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public class PVConfig {

    public HashMap<String, String> comandAlert = new HashMap<>();
    private PixelVip plugin;
    private int delay = 0;
    private PVDataManager dataManager;
    private CommentedConfig comConfig;
    private CommentedConfig apisConfig;

    public PVConfig(PixelVip plugin) {
        this.plugin = plugin;

        /*----------------------------------------------------------------------------------*/

        String header = "=============== PixelVip Configuration Options ================\n" +
                "The configuration is commented! If you need more help or have issues, use our github:\n" +
                "https://github.com/FabioZumbi12/PixelVip\n" +
                "\n" +
                "Pixelvip by FabioZumbi12";

        comConfig = new CommentedConfig(new File(plugin.getDataFolder(), "config.yml"), plugin.getConfig(), header);

        comConfig.setDefault("groups", null, "Group names like is in your permissions plugin (case sensitive)!\n" +
                "Available placeholders: \n" +
                "- {p} = Players Name\n" +
                "- {vip} = Vip Group\n" +
                "- {playergroup} = Player Group before Vip activation\n" +
                "- {days} = Days of activated Vip");
        if (comConfig.configurations.contains("groups") && !comConfig.configurations.getConfigurationSection("groups").getKeys(false).isEmpty()) {
            comConfig.configurations.getConfigurationSection("groups").getKeys(false).forEach(g -> {
                comConfig.setDefault("groups." + g + ".title", g);
                comConfig.setDefault("groups." + g + ".commands", new ArrayList<>());
                comConfig.setDefault("groups." + g + ".cmdChances.0", new ArrayList<>());
                comConfig.setDefault("groups." + g + ".run-on-vip-finish", new ArrayList<>());
                comConfig.setDefault("groups." + g + ".essentials-kit", g);
            });
        } else {
            comConfig.setDefault("groups.vipExample", null, "This is an Example of vip group properties.\nCopy or use this as example to setups all your other groups.");
            comConfig.setDefault("groups.vipExample.essentials-kit", "ExampleKit", "Put the Essentials kit name to freeze the kit time when this vip is not in use.\nThis is anti-exploit.");
            comConfig.setDefault("groups.vipExample.title", "&bVip Example", "Title to use on commands and to show on chat.");
            comConfig.setDefault("groups.vipExample.commands", Arrays.asList("broadcast &aThe player &6{p} &ahas acquired your &6{vip} &afor &6{days} &adays", "give {p} minecraft:diamond 10", "eco give {p} 10000"),
                    "Add the commands to run when the player use the key for activation \n" +
                            "You can use the variables:\n" +
                            "{p} = Player name, {vip} = Vip group, {days} = Vip days, {playergroup} = Player group before activate vip");
            comConfig.setDefault("groups.vipExample.cmdChances", null,
                    "Add commands here to give items to players based on chances.\n" +
                            "Use 1 - 100 for add chance commands.");
            comConfig.setDefault("groups.vipExample.cmdChances.50", Collections.singletonList("give {p} minecraft:diamond_block 5"));
            comConfig.setDefault("groups.vipExample.cmdChances.30", Collections.singletonList("give {p} minecraft:mob_spawner 1"));
            comConfig.setDefault("groups.vipExample.run-on-vip-finish", Collections.singletonList("broadcast [Example message from PixelVip on run-on-vip-finish] The vip of {p} (Vip {vip}) has ended and now is back to {playergroup}!"), "Commands to run on this vip ends.");
        }

        //database
        comConfig.setDefault("configs.database.type", "file", "Options: \"file\" or \"mysql\"");
        comConfig.setDefault("configs.database.mysql", null, "Database configuration!\n" +
                "H2 uri: \"jdbc:h2:%s/pixelvip.db;mode=MySQL\" (%s will be replaced by pixelvip path)\n" +
                "Mysql uri: \"jdbc:mysql://localhost:3306/\"");
        comConfig.setDefault("configs.database.mysql.host", "jdbc:mysql://localhost:3306/");
        comConfig.setDefault("configs.database.mysql.db-name", "pixelvip");
        comConfig.setDefault("configs.database.mysql.username", "user");
        comConfig.setDefault("configs.database.mysql.password", "pass");

        comConfig.setDefault("configs.database.mysql.keys.table-name", "pixelvip_keys");
        comConfig.setDefault("configs.database.mysql.keys.columns.key", "col_key");
        comConfig.setDefault("configs.database.mysql.keys.columns.group", "col_group");
        comConfig.setDefault("configs.database.mysql.keys.columns.duration", "col_duration");
        comConfig.setDefault("configs.database.mysql.keys.columns.uses", "col_uses");
        comConfig.setDefault("configs.database.mysql.keys.columns.cmds", "col_cmds");
        comConfig.setDefault("configs.database.mysql.keys.columns.info", "col_info");
        comConfig.setDefault("configs.database.mysql.keys.columns.comments", "col_comments");

        comConfig.setDefault("configs.database.mysql.vips.table-name", "pixelvip_vips");
        comConfig.setDefault("configs.database.mysql.vips.columns.uuid", "col_uuid");
        comConfig.setDefault("configs.database.mysql.vips.columns.vip", "col_vip");
        comConfig.setDefault("configs.database.mysql.vips.columns.playerGroup", "col_playerGroup");
        comConfig.setDefault("configs.database.mysql.vips.columns.duration", "col_duration");
        comConfig.setDefault("configs.database.mysql.vips.columns.nick", "col_nick");
        comConfig.setDefault("configs.database.mysql.vips.columns.expires-on-exact", "col_expires");
        comConfig.setDefault("configs.database.mysql.vips.columns.active", "col_active");
        comConfig.setDefault("configs.database.mysql.vips.columns.kits", "col_kits");
        comConfig.setDefault("configs.database.mysql.vips.columns.comments", "col_comments");

        comConfig.setDefault("configs.database.mysql.transactions.table-name", "pixelvip_transactions");
        comConfig.setDefault("configs.database.mysql.transactions.columns.idt", "col_idt");
        comConfig.setDefault("configs.database.mysql.transactions.columns.payment", "col_payment");
        comConfig.setDefault("configs.database.mysql.transactions.columns.nick", "col_nick");
        //end database

        try {
            plugin.serv.spigot();
            comConfig.setDefault("configs.spigot.clickKeySuggest", true);
        } catch (NoSuchMethodError e) {
            comConfig.setDefault("configs.spigot.clickKeySuggest", false);
        }
        comConfig.setDefault("configs.spigot.clickSuggest", "/usekey {key}");

        comConfig.setDefault("configs.key-size", 10, "Sets the length of your vip keys.");

        comConfig.setDefault("configs.useKeyWarning", true, "Should we alert the player about free inventory space before use the key?");

        comConfig.setDefault("configs.Vault.use", true);
        comConfig.setDefault("configs.Vault.mode", "set");

        comConfig.setDefault("configs.cmdToReloadPermPlugin", "", "Command to reload the permissions plugin after some action.");
        comConfig.setDefault("configs.cmdOnRemoveVip", "lp user {p} parent remove {vip}", "Command to run when a vip is removed by command.");
        comConfig.setDefault("configs.commandsToRunOnVipFinish", Collections.singletonList("nick {p} off"),
                "Run this commands when the vip of a player finish.\n" +
                        "Variables: {p} get the player name, {vip} get the actual vip, {playergroup} get the group before the player activate your vip.");
        comConfig.setDefault("configs.commandsToRunOnChangeVip", Arrays.asList("lp user {p} parent set {newvip}",
                "lp user {p} parent remove {oldvip}"),
                "Run this commands on player change your vip to other.\n" +
                        "Variables: {p} get the player name, {newvip} get the new vip, {oldvip} get the vip group before change.");
        comConfig.setDefault("configs.queueCmdsForOfflinePlayers", false);
        Set<String> worlds = new HashSet<>(comConfig.configurations.getStringList("configs.worldCmdsAllowed"));
        for (World w : Bukkit.getWorlds()) {
            worlds.add(w.getName());
        }
        comConfig.setDefault("configs.worldCmdsAllowed", new ArrayList<>(worlds));
        comConfig.setDefault("bungee.enableSync", false);
        comConfig.setDefault("bungee.serverID", "server1");


        //strings
        comConfig.setDefault("strings._pluginTag", "&7[&6PixelVip&7] ");
        comConfig.setDefault("strings.noPlayersByName", "&cTheres no players with this name!");
        comConfig.setDefault("strings.onlyPlayers", "&cOnly players ca use this command!");
        comConfig.setDefault("strings.noKeys", "&aTheres no available keys! Use &6/newkey &aor &6/newikey &ato generate one.");
        comConfig.setDefault("strings.listKeys", "&aList of Keys:");
        comConfig.setDefault("strings.listItemKeys", "&aList of Item Keys:");
        comConfig.setDefault("strings.vipInfoFor", "&aVip info for ");
        comConfig.setDefault("strings.playerNotVip", "&cThis player(or you) is not VIP!");
        comConfig.setDefault("strings.moreThanZero", "&cThis number need to be more than 0");
        comConfig.setDefault("strings.keyGenerated", "&aGenerated a key with the following:");
        comConfig.setDefault("strings.keySendTo", "&aYou received a key with the following:");
        comConfig.setDefault("strings.invalidKey", "&cThis key is invalid or not exists!");
        comConfig.setDefault("strings.vipActivated", "&aVip activated with success:");
        comConfig.setDefault("strings.usesLeftActivation", "&bThis key can be used for more: &6{uses} &btimes.");
        comConfig.setDefault("strings.activeVip", "&b- Vip: &6{vip}");
        comConfig.setDefault("strings.activeDays", "&b- Days: &6{days} &bdays");
        comConfig.setDefault("strings.timeLeft", "&b- Time left: &6");
        comConfig.setDefault("strings.totalTime", "&b- Days: &6");
        comConfig.setDefault("strings.timeKey", "&b- Key: &6");
        comConfig.setDefault("strings.hoverKey", "&7&o(Click to get the Key)&r");
        comConfig.setDefault("strings.timeGroup", "&b- Vip: &6");
        comConfig.setDefault("strings.timeActive", "&b- In Use: &6");
        comConfig.setDefault("strings.infoUses", "&b- Uses left: &6");
        comConfig.setDefault("strings.activeVipSetTo", "&aYour active VIP is ");
        comConfig.setDefault("strings.noGroups", "&cNo groups with name &6");
        comConfig.setDefault("strings.days", " &bdays");
        comConfig.setDefault("strings.hours", " &bhours");
        comConfig.setDefault("strings.minutes", " &bminutes");
        comConfig.setDefault("strings.and", " &band");
        comConfig.setDefault("strings.vipEnded", " &bYour vip &6{vip} &bhas ended. &eWe hope you enjoyed your Vip time &a:D");
        comConfig.setDefault("strings.lessThan", "&6Less than one minute to end your vip...");
        comConfig.setDefault("strings.vipsRemoved", "&aVip(s) of player removed with success!");
        comConfig.setDefault("strings.vipSet", "&aVip set with success for this player!");
        comConfig.setDefault("strings.sync-groups", "&aGroup configs send to all servers!");
        comConfig.setDefault("strings.list-of-vips", "&aList of active VIPs: ");
        comConfig.setDefault("strings.vipAdded", "&aVip added with success for this player!");
        comConfig.setDefault("strings.item", "&a-- Item: &b");
        comConfig.setDefault("strings.itemsGiven", "&aGiven {items} item(s) using a key.");
        comConfig.setDefault("strings.itemsAdded", "&aItem(s) added to key:");
        comConfig.setDefault("strings.keyRemoved", "&aKey removed with success: &b");
        comConfig.setDefault("strings.noKeyRemoved", "&cTheres no keys to remove!");
        comConfig.setDefault("strings.cmdNotAllowedWorld", "&cThis command is not allowed in this world!");
        comConfig.setDefault("strings.true", "&atrue");
        comConfig.setDefault("strings.false", "&cfalse");
        comConfig.setDefault("strings.reload", "&aPixelvip reloaded with success!");
        comConfig.setDefault("strings.wait-cmd", "&cWait before use a pixelvip command again!");
        comConfig.setDefault("strings.confirmUsekey", "&4Warning: &cMake sure you have free space on your inventory to use this key for your vip or items. &6Use the same command again to confirm!");
        comConfig.setDefault("strings.pendent", "&cYou have some pendent activation(s) to use. Please select one before continue!");

        comConfig.setDefault("strings.payment.waiting", "&c{payment}: Your purchase has not yet been approved!");
        comConfig.setDefault("strings.payment.codeused", "&c{payment}: This code has already been used!");
        comConfig.setDefault("strings.payment.expired", "&c{payment}: This code has expired!");
        comConfig.setDefault("strings.payment.noitems", "&c{payment}: No items delivered. Code: {transaction} - Print this message and send to an Administrator!");
        /*---------------------------------------------------------*/

        // Apis configs
        String apiHeader = "=============== PixelVip Payment APIs Options ================\n" +
                "The configuration is commented! If you need more help or have issues, use our github:\n" +
                "https://github.com/FabioZumbi12/PixelVip/wiki/(2)-Payments-APIs\n" +
                "\n" +
                "Pixelvip by FabioZumbi12";
        apisConfig = new CommentedConfig(new File(plugin.getDataFolder(), "apis.yml"), new YamlConfiguration(), apiHeader);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        apisConfig.setDefault("apis.in-test", false, "Set this to true until you is testing the APIs.\n" +
                "In test, we will not save the transaction codes.\n" +
                "DONT FORGET TO SET THIS TO FALSE WHEN DONE YOUR TESTS!!");

        apisConfig.setDefault("apis.pagseguro", null, "Wiki: https://github.com/FabioZumbi12/PixelVip/wiki/(2)-Payments-APIs#pagseguro-brazil");
        apisConfig.setDefault("apis.pagseguro.use", false);
        apisConfig.setDefault("apis.pagseguro.sandbox", false);
        apisConfig.setDefault("apis.pagseguro.email", "your@email.com");
        apisConfig.setDefault("apis.pagseguro.token", "yourtoken");
        apisConfig.setDefault("apis.pagseguro.ignoreOldest", sdf.format(Calendar.getInstance().getTime()));
        apisConfig.setDefault("apis.pagseguro.product-id-location", "ID", "Define se a identificação do produto vai ser pelo ID ou pela descrição.\n" +
                "As opções são: \"ID\" ou \"DESCRICAO\"\n" +
                "ID: Iremos verificar o REF, SKU ou ID do produto\n" +
                "DESCRICAO: Iremos verificar se na descrição do produto, a primeira palavra é o id, ou se o id esta no meio da descrição iniciado com #\n" +
                "Exemplo com código de pacote do PixelVip 999: \n" +
                " - \"999 - Vip4 Elite\"\n" +
                " - \"Vip4 Elite #999\"");


        apisConfig.setDefault("apis.mercadopago", null, "Wiki: https://github.com/FabioZumbi12/PixelVip/wiki/(2)-Payments-APIs#mercadopago");
        apisConfig.setDefault("apis.mercadopago.use", false);
        apisConfig.setDefault("apis.mercadopago.sandbox", false);
        apisConfig.setDefault("apis.mercadopago.access-token", "ACCESS-TOKEN");
        apisConfig.setDefault("apis.mercadopago.ignoreOldest", sdf.format(Calendar.getInstance().getTime()));
        apisConfig.setDefault("apis.mercadopago.product-id-location", "ID", "Define se a identificação do produto vai ser pelo ID ou pela descrição.\n" +
                "As opções são: \"ID\" ou \"DESCRICAO\"\n" +
                "ID: Iremos verificar o REF, SKU ou ID do produto\n" +
                "DESCRICAO: Iremos verificar se na descrição do produto, a primeira palavra é o id, ou se o id esta no meio da descrição iniciado com #\n" +
                "Exemplo com código de pacote do PixelVip 999: \n" +
                " - \"999 - Vip4 Elite\"\n" +
                " - \"Vip4 Elite #999\"");


        apisConfig.setDefault("apis.paypal", null, "Wiki: https://github.com/FabioZumbi12/PixelVip/wiki/(2)-Payments-APIs#paypal");
        apisConfig.setDefault("apis.paypal.use", false);
        apisConfig.setDefault("apis.paypal.sandbox", false);
        apisConfig.setDefault("apis.paypal.username", "username");
        apisConfig.setDefault("apis.paypal.password", "password");
        apisConfig.setDefault("apis.paypal.signature", "signature");
        apisConfig.setDefault("apis.paypal.ignoreOldest", sdf.format(Calendar.getInstance().getTime()));
        apisConfig.setDefault("apis.paypal.product-id-location", "ID", "Define se a identificação do produto vai ser pelo ID ou pela descrição.\n" +
                "As opções são: \"ID\" ou \"DESCRICAO\"\n" +
                "ID: Iremos verificar o REF, SKU ou ID do produto\n" +
                "DESCRICAO: Iremos verificar se na descrição do produto, a primeira palavra é o id, ou se o id esta no meio da descrição iniciado com #\n" +
                "Exemplo com código de pacote do PixelVip 999: \n" +
                " - \"999 - Vip4 Elite\"\n" +
                " - \"Vip4 Elite #999\"");

        if (comConfig.configurations.getConfigurationSection("apis") != null) {
            plugin.getPVLogger().warning("APIs configurations moved to 'apis.yml'");
            comConfig.configurations.getConfigurationSection("apis").getKeys(true).forEach((keys -> {
                apisConfig.configurations.set("apis." + keys, comConfig.configurations.get("apis." + keys));
            }));

            comConfig.configurations.set("apis", null);
        }
        apisConfig.saveConfig();

        //init database
        reloadVips();

        /*---------------------------------------------------------*/
        //move vips to new file if is in config.yml

        if (comConfig.configurations.getConfigurationSection("activeVips") != null) {
            plugin.getPVLogger().warning("Active Vips moved to file 'vips.yml'");
            comConfig.configurations.getConfigurationSection("activeVips").getKeys(false).forEach((group -> {
                comConfig.configurations.getConfigurationSection("activeVips." + group).getKeys(false).forEach((id) -> {
                    dataManager.addRawVip(group, id,
                            Arrays.asList(comConfig.configurations.getString("activeVips." + group + "." + id + ".playerGroup").split(",")),
                            comConfig.configurations.getLong("activeVips." + group + "." + id + ".duration"),
                            comConfig.configurations.getString("activeVips." + group + "." + id + ".nick"),
                            comConfig.configurations.getString("activeVips." + group + "." + id + ".expires-on-exact"));
                    dataManager.setVipActive(id, group, comConfig.configurations.getBoolean("activeVips." + group + "." + id + ".active"));
                });
            }));

            comConfig.configurations.set("activeVips", null);
            saveVips();
        }

        /*---------------------------------------------------------*/

        /*---------------------------------------------------------*/
        //move keys to new file if is in config.yml

        if (comConfig.configurations.getConfigurationSection("keys") != null) {
            plugin.getPVLogger().warning("keys moved to file 'keys.yml'");
            comConfig.configurations.getConfigurationSection("keys").getKeys(false).forEach((key) -> {
                dataManager.addRawKey(key,
                        comConfig.configurations.getString("keys." + key + ".group"),
                        comConfig.configurations.getLong("keys." + key + ".duration"),
                        comConfig.configurations.getInt("keys." + key + ".uses"));
            });

            comConfig.configurations.set("keys", null);
            saveKeys();
        }

        if (comConfig.configurations.getConfigurationSection("itemKeys") != null) {
            plugin.getPVLogger().warning("itemKeys moved to file 'keys.yml'");
            comConfig.configurations.getConfigurationSection("itemKeys").getKeys(false).forEach((key) -> {
                dataManager.addRawItemKey(key, comConfig.configurations.getStringList("itemKeys." + key + ".cmds"));
            });

            comConfig.configurations.set("itemKeys", null);
            saveKeys();
        }

        /*---------------------------------------------------------*/

        comConfig.saveConfig();
    }

    public FileConfiguration getRoot() {
        return this.comConfig.configurations;
    }

    public FileConfiguration getApiRoot() {
        return this.apisConfig.configurations;
    }

    public CommentedConfig getCommConfig() {
        return this.comConfig;
    }

    public String getVipTitle(String vipGroup) {
        return ChatColor.translateAlternateColorCodes('&', comConfig.configurations.getString("groups." + vipGroup + ".title", vipGroup));
    }

    public String getVipByTitle(String vipTitle) {
        vipTitle = vipTitle.replace("_", " ");
        for (String group : comConfig.configurations.getConfigurationSection("groups").getKeys(false)) {
            if (!vipTitle.isEmpty() && comConfig.configurations.getString("groups." + group + ".title") != null &&
                    plugin.getUtil().removeColor(comConfig.configurations.getString("groups." + group + ".title")).equalsIgnoreCase(vipTitle))
                return group;
        }
        return vipTitle;
    }

    public void changeUUIDs(String oldUUID, String newUUID) {
        dataManager.changeUUID(oldUUID, newUUID);
    }

    public void reloadVips() {
        if (dataManager != null) {
            dataManager.closeCon();
        }
        if (comConfig.configurations.getString("configs.database.type").equalsIgnoreCase("mysql")) {
            dataManager = new PVDataMysql(plugin);
        } else {
            dataManager = new PVDataFile(plugin);
        }
    }

    public boolean transExist(String payment, String trans) {
        return dataManager.transactionExist(payment, trans);
    }

    public void addTrans(String payment, String trans, String player) {
        if (apisConfig.configurations.getBoolean("apis.in-test"))
            return;
        dataManager.addTras(payment, trans, player);
    }

    public HashMap<String, Map<String, String>> getAllTrans() {
        return dataManager.getAllTrans();
    }

    public boolean worldAllowed(World w) {
        return comConfig.configurations.getStringList("configs.worldCmdsAllowed").contains(w.getName());
    }

    public List<String> getItemKeyCmds(String key) {
        return dataManager.getItemKeyCmds(key);
    }

    public void saveKeys() {
        dataManager.saveKeys();
    }

    public void saveVips() {
        dataManager.saveVips();
    }

    public void closeCon() {
        if (dataManager != null) {
            dataManager.closeCon();
        }
    }

    public boolean isVipActive(String vip, String id) {
        return dataManager.isVipActive(id, vip);
    }

    public boolean bungeeSyncEnabled() {
        return getBoolean(false, "bungee.enableSync");
    }

    public boolean queueCmds() {
        return getBoolean(false, "configs.queueCmdsForOfflinePlayers");
    }

    /*
     * For BungeeCord
     */
    public void setVipActive(String uuid, String vip, boolean active) {
        dataManager.setVipActive(uuid, vip, active);
    }

    public List<String> getQueueCmds(String uuid) {
        List<String> cmds = new ArrayList<>();
        if (comConfig.configurations.contains("joinCmds." + uuid + ".cmds")) {
            cmds.addAll(comConfig.configurations.getStringList("joinCmds." + uuid + ".cmds"));
        }
        if (comConfig.configurations.contains("joinCmds." + uuid + ".chanceCmds")) {
            cmds.addAll(comConfig.configurations.getStringList("joinCmds." + uuid + ".chanceCmds"));
        }
        comConfig.setDefault("joinCmds." + uuid, null);
        comConfig.saveConfig();
        return cmds;
    }

    private void setJoinCmds(String uuid, List<String> cmds, List<String> chanceCmds) {
        comConfig.setDefault("joinCmds." + uuid + ".cmds", cmds);
        comConfig.setDefault("joinCmds." + uuid + ".chanceCmds", chanceCmds);
        comConfig.saveConfig();
    }

    public void addKey(String key, String group, long millis, int uses) {
        dataManager.addRawKey(key, group, millis, uses);
        saveConfigAll();
    }

    public void addItemKey(String key, List<String> cmds) {
        cmds.addAll(dataManager.getItemKeyCmds(key));
        dataManager.addRawItemKey(key, cmds);
        saveConfigAll();
    }

    private void saveConfigAll() {
        saveVips();
        saveKeys();
        comConfig.saveConfig();
        plugin.getPVBungee().sendBungeeSync();
    }

    public boolean delItemKey(String key) {
        if (dataManager.getItemListKeys().contains(key)) {
            dataManager.removeItemKey(key);
            saveConfigAll();
            return true;
        } else {
            return false;
        }
    }

    public boolean delKey(String key, int uses) {
        if (dataManager.getListKeys().contains(key)) {
            if (uses <= 1) {
                dataManager.removeKey(key);
            } else {
                dataManager.setKeyUse(key, uses - 1);
            }
            saveConfigAll();
            return true;
        } else {
            return false;
        }
    }

    public boolean activateVip(OfflinePlayer p, String key, String group, long days, String pname) {

        boolean hasItemkey = dataManager.getItemListKeys().contains(key);
        if (hasItemkey) {
            StringBuilder cmdsBuilder = new StringBuilder();
            List<String> cmds = dataManager.getItemKeyCmds(key);
            for (String cmd : cmds) {
                cmdsBuilder.append(cmd).append(", ");
                plugin.serv.getScheduler().runTaskLater(plugin, () -> {
                    String cmdf = cmd.replace("{p}", p.getName());
                    if (p.isOnline()) {
                        plugin.getUtil().ExecuteCmd(cmdf, null);
                    }
                }, delay * 2);
                delay++;
            }
            dataManager.removeItemKey(key);
            saveConfigAll();

            p.getPlayer().sendMessage(plugin.getUtil().toColor(getLang("_pluginTag", "itemsGiven").replace("{items}", cmds.size() + "")));

            String cmdBuilded = cmdsBuilder.toString();
            plugin.addLog("ItemKey | " + p.getName() + " | " + key + " | Cmds: " + cmdBuilded.substring(0, cmdBuilded.length() - 2));
        }
        if (dataManager.getKeyInfo(key).length == 3) {
            String[] keyinfo = dataManager.getKeyInfo(key);
            int uses = Integer.parseInt(keyinfo[2]);

            delKey(key, uses);

            p.getPlayer().sendMessage(plugin.getUtil().toColor("&b---------------------------------------------"));
            if (uses - 1 > 0) {
                p.getPlayer().sendMessage(plugin.getUtil().toColor(getLang("_pluginTag", "usesLeftActivation").replace("{uses}", "" + (uses - 1))));
            }
            enableVip(p, keyinfo[0], new Long(keyinfo[1]), pname, key);
            return true;
        } else if (!group.equals("")) {
            enableVip(p, group, plugin.getUtil().dayToMillis(days), pname, key);
            return true;
        } else {
            if (!hasItemkey) {
                p.getPlayer().sendMessage(plugin.getUtil().toColor(getLang("_pluginTag", "invalidKey")));
                return false;
            }
            return true;
        }
    }

    /*
     * For bungeecord
     */
    public void addVip(String group, String uuid, String pgroup, long duration, String nick, String expires) {
        dataManager.addRawVip(group, uuid, new ArrayList<>(Arrays.asList(pgroup.split(","))), duration, nick, expires);
    }

    /**
     * Return the key info: <p>
     * [0] = Vip Group | [1] = Duration in millis | [2] = Uses
     *
     * @param key - The key to get info
     * @return {@code String[]} - Arrays with the key info.
     */
    public String[] getKeyInfo(String key) {
        return dataManager.getKeyInfo(key);
    }

    private void enableVip(OfflinePlayer p, String group, long durMillis, String pname, String key) {
        int count = 0;
        long durf = durMillis;
        for (String[] k : getVipInfo(p.getUniqueId().toString())) {
            if (k[1].equals(group)) {
                durMillis += new Long(k[0]);
                count++;
                break;
            }
        }

        if (count == 0) {
            durMillis += plugin.getUtil().getNowMillis();
        }

        List<String> pGroups = plugin.getPerms().getGroupsList(p);
        List<String> pdGroup = pGroups;
        List<String[]> vips = getVipInfo(p.getUniqueId().toString());
        if (!vips.isEmpty()) {
            pGroups = new ArrayList<>(Arrays.asList(vips.get(0)[2].split(",")));
        }


        List<String> normCmds = new ArrayList<>();
        List<String> chanceCmds = new ArrayList<>();

        //run command from vip
        comConfig.configurations.getStringList("groups." + group + ".commands").forEach((cmd) -> {
            plugin.serv.getScheduler().runTaskLater(plugin, () -> {
                String cmdf = cmd.replace("{p}", p.getName())
                        .replace("{vip}", group)
                        .replace("{playergroup}", pdGroup.isEmpty() ? "" : pdGroup.get(0))
                        .replace("{days}", String.valueOf(plugin.getUtil().millisToDay(durf)));
                if (p.isOnline()) {
                    plugin.getUtil().ExecuteCmd(cmdf, null);
                } else {
                    normCmds.add(cmdf);
                }
            }, delay);
            delay++;
        });

        //run command chances from vip
        getCmdChances(group).forEach((chanceString) -> {

            int chance = Integer.parseInt(chanceString);
            double rand = Math.random() * 100;

            //test chance
            if (rand <= chance) {
                comConfig.configurations.getStringList("groups." + group + ".cmdChances." + chanceString).forEach((cmd) -> {
                    plugin.serv.getScheduler().runTaskLater(plugin, () -> {
                        String cmdf = cmd.replace("{p}", p.getName())
                                .replace("{vip}", group)
                                .replace("{playergroup}", pdGroup.isEmpty() ? "" : pdGroup.get(0))
                                .replace("{days}", String.valueOf(plugin.getUtil().millisToDay(durf)));
                        if (p.isOnline()) {
                            plugin.getUtil().ExecuteCmd(cmdf, null);
                        } else {
                            chanceCmds.add(cmdf);
                        }
                    }, delay);
                    delay++;
                });
            }
        });

        if (queueCmds() && (normCmds.size() > 0 || chanceCmds.size() > 0)) {
            plugin.serv.getScheduler().runTaskLater(plugin, () -> {
                plugin.getLogger().info("Queued cmds for player " + p.getName() + " to run on join.");
                setJoinCmds(p.getUniqueId().toString(), normCmds, chanceCmds);
            }, delay);
        }

        delay = 0;

        dataManager.addRawVip(group, p.getUniqueId().toString(),
                pGroups,
                durMillis,
                pname,
                plugin.getUtil().expiresOn(durMillis));

        setActive(p.getUniqueId().toString(), group, pdGroup);

        if (p.isOnline()) {
            p.getPlayer().sendMessage(plugin.getUtil().toColor(getLang("_pluginTag", "vipActivated")));
            p.getPlayer().sendMessage(plugin.getUtil().toColor(getLang("activeVip").replace("{vip}", group)));
            p.getPlayer().sendMessage(plugin.getUtil().toColor(getLang("activeDays").replace("{days}", String.valueOf(plugin.getUtil().millisToDay(durf)))));
            p.getPlayer().sendMessage(plugin.getUtil().toColor("&b---------------------------------------------"));
        }
        plugin.addLog("EnableVip | key: " + key + " | " + p.getName() + " | " + group + " | Expires on: " + plugin.getUtil().expiresOn(durMillis));
    }

    public void setVip(String uuid, String group, long durMillis, String pname) {
        int count = 0;
        for (String[] k : getVipInfo(uuid)) {
            if (k[1].equals(group)) {
                durMillis += new Long(k[0]);
                count++;
                break;
            }
        }

        if (count == 0) {
            durMillis += plugin.getUtil().getNowMillis();
        }

        List<String> pGroups = new ArrayList<>();
        OfflinePlayer p = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
        if (p.getName() != null) {
            pGroups = plugin.getPerms().getGroupsList(p);
        }
        List<String[]> vips = getVipInfo(uuid);
        if (!vips.isEmpty()) {
            pGroups = new ArrayList<>(Collections.singletonList(vips.get(0)[2]));
        }

        dataManager.addRawVip(group, uuid,
                pGroups,
                durMillis,
                pname,
                plugin.getUtil().expiresOn(durMillis));
        setActive(uuid, group, pGroups);

        plugin.addLog("SetVip | " + p.getName() + " | " + group + " | Expires on: " + plugin.getUtil().expiresOn(durMillis));
    }

    public void setActive(String uuid, String group, List<String> pgroup) {
        String newVip = group;
        String oldVip = pgroup.stream().anyMatch(str -> getGroupList(true).contains(str)) ? pgroup.stream().filter(str -> getGroupList(true).contains(str)).findFirst().get() : "";
        for (String glist : getGroupList(true)) {
            if (dataManager.containsVip(uuid, glist)) {
                if (glist.equals(group)) {
                    if (!dataManager.isVipActive(uuid, glist)) {
                        newVip = glist;
                        long total = dataManager.getVipDuration(uuid, glist) + plugin.getUtil().getNowMillis();
                        dataManager.setVipDuration(uuid, glist, total);
                    }
                    dataManager.setVipActive(uuid, glist, true);
                } else {
                    if (dataManager.isVipActive(uuid, glist)) {
                        oldVip = glist;
                        long total = dataManager.getVipDuration(uuid, glist) - plugin.getUtil().getNowMillis();
                        dataManager.setVipDuration(uuid, glist, total);
                    }
                    dataManager.setVipActive(uuid, glist, false);
                }
            }
        }
        //change kits
        changeVipKit(uuid, oldVip, newVip);

        OfflinePlayer p = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
        if (p.getName() != null) {
            runChangeVipCmds(p, newVip, oldVip);
        }
        saveConfigAll();
    }

    public void runChangeVipCmds(OfflinePlayer p, String newVip, String oldVip) {
        for (String cmd : comConfig.configurations.getStringList("configs.commandsToRunOnChangeVip")) {
            if (p.getName() == null) {
                break;
            }

            String cmdf = cmd.replace("{p}", p.getName());
            if (oldVip != null && !oldVip.equals("") && cmdf.contains("{oldvip}")) {
                plugin.serv.getScheduler().runTaskLater(plugin, () -> plugin.getUtil().ExecuteCmd(cmdf.replace("{oldvip}", oldVip), null), delay);
                delay++;
            } else if (!newVip.equals("") && cmdf.contains("{newvip}")) {
                plugin.serv.getScheduler().runTaskLater(plugin, () -> plugin.getUtil().ExecuteCmd(cmdf.replace("{newvip}", newVip), null), delay);
                delay++;
            } else {
                plugin.serv.getScheduler().runTaskLater(plugin, () -> plugin.getUtil().ExecuteCmd(cmdf, null), delay);
                delay++;
            }
        }
        if (comConfig.configurations.getBoolean("configs.Vault.use")) {
            plugin.serv.getScheduler().runTaskLater(plugin, () -> {
                if (oldVip != null && !oldVip.isEmpty() && !oldVip.equals(newVip)) {
                    plugin.getPerms().removeGroup(p.getUniqueId().toString(), oldVip);
                }
                if (comConfig.configurations.getString("configs.Vault.mode").equalsIgnoreCase("set")) {
                    plugin.getPerms().setGroup(p.getUniqueId().toString(), newVip);
                }
                if (comConfig.configurations.getString("configs.Vault.mode").equalsIgnoreCase("add")) {
                    plugin.getPerms().addGroup(p.getUniqueId().toString(), newVip);
                }
            }, delay);
            delay++;
        } else {
            reloadPerms();
        }
    }

    private void changeVipKit(String uuid, String oldVip, String newVip) {
        if (plugin.ess != null) {
            long now = System.currentTimeMillis();
            String oldKit = this.getString("", "groups." + oldVip + ".essentials-kit");
            try {
                User user = plugin.ess.getUser(UUID.fromString(uuid));
                if (!oldKit.isEmpty() && user != null) {
                    long oldTime = user.getKitTimestamp(oldKit.toLowerCase(Locale.ENGLISH));
                    dataManager.setVipKitCooldown(uuid, oldVip, now - oldTime);
                }

                String newKit = this.getString("", "groups." + newVip + ".essentials-kit");
                if (!newKit.isEmpty() && user != null) {
                    long newTime = dataManager.getVipCooldown(uuid, newVip);
                    if (newTime > 0) {
                        user.setKitTimestamp(newKit.toLowerCase(Locale.ENGLISH), now - newTime);
                    }
                }
            } catch (Exception ignored) {
                plugin.getPVLogger().warning("An old version of Essentials plugin was detected! Ignoring kit timer handler.");
            }
        }
    }

    void removeVip(String uuid, String pname, String group) {
        plugin.addLog("RemoveVip | " + pname + " | " + group);

        dataManager.removeVip(uuid, group);
        plugin.serv.getScheduler().runTaskLater(plugin, () -> plugin.getUtil().ExecuteCmd(getString("", "configs.cmdOnRemoveVip").replace("{p}", Optional.ofNullable(pname).get()).replace("{vip}", group), null), delay);
        delay++;

        if (comConfig.configurations.getBoolean("configs.Vault.use")) {
            plugin.serv.getScheduler().runTaskLater(plugin, () -> plugin.getPerms().removeGroup(uuid, group), delay);
            delay++;
        }
    }

    public void removeVip(String uuid, Optional<String> optg) {
        List<String[]> vipInfo = getVipInfo(uuid);
        boolean id = false;
        String nick = "";
        List<String> oldGroup = new ArrayList<>();
        String vipGroup = "";
        if (vipInfo.size() > 0) {
            for (String[] key : vipInfo) {
                vipGroup = key[1];
                oldGroup = Arrays.asList(key[2].split(","));
                nick = key[4];
                if (vipInfo.size() > 1) {
                    if (optg.isPresent()) {
                        if (optg.get().equals(vipGroup)) {
                            removeVip(uuid, nick, vipGroup);
                        } else if (!id) {
                            setActive(uuid, vipGroup, new ArrayList<>());
                            id = true;
                        }
                    } else {
                        removeVip(uuid, nick, vipGroup);
                    }
                } else {
                    removeVip(uuid, nick, vipGroup);
                }
            }
        }

        //commands to run on vip finish
        if (getVipInfo(uuid).size() == 0) {
            for (String cmd : comConfig.configurations.getStringList("configs.commandsToRunOnVipFinish")) {
                if (cmd == null || cmd.isEmpty() || cmd.contains("{vip}")) {
                    continue;
                }
                if (!oldGroup.isEmpty() && cmd.contains("{playergroup}")) {
                    for (String group : oldGroup) {
                        String cmdf = cmd.replace("{p}", nick).replace("{playergroup}", group);
                        plugin.serv.getScheduler().runTaskLater(plugin, () -> plugin.getUtil().ExecuteCmd(cmdf, null), delay);
                        delay++;
                    }
                } else {
                    String cmdf = cmd.replace("{p}", nick);
                    plugin.serv.getScheduler().runTaskLater(plugin, () -> plugin.getUtil().ExecuteCmd(cmdf, null), delay);
                    delay++;
                }
            }
        }

        //command to run from vip GROUP on finish
        for (String cmd : getCmdsToRunOnFinish(vipGroup)) {
            if (cmd == null || cmd.isEmpty()) continue;
            if (!oldGroup.isEmpty() && cmd.contains("{playergroup}")) {
                for (String group : oldGroup) {
                    String cmdf = cmd.replace("{p}", nick).replace("{playergroup}", group);
                    plugin.serv.getScheduler().runTaskLater(plugin, () -> plugin.getUtil().ExecuteCmd(cmdf, null), delay);
                    delay++;
                }
            } else {
                String cmdf = cmd.replace("{p}", nick);
                plugin.serv.getScheduler().runTaskLater(plugin, () -> plugin.getUtil().ExecuteCmd(cmdf, null), delay);
                delay++;
            }
        }

        //use vault to add back oldgroup
        if (comConfig.configurations.getBoolean("configs.Vault.use")) {
            for (String group : oldGroup) {
                plugin.serv.getScheduler().runTaskLater(plugin, () -> plugin.getPerms().addGroup(uuid, group), delay);
                delay++;
            }
        } else {
            reloadPerms();
        }
        saveConfigAll();
    }

    public void reloadPerms() {
        plugin.serv.getScheduler().runTaskLater(plugin, () -> plugin.getUtil().ExecuteCmd(getString("", "configs.cmdToReloadPermPlugin"), null), delay);
        delay = 0;
    }

    public long getLong(int def, String node) {
        return comConfig.configurations.getLong(node, def);
    }

    public int getInt(int def, String node) {
        return comConfig.configurations.getInt(node, def);
    }

    public String getString(String def, String node) {
        return comConfig.configurations.getString(node, def);
    }

    public boolean getBoolean(boolean def, String node) {
        return comConfig.configurations.getBoolean(node, def);
    }

    public String getLang(String... nodes) {
        StringBuilder msg = new StringBuilder();
        for (String node : nodes) {
            msg.append(getString("No strings with " + node, "strings." + node));
        }
        return msg.toString();
    }

    public boolean groupExists(String group) {
        return comConfig.configurations.contains("groups." + group);
    }

    public Set<String> getCmdChances(String vip) {
        if (comConfig.configurations.getConfigurationSection("groups." + vip + ".cmdChances") != null) {
            return comConfig.configurations.getConfigurationSection("groups." + vip + ".cmdChances").getKeys(false);
        }
        return new HashSet<>();
    }

    public Set<String> getCmdsToRunOnFinish(String vip) {
        if (comConfig.configurations.getConfigurationSection("groups." + vip + ".run-on-vip-end") != null) {
            return comConfig.configurations.getConfigurationSection("groups." + vip + ".run-on-vip-end").getKeys(false);
        }
        return new HashSet<>();
    }

    public Set<String> getListKeys() {
        return dataManager.getListKeys();
    }

    public Set<String> getItemListKeys() {
        return dataManager.getItemListKeys();
    }

    public Set<String> getGroupList(boolean raw) {
        Set<String> list = new HashSet<>();
        if (raw) {
            if (comConfig.configurations.getConfigurationSection("groups") != null) {
                return comConfig.configurations.getConfigurationSection("groups").getKeys(false);
            }
        } else {
            if (comConfig.configurations.getConfigurationSection("groups") != null) {
                for (String group : comConfig.configurations.getConfigurationSection("groups").getKeys(false)) {
                    if (comConfig.configurations.getString("groups." + group + ".title") != null &&
                            !comConfig.configurations.getString("groups." + group + ".title").isEmpty())
                        list.add(comConfig.configurations.getString("groups." + group + ".title"));
                    else
                        list.add(group);
                }
            }
        }
        return list;
    }

    public HashMap<String, List<String[]>> getVipList() {
        return dataManager.getActiveVipList();
    }

    /**
     * Return player's vip info.<p>
     * [0] = Duration, [1] = Vip Group, [2] = Player Group, [3] = Is Active, [4] = Player Nick
     *
     * @param puuid Player UUID as string.
     * @return {@code List<String[5]>} or a empty list if theres no vip for player.
     */
    public List<String[]> getVipInfo(String puuid) {
        return dataManager.getVipInfo(puuid);
    }


    /**
     * Return player's vip info.<p>
     * [0] = Duration, [1] = Vip Group, [2] = Player Group, [3] = Is Active, [4] = Player Nick
     *
     * @param playName Player UUID as string or nickname.
     * @return {@code String[5]}
     */
    public String[] getActiveVipInfo(String playName) {
        String uuid;
        try {
            UUID.fromString(playName);
            uuid = playName;
        } catch (IllegalArgumentException ex) {
            uuid = getVipUUID(playName);
        }
        for (String[] vips : getVipInfo(uuid)) {
            if (vips[3].equals("true")) {
                return vips;
            }
        }
        return new String[5];
    }

    /**
     * Return all vip info.<p>
     *
     * @return {@code HashMap<String,List<String[]>>}<p>
     * Key: Existing Group Name.<p>
     * Value: [0] = Duration, [1] = Vip Group, [2] = Player Group, [3] = Is Active, [4] = Player Nick
     */
    public HashMap<String, List<String[]>> getAllVips() {
        return dataManager.getAllVipList();
    }

    public String getVipUUID(String string) {
        return dataManager.getVipUUID(string);
    }
}
