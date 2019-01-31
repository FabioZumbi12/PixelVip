package br.net.fabiozumbi12.pixelvip.sponge.config;

import br.net.fabiozumbi12.pixelvip.sponge.PixelVip;
import br.net.fabiozumbi12.pixelvip.sponge.db.PVDataFile;
import br.net.fabiozumbi12.pixelvip.sponge.db.PVDataManager;
import br.net.fabiozumbi12.pixelvip.sponge.db.PVDataMysql;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.GuiceObjectMapperFactory;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.world.World;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class PVConfig {

    private PVDataManager dataManager;

    private MainCategory root;
    private CommentedConfigurationNode configRoot;
    private ConfigurationLoader<CommentedConfigurationNode> cfgLoader;
    private File defConfig = new File(PixelVip.get().configDir(), "config.conf");

    private int delay = 0;
    private HashMap<String, String> comandAlert = new HashMap<>();

    public MainCategory root() {
        return this.root;
    }

    public PVConfig(GuiceObjectMapperFactory factory) {
        try {
            Files.createDirectories(PixelVip.get().configDir().toPath());
            if (!defConfig.exists()) {
                PixelVip.get().getLogger().info("Creating config file...");
                defConfig.createNewFile();
            }

            /*-------------------------------- config.conf---------------------------------*/
            String header = "# =============== PixelVip Configuration Options ================\n" +
                    "\n" +
                    "This is the default configuration and some information about some configurations.\n" +
                    "\n" +
                    "In \"groups\" on \"commands\" and \"cmdChances\"(Lists) you can use this placeholders:\n" +
                    "- {p} = Players Name\n" +
                    "- {vip} = Vip Group\n" +
                    "- {playergroup} = Player Group before Vip activation\n" +
                    "- {days} = Days of activated Vip\n" +
                    "\n" +
                    "In \"groups\" > \"cmdChances\"(List) you can add commands to run based on a % chance. \n" +
                    "Use numbers below 0-100 like the example on \"vip1\".\n" +
                    "\n" +
                    "In \"configs\" > \"cmdOnRemoveVip\"(String) you can use this placeholders:\n" +
                    "- {p} = Player Name\n" +
                    "- {vip} = Name of Vip Removed\n" +
                    "\n" +
                    "In \"configs\" > \"commandsToRunOnChangeVip\"(List) you can use this placeholders:\n" +
                    "- {p} = Player Name\n" +
                    "- {newvip} = Name of Vip the player is changing to\n" +
                    "- {oldvip} = Name of Vip the player is changing from\n" +
                    "\n" +
                    "In \"configs\" > \"commandsToRunOnVipFinish\" and \"run-on-vip-finish\" (Lists) you can use this placeholders:\n" +
                    "- {p} = Player Name\n" +
                    "- {vip} = Name of Vip\n" +
                    "- {playergroup} = Player Group before Vip activation\n" +
                    "\n";

            cfgLoader = HoconConfigurationLoader.builder().setFile(defConfig).build();
            configRoot = cfgLoader.load(ConfigurationOptions.defaults().setObjectMapperFactory(factory).setShouldCopyDefaults(true).setHeader(header));
            root = configRoot.getValue(TypeToken.of(MainCategory.class), new MainCategory());


            //init database
            reloadVips();

            //save all
            saveConfigAll();

        } catch (IOException | ObjectMappingException e1) {
            e1.printStackTrace();
        }
    }

    public void reloadVips() {
        if (dataManager != null) {
            dataManager.closeCon();
        }
        if (PixelVip.get().getConfig().root.configs.database.type.equalsIgnoreCase("mysql")) {
            dataManager = new PVDataMysql(PixelVip.get());
        } else {
            dataManager = new PVDataFile(PixelVip.get());
        }
    }

    public boolean transExist(String payment, String trans) {
        return dataManager.transactionExist(payment, trans);
    }

    public void addTrans(String payment, String trans, String player) {
        dataManager.addTras(payment, trans, player);
    }

    public void removeTrans(String payment, String trans) {
        dataManager.removeTrans(payment, trans);
    }

    public HashMap<String, Map<String, String>> getAllTrans() {
        return dataManager.getAllTrans();
    }

    public boolean worldAllowed(World w) {
        return root.configs.worldCmdsAllowed.contains(w.getName());
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

    /*public boolean bungeeSyncEnabled() {
        return root.bungee.enableSync;
    }*/

    public boolean queueCmds() {
        return root.configs.queueCmdsForOfflinePlayers;
    }

    /*
     * For BungeeCord
     */
    public void setVipActive(String uuid, String vip, boolean active) {
        dataManager.setVipActive(uuid, vip, active);
    }

    public List<String> getQueueCmds(String uuid) {
        List<String> cmds = new ArrayList<>();
        if (root.joinCmds.containsKey(uuid)) {
            if (!root.joinCmds.get(uuid).cmds.isEmpty())
                cmds.addAll(root.joinCmds.get(uuid).cmds);
            if (!root.joinCmds.get(uuid).chanceCmds.isEmpty())
                cmds.addAll(root.joinCmds.get(uuid).chanceCmds);
        }
        root.joinCmds.remove(uuid);
        saveConfigAll();
        return cmds;
    }

    private void setJoinCmds(String uuid, List<String> cmds, List<String> chanceCmds) {
        root.joinCmds.put(uuid, new MainCategory.JoinCmdsCat(cmds, chanceCmds));
        saveConfigAll();
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

    public void saveConfigAll() {
        saveVips();
        saveKeys();
        try {
            configRoot.setValue(TypeToken.of(MainCategory.class), root);
            cfgLoader.save(configRoot);
        } catch (IOException | ObjectMappingException e) {
            e.printStackTrace();
        }
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

    public CommandResult activateVip(User p, String key, String group, long days, String pname) throws CommandException {

        if (root.configs.useKeyWarning && p.isOnline() && (key != null && !key.isEmpty())) {
            if (!comandAlert.containsKey(p.getName()) || !comandAlert.get(p.getName()).equalsIgnoreCase(key)) {
                comandAlert.put(p.getName(), key);
                p.getPlayer().get().sendMessage(PixelVip.get().getUtil().toText(getLang("_pluginTag", "confirmUsekey")));
                return CommandResult.success();
            }
            comandAlert.remove(p.getName());
        }

        boolean hasItemkey = key != null && dataManager.getItemListKeys().contains(key);
        if (hasItemkey) {
            StringBuilder cmdsBuilder = new StringBuilder();
            List<String> cmds = dataManager.getItemKeyCmds(key);
            for (String cmd : cmds) {
                cmdsBuilder.append(cmd).append(", ");
                Sponge.getGame().getScheduler().createSyncExecutor(PixelVip.get()).schedule(() -> {
                    String cmdf = cmd.replace("{p}", p.getName());
                    if (p.isOnline()) {
                        Sponge.getCommandManager().process(Sponge.getServer().getConsole(), cmdf);
                    }
                }, delay * 100, TimeUnit.MILLISECONDS);
                delay++;
            }
            dataManager.removeItemKey(key);
            saveConfigAll();

            p.getPlayer().get().sendMessage(PixelVip.get().getUtil().toText(getLang("_pluginTag", "itemsGiven").replace("{items}", cmds.size() + "")));

            String cmdBuilded = cmdsBuilder.toString();
            PixelVip.get().addLog("ItemKey | " + p.getName() + " | " + key + " | Cmds: " + cmdBuilded.substring(0, cmdBuilded.length() - 2));
        }
        if (getKeyInfo(key).length == 3) {
            String[] keyinfo = getKeyInfo(key);
            int uses = Integer.parseInt(keyinfo[2]);

            delKey(key, uses);

            p.getPlayer().get().sendMessage(PixelVip.get().getUtil().toText("&b---------------------------------------------"));
            if (uses - 1 > 0) {
                p.getPlayer().get().sendMessage(PixelVip.get().getUtil().toText(getLang("_pluginTag", "usesLeftActivation").replace("{uses}", "" + (uses - 1))));
            }
            enableVip(p, keyinfo[0], new Long(keyinfo[1]), pname);
            return CommandResult.success();
        } else if (!group.equals("")) {
            enableVip(p, group, PixelVip.get().getUtil().dayToMillis(days), pname);
            return CommandResult.success();
        } else {
            if (!hasItemkey) {
                throw new CommandException(PixelVip.get().getUtil().toText(PixelVip.get().getConfig().getLang("_pluginTag", "invalidKey")));
            }
            return CommandResult.success();
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

    private void enableVip(User p, String group, long durMillis, String pname) {
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
            durMillis += PixelVip.get().getUtil().getNowMillis();
        }

        String pGroup = PixelVip.get().getPerms().getGroup(p);
        String pdGroup = pGroup;
        List<String[]> vips = getVipInfo(p.getUniqueId().toString());
        if (!vips.isEmpty()) {
            pGroup = vips.get(0)[2];
        }

        List<String> normCmds = new ArrayList<String>();
        List<String> chanceCmds = new ArrayList<String>();

        //run command from vip
        root.groups.get(group).commands.forEach((cmd) -> {
            Sponge.getGame().getScheduler().createSyncExecutor(PixelVip.get()).schedule(() -> {
                String cmdf = cmd.replace("{p}", p.getName())
                        .replace("{vip}", group)
                        .replace("{playergroup}", pdGroup)
                        .replace("{days}", String.valueOf(PixelVip.get().getUtil().millisToDay(durf)));
                if (p.isOnline()) {
                    Sponge.getGame().getCommandManager().process(Sponge.getServer().getConsole(), cmdf);
                } else {
                    normCmds.add(cmdf);
                }
            }, delay * 100, TimeUnit.MILLISECONDS);
            delay++;
        });

        //run command chances from vip
        getCmdChances(group).forEach((node) -> {
            String chanceString = String.valueOf(node.getKey());
            int chance = Integer.parseInt(chanceString);
            double rand = Math.random() * 100;

            //test chance
            if (rand <= chance) {
                root.groups.get(group).cmdChances.get(chanceString).forEach((cmd) -> {
                    Sponge.getScheduler().createSyncExecutor(PixelVip.get()).schedule(() -> {
                        String cmdf = cmd.replace("{p}", p.getName())
                                .replace("{vip}", group)
                                .replace("{playergroup}", pdGroup)
                                .replace("{days}", String.valueOf(PixelVip.get().getUtil().millisToDay(durf)));
                        if (p.isOnline()) {
                            Sponge.getCommandManager().process(Sponge.getServer().getConsole(), cmdf);
                        } else {
                            chanceCmds.add(cmdf);
                        }
                    }, delay * 100, TimeUnit.MILLISECONDS);
                    delay++;
                });
            }
        });

        if (queueCmds() && (normCmds.size() > 0 || chanceCmds.size() > 0)) {
            Sponge.getScheduler().createSyncExecutor(PixelVip.get()).schedule(() -> {
                PixelVip.get().getLogger().info("Queued cmds for player " + p.getName() + " to run on join.");
                setJoinCmds(p.getUniqueId().toString(), normCmds, chanceCmds);
            }, delay * 100, TimeUnit.MILLISECONDS);
        }

        delay = 0;

        dataManager.addRawVip(group, p.getUniqueId().toString(),
                Collections.singletonList(pGroup),
                durMillis,
                pname,
                PixelVip.get().getUtil().expiresOn(durMillis));

        setActive(p.getUniqueId().toString(), group, pdGroup);

        if (p.isOnline()) {
            p.getPlayer().get().sendMessage(PixelVip.get().getUtil().toText(PixelVip.get().getConfig().getLang("_pluginTag", "vipActivated")));
            p.getPlayer().get().sendMessage(PixelVip.get().getUtil().toText(PixelVip.get().getConfig().getLang("activeVip").replace("{vip}", group)));
            p.getPlayer().get().sendMessage(PixelVip.get().getUtil().toText(PixelVip.get().getConfig().getLang("activeDays").replace("{days}", String.valueOf(PixelVip.get().getUtil().millisToDay(durf)))));
            p.getPlayer().get().sendMessage(PixelVip.get().getUtil().toText("&b---------------------------------------------"));
        }
        PixelVip.get().addLog("EnableVip | " + p.getName() + " | " + group + " | Expires on: " + PixelVip.get().getUtil().expiresOn(durMillis));
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
            durMillis += PixelVip.get().getUtil().getNowMillis();
        }

        String pGroup = "";
        Optional<Player> optPlayer = Sponge.getServer().getPlayer(uuid);
        if (optPlayer.isPresent())
            pGroup = PixelVip.get().getPerms().getGroup(optPlayer.get());

        List<String[]> vips = PixelVip.get().getConfig().getVipInfo(uuid);
        if (!vips.isEmpty()) {
            pGroup = vips.get(0)[2];
        }
        dataManager.addRawVip(group, uuid,
                Collections.singletonList(pGroup),
                durMillis,
                pname,
                PixelVip.get().getUtil().expiresOn(durMillis));
        setActive(uuid, group, pGroup);

        PixelVip.get().addLog("SetVip | " + optPlayer.get().getName() + " | " + group + " | Expires on: " + PixelVip.get().getUtil().expiresOn(durMillis));
    }

    public void setActive(String uuid, String group, String pgroup) {
        String newVip = group;
        String oldVip = pgroup;

        for (String glist : getGroupList()) {
            if (dataManager.containsVip(uuid, glist)){
                if (glist.equals(group)) {
                    if (!dataManager.isVipActive(uuid, glist)) {
                        newVip = glist;
                        long total = dataManager.getVipDuration(uuid, glist) + PixelVip.get().getUtil().getNowMillis();
                        dataManager.setVipDuration(uuid, glist, total);
                    }
                    dataManager.setVipActive(uuid, glist, true);
                } else {
                    if (dataManager.isVipActive(uuid, glist)) {
                        oldVip = glist;
                        long total = dataManager.getVipDuration(uuid, glist) - PixelVip.get().getUtil().getNowMillis();
                        dataManager.setVipDuration(uuid, glist, total);
                    }
                    dataManager.setVipActive(uuid, glist, false);
                }
            }
        }
        runChangeVipCmds(uuid, newVip, oldVip);
        saveConfigAll();
    }

    public void runChangeVipCmds(String puuid, String newVip, String oldVip) {
        for (String cmd : root.configs.commandsToRunOnChangeVip) {

            String cmdf = cmd.replace("{p}", PixelVip.get().getUtil().getUser(UUID.fromString(puuid)).get().getName());
            if (!oldVip.equals("") && cmdf.contains("{oldvip}")) {
                Sponge.getGame().getScheduler().createTaskBuilder().delay(delay * 100, TimeUnit.MILLISECONDS).execute(t -> {
                    Sponge.getGame().getCommandManager().process(Sponge.getServer().getConsole(), cmdf.replace("{oldvip}", oldVip));
                }).submit(PixelVip.get());
                delay++;
            } else if (!newVip.equals("") && cmdf.contains("{newvip}")) {
                Sponge.getGame().getScheduler().createTaskBuilder().delay(delay * 100, TimeUnit.MILLISECONDS).execute(t -> {
                    Sponge.getGame().getCommandManager().process(Sponge.getServer().getConsole(), cmdf.replace("{newvip}", newVip));
                }).submit(PixelVip.get());
                delay++;
            } else {
                Sponge.getGame().getScheduler().createTaskBuilder().delay(delay * 100, TimeUnit.MILLISECONDS).execute(t -> {
                    Sponge.getGame().getCommandManager().process(Sponge.getServer().getConsole(), cmdf);
                }).submit(PixelVip.get());
                delay++;
            }
        }
        reloadPerms();
    }

    private void removeVip(String uuid, String pname, String group) {
        PixelVip.get().addLog("RemoveVip | " + pname + " | " + group);

        dataManager.removeVip(uuid, group);
        Sponge.getGame().getScheduler().createTaskBuilder().delay(delay * 100, TimeUnit.MILLISECONDS).execute(t -> {
            Sponge.getCommandManager().process(Sponge.getServer().getConsole(), root.configs.cmdOnRemoveVip.replace("{p}", pname).replace("{vip}", group));
        }).submit(PixelVip.get());
        delay++;
    }

    public void removeVip(User p, Optional<String> optg) {
        String uuid = p.getUniqueId().toString();
        List<String[]> vipInfo = PixelVip.get().getConfig().getVipInfo(uuid);
        boolean id = false;
        String nick = "";
        String oldGroup = "";
        String vipGroup = "";
        if (vipInfo.size() > 0) {
            for (String[] key : vipInfo) {
                vipGroup = key[1];
                oldGroup = key[2];
                nick = key[4];
                if (vipInfo.size() > 1) {
                    if (optg.isPresent()) {
                        if (optg.get().equals(vipGroup)) {
                            removeVip(uuid, nick, vipGroup);
                        } else if (!id) {
                            PixelVip.get().getConfig().setActive(uuid, vipGroup, "");
                            id = true;
                        }
                    } else {
                        PixelVip.get().getConfig().removeVip(uuid, nick, vipGroup);
                    }
                } else {
                    PixelVip.get().getConfig().removeVip(uuid, nick, vipGroup);
                }
            }
        }

        //commands to run on vip finish
        if (getVipInfo(uuid).size() == 0) {
            for (String cmd : root.configs.commandsToRunOnVipFinish) {
                if (cmd == null || cmd.isEmpty() || cmd.contains("{vip}")) {
                    continue;
                }
                String cmdf = cmd.replace("{p}", p.getName()).replace("{playergroup}", oldGroup);
                Sponge.getGame().getScheduler().createTaskBuilder().delay(delay * 100, TimeUnit.MILLISECONDS).execute(t -> {
                    Sponge.getGame().getCommandManager().process(Sponge.getServer().getConsole(), cmdf);
                }).submit(PixelVip.get());
                delay++;
            }
        }

        //command to run from vip GROUP on finish
        for (String cmd : getCmdsToRunOnFinish(vipGroup)) {
            String cmdf = cmd.replace("{p}", p.getName());
            Sponge.getGame().getScheduler().createTaskBuilder().delay(delay * 100, TimeUnit.MILLISECONDS).execute(t -> {
                Sponge.getGame().getCommandManager().process(Sponge.getServer().getConsole(), cmdf);
            }).submit(PixelVip.get());
            delay++;
        }
        reloadPerms();
        saveConfigAll();
    }

    public void reloadPerms() {
        Sponge.getGame().getScheduler().createTaskBuilder().delay(1 + delay * 100, TimeUnit.MILLISECONDS).execute(t -> {
            Sponge.getGame().getCommandManager().process(Sponge.getServer().getConsole(), root.configs.cmdToReloadPermPlugin);
        }).submit(PixelVip.get());
        delay = 0;
    }

    public String getLang(String... nodes) {
        StringBuilder msg = new StringBuilder();
        for (String node : nodes) {
            if (root.strings.containsKey(node)){
                msg.append(root.strings.get(node));
            } else {
                msg.append("No strings found for node &6").append(node);
            }
        }
        return msg.toString();
    }

    public boolean groupExists(String group) {
        return root.groups.containsKey(group);
    }

    public Set<Map.Entry<String,List<String>>> getCmdChances(String vip) {
        return root.groups.get(vip).cmdChances.entrySet();
    }

    public List<String> getCmdsToRunOnFinish(String vip) {
        if (root.groups.containsKey(vip)) {
            return root.groups.get(vip).run_on_vip_end;
        }
        return new ArrayList<>();
    }

    public Set<String> getListKeys() {
        return dataManager.getListKeys();
    }

    public Set<String> getItemListKeys() {
        return dataManager.getItemListKeys();
    }

    public Set<String> getGroupList() {
        return root.groups.keySet();
    }

    public HashMap<String, List<String[]>> getVipList() {
        return dataManager.getActiveVipList();
    }

    /**
     * Return player's vip info.<p>
     * [0] = Duration, [1] = Vip Group, [2] = Player Group, [3] = Is Active, [4] = Player Nick
     *
     * @param puuid Player UUID as string.
     * @return {@code List<String[5]>}
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

    public String getVipUUID(String string) {
        return dataManager.getVipUUID(string);
    }

    public HashMap<String, String> getCmdChoices() {
        HashMap<String, String> choices = new HashMap<String, String>();
        getGroupList().forEach((k) -> choices.put(k, k));
        return choices;
    }
}
