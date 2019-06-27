package br.net.fabiozumbi12.pixelvip.bukkit.cmds;

import br.net.fabiozumbi12.pixelvip.bukkit.Packages.PVPackage;
import br.net.fabiozumbi12.pixelvip.bukkit.Packages.PackageManager;
import br.net.fabiozumbi12.pixelvip.bukkit.PaymentsAPI.PaymentModel;
import br.net.fabiozumbi12.pixelvip.bukkit.PixelVip;
import br.net.fabiozumbi12.pixelvip.bukkit.bungee.SpigotText;
import br.net.fabiozumbi12.pixelvip.bukkit.db.PVDataFile;
import br.net.fabiozumbi12.pixelvip.bukkit.db.PVDataManager;
import br.net.fabiozumbi12.pixelvip.bukkit.db.PVDataMysql;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PVCommands implements CommandExecutor, TabCompleter, Listener {

    private PixelVip plugin;
    private List<String> cmdWait;

    public PVCommands(PixelVip plugin) {
        this.plugin = plugin;
        this.cmdWait = new ArrayList<>();
        plugin.serv.getPluginManager().registerEvents(this, plugin);

        plugin.getCommand("delkey").setExecutor(this);
        plugin.getCommand("newkey").setExecutor(this);
        plugin.getCommand("sendkey").setExecutor(this);
        plugin.getCommand("newitemkey").setExecutor(this);
        plugin.getCommand("additemkey").setExecutor(this);
        plugin.getCommand("listkeys").setExecutor(this);
        plugin.getCommand("usekey").setExecutor(this);
        plugin.getCommand("viptime").setExecutor(this);
        plugin.getCommand("removevip").setExecutor(this);
        plugin.getCommand("setactive").setExecutor(this);
        plugin.getCommand("addvip").setExecutor(this);
        plugin.getCommand("setvip").setExecutor(this);
        plugin.getCommand("pixelvip").setExecutor(this);
        plugin.getCommand("listvips").setExecutor(this);
        plugin.getCommand("givepackage").setExecutor(this);
        plugin.getCommand("getvariant").setExecutor(this);
        plugin.getCommand("listpackages").setExecutor(this);
        plugin.getCommand("addpackage").setExecutor(this);
        plugin.getCommand("delpackage").setExecutor(this);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {

        if (args.length == 1) {
            if (cmd.getName().equalsIgnoreCase("newkey")) {
                List<String> list = new ArrayList<>(plugin.getPVConfig().getGroupList(false));
                list.replaceAll(g -> plugin.getUtil().removeColor(g));
                return list;
            }
            if (sender instanceof Player && cmd.getName().equalsIgnoreCase("setactive")) {
                Player p = (Player) sender;
                List<String> list = new ArrayList<>();
                for (String[] vip : plugin.getPVConfig().getVipInfo(p.getUniqueId().toString())) {
                    list.add(ChatColor.stripColor(plugin.getPVConfig().getVipTitle(vip[1])));
                }
                return list;
            }
            if (cmd.getName().equalsIgnoreCase("delpackage")) {
                return new ArrayList<>(plugin.getPackageManager().getPackages().getConfigurationSection("packages").getKeys(false));
            }
        }
        if (args.length == 2) {
            if (cmd.getName().equalsIgnoreCase("setvip") ||
                    cmd.getName().equalsIgnoreCase("addvip") ||
                    cmd.getName().equalsIgnoreCase("removevip")) {
                List<String> list = new ArrayList<>(plugin.getPVConfig().getGroupList(false));
                list.replaceAll(g -> plugin.getUtil().removeColor(g));
                return list;
            }
            if (cmd.getName().equalsIgnoreCase("givepackage")) {
                return new ArrayList<>(plugin.getPackageManager().getPackages().getConfigurationSection("packages").getKeys(false));
            }
            if (cmd.getName().equalsIgnoreCase("addpackage")) {
                return Arrays.asList("hand", "command");
            }
        }

        if (cmd.getName().equalsIgnoreCase("pixelvip") && args.length == 1) {
            return Arrays.asList("reload", "mysqlToFile", "fileToMysql");
        }
        return null;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player sender = e.getPlayer();
        if (plugin.getPackageManager().hasPendingPlayer(sender)) {
            sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "pendent")));
            for (String pend : plugin.getPackageManager().getPendingVariant(sender)) {
                givePackage(sender, new String[]{sender.getName(), pend}, false);
            }
            e.setCancelled(true);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player && !plugin.getPVConfig().worldAllowed(((Player) sender).getWorld())) {
            sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "cmdNotAllowedWorld")));
            return true;
        }

        if (sender instanceof Player) {
            if (plugin.getPackageManager().hasPendingPlayer((Player) sender) && !cmd.getName().equalsIgnoreCase("getvariant")) {
                sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "pendent")));
                for (String pend : plugin.getPackageManager().getPendingVariant((Player) sender)) {
                    givePackage(sender, new String[]{sender.getName(), pend}, false);
                }
                return true;
            }

            if (cmdWait.contains(sender.getName())) {
                sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "wait-cmd")));
                return true;
            } else {
                cmdWait.add(sender.getName());
                Bukkit.getScheduler().runTaskLater(plugin, () -> cmdWait.remove(sender.getName()), 20);
            }
        }


        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean success = true;
            if (cmd.getName().equalsIgnoreCase("delkey")) {
                success = delKey(sender, args);
            }

            if (cmd.getName().equalsIgnoreCase("newkey")) {
                success = newKey(sender, args, false);
            }

            if (cmd.getName().equalsIgnoreCase("sendkey")) {
                success = sendKey(args);
            }

            if (cmd.getName().equalsIgnoreCase("newitemkey")) {
                success = newItemKey(sender, args);
            }

            if (cmd.getName().equalsIgnoreCase("additemkey")) {
                success = addItemKey(sender, args);
            }

            if (cmd.getName().equalsIgnoreCase("listkeys")) {
                success = listKeys(sender);
            }

            if (cmd.getName().equalsIgnoreCase("usekey")) {
                success = useKey(sender, args);
            }

            if (cmd.getName().equalsIgnoreCase("viptime")) {
                success = vipTime(sender, args);
            }

            if (cmd.getName().equalsIgnoreCase("removevip")) {
                success = removeVip(sender, args);
            }

            if (cmd.getName().equalsIgnoreCase("setactive")) {
                success = setActive(sender, args);
            }

            if (cmd.getName().equalsIgnoreCase("addvip")) {
                success = addVip(sender, args);
            }

            if (cmd.getName().equalsIgnoreCase("setvip")) {
                success = setVip(sender, args);
            }

            if (cmd.getName().equalsIgnoreCase("pixelvip")) {
                success = mainCommand(sender, args);
            }

            if (cmd.getName().equalsIgnoreCase("listvips")) {
                success = listVips(sender);
            }

            if (cmd.getName().equalsIgnoreCase("givepackage")) {
                success = givePackage(sender, args, true);
            }

            if (cmd.getName().equalsIgnoreCase("getvariant")) {
                success = getVariant(sender, args);
            }

            if (cmd.getName().equalsIgnoreCase("listpackages")) {
                success = listPackages(sender);
            }

            if (cmd.getName().equalsIgnoreCase("addpackage")) {
                success = addPackage(sender, args);
            }

            if (cmd.getName().equalsIgnoreCase("delpackage")) {
                success = delPackage(sender, args);
            }
            if (!success) {
                sender.sendMessage(cmd.getDescription());
                sender.sendMessage(cmd.getUsage());
            }
        });
        return true;
    }

    private boolean delPackage(CommandSender sender, String[] args) {
        if (args.length == 1) {
            PackageManager packages = plugin.getPackageManager();
            String id = args[0];
            if (packages.getPackage(id) == null) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        plugin.getPVConfig().getLang("_pluginTag") + packages.getPackages().getString("strings.no-package").replace("{id}", id)));
                return true;
            }
            packages.getPackages().set("packages." + id, null);
            packages.save();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getPVConfig().getLang("_pluginTag") + packages.getPackages().getString("strings.removed")));
            return true;
        }
        return false;
    }

    private boolean addPackage(CommandSender sender, String[] args) {
        PackageManager packages = plugin.getPackageManager();
        //itemstack: /addpackage <id> hand|command [command1, command2]
        if (args.length >= 2) {
            String id = args[0];
            if (packages.getPackage(id) != null) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        plugin.getPVConfig().getLang("_pluginTag") + packages.getPackages().getString("strings.exists").replace("{id}", id)));
                return true;
            }

            if (args.length == 2 && args[1].equalsIgnoreCase("hand")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            plugin.getPVConfig().getLang("_pluginTag") + packages.getPackages().getString("strings.only-players")));
                    return true;
                }
                Player p = (Player) sender;
                if (p.getItemInHand().getType().equals(Material.AIR)) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            plugin.getPVConfig().getLang("_pluginTag") + packages.getPackages().getString("strings.hand-empty")));
                    return true;
                }
                String item = p.getItemInHand().getType().name();
                int amount = p.getItemInHand().getAmount();
                String cmd = packages.getPackages().getString("hand.cmd", "give {p} {item} {amount}")
                        .replace("{item}", item)
                        .replace("{amount}", amount + "");

                packages.getPackages().set("packages." + id + ".commands", Collections.singletonList(cmd));
                packages.save();
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        plugin.getPVConfig().getLang("_pluginTag") + packages.getPackages().getString("strings.added")));
                return true;
            }
            if (args[1].equalsIgnoreCase("command")) {
                args[0] = "";
                args[1] = "";
                List<String> cmds = fixArgs(args);
                packages.getPackages().set("packages." + id + ".commands", cmds);
                packages.save();
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        plugin.getPVConfig().getLang("_pluginTag") + packages.getPackages().getString("strings.added")));
                return true;
            }
        }
        return false;
    }

    private boolean listPackages(CommandSender sender) {
        sender.sendMessage(ChatColor.AQUA + "PixelVip Packages:");
        for (String pkg : plugin.getPackageManager().getPackages().getConfigurationSection("packages").getKeys(false)) {
            sender.sendMessage(ChatColor.GREEN + "ID: " + pkg + " - Variants: " + (plugin.getPackageManager().getPackage(pkg).getVariants() != null));
        }
        return true;
    }

    private boolean getVariant(CommandSender sender, String[] args) {
        if (sender instanceof Player && args.length == 2) {
            Player p = (Player) sender;
            if (plugin.getPackageManager().hasPendingPlayer(p)) {
                String id = args[0];
                for (String idv : plugin.getPackageManager().getPendingVariant(p)) {
                    if (idv.equalsIgnoreCase(id)) {
                        PVPackage pkg = plugin.getPackageManager().getPackage(idv);
                        if (pkg.hasVariant(args[1])) {
                            pkg.giveVariant(p, args[1]);
                        } else {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                    plugin.getPVConfig().getLang("_pluginTag") + plugin.getPackageManager().getPackages().getString("strings.no-pendent")));
                        }
                        break;
                    }
                }
                return true;
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        plugin.getPVConfig().getLang("_pluginTag") + plugin.getPackageManager().getPackages().getString("strings.no-pendent")));
                return true;
            }
        }
        return false;
    }

    private boolean givePackage(CommandSender sender, String[] args, boolean add) {
        if (args.length == 2) {
            if (plugin.serv.getPlayer(args[0]) != null) {
                YamlConfiguration packages = plugin.getPackageManager().getPackages();
                Player p = plugin.serv.getPlayer(args[0]);
                if (plugin.getPackageManager().getPackage(args[1]) != null) {
                    PVPackage pkg = plugin.getPackageManager().getPackage(args[1]);
                    pkg.runCommands(p);
                    if (pkg.getVariants() != null) {

                        //add for usage
                        if (add) {
                            List<String> pending = packages.getStringList("pending-variants." + p.getName());
                            pending.add(pkg.getID());
                            packages.set("pending-variants." + p.getName(), pending);
                            try {
                                packages.save(new File(plugin.getDataFolder(), "packages.yml"));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        if (plugin.getPVConfig().getRoot().getBoolean("configs.spigot.clickKeySuggest")) {
                            SpigotText text = new SpigotText(ChatColor.translateAlternateColorCodes('&',
                                    plugin.getPVConfig().getLang("_pluginTag") + packages.getString("packages." + pkg.getID() + ".variants.message")), null, null, null);
                            String start = "";
                            for (String var : pkg.getVariants().keySet()) {
                                text.getText().addExtra(new SpigotText(
                                        ChatColor.translateAlternateColorCodes('&', start + "&e" + var),
                                        null,
                                        String.format("/getvariant %s %s", pkg.getID(), var),
                                        ChatColor.translateAlternateColorCodes('&', packages.getString("strings.hover-info"))).getText());
                                if (start.equals("")) start = ", ";
                            }
                            text.sendMessage(p);
                        } else {
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                    plugin.getPVConfig().getLang("_pluginTag") + String.format(packages.getString("strings.use-cmd"), pkg.getID())));

                            StringBuilder vars = new StringBuilder();
                            for (String var : pkg.getVariants().keySet()) {
                                vars.append(", ").append(var);
                            }
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                    packages.getString("strings.variants") + "&e" + vars.toString().substring(2)));
                        }
                    }
                    return true;
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            plugin.getPVConfig().getLang("_pluginTag") + packages.getString("strings.no-package").replace("{id}", args[1])));
                }
            }
        }
        return false;
    }

    private boolean listVips(CommandSender sender) {
        HashMap<String, List<String[]>> vips = plugin.getPVConfig().getVipList();
        sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "list-of-vips")));
        sender.sendMessage(plugin.getUtil().toColor("&b---------------------------------------------"));
        vips.forEach((uuid, vipinfolist) -> vipinfolist.forEach((vipinfo) -> {
            String pname = plugin.getServer().getOfflinePlayer(UUID.fromString(uuid)).getName();
            if (pname == null) {
                pname = vipinfo[4];
            }
            sender.sendMessage(plugin.getUtil().toColor("&7> Player &3" + pname + "&7:"));
            sender.sendMessage(plugin.getUtil().toColor("  " + plugin.getPVConfig().getLang("timeGroup") + plugin.getPVConfig().getVipTitle(vipinfo[1])));
            sender.sendMessage(plugin.getUtil().toColor("  " + plugin.getPVConfig().getLang("timeLeft") + plugin.getUtil().millisToMessage(Long.parseLong(vipinfo[0]) - plugin.getUtil().getNowMillis())));
        }));
        sender.sendMessage(plugin.getUtil().toColor("&b---------------------------------------------"));
        return true;
    }

    private boolean mainCommand(CommandSender sender, String[] args) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                plugin.reloadCmd(sender);
                return true;
            }
        }
        if (sender instanceof ConsoleCommandSender) {
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("fileToMysql")) {
                    if (plugin.getPVConfig().getRoot().getString("configs.database.type").equalsIgnoreCase("mysql")) {
                        sender.sendMessage(plugin.getUtil().toColor("&cYour database type is already Mysql. Use &4/pixelvip mysqlToFile &cif you want to convert to file."));
                        return true;
                    }
                    plugin.getPVConfig().getRoot().set("configs.database.type", "mysql");
                    convertDB(new PVDataMysql(plugin));
                    return true;
                }
                if (args[0].equalsIgnoreCase("mysqlToFile")) {
                    if (plugin.getPVConfig().getRoot().getString("configs.database.type").equalsIgnoreCase("file")) {
                        sender.sendMessage(plugin.getUtil().toColor("&cYour database type is already File. Use &4/pixelvip fileToMysql &cif you want to convert to mysql."));
                        return true;
                    }
                    plugin.getPVConfig().getRoot().set("configs.database.type", "file");
                    convertDB(new PVDataFile(plugin));
                    return true;
                }
            }
        }
        return false;
    }

    private void convertDB(PVDataManager dm) {
        plugin.getPVConfig().getAllVips().forEach((uuid, vipInfo) -> {
            vipInfo.forEach(vips -> {
                dm.addRawVip(vips[1], uuid, Arrays.asList(vips[2].split("'")), Long.valueOf(vips[0]), vips[4], plugin.getUtil().expiresOn(Long.valueOf(vips[0])), Boolean.parseBoolean(vips[3]));
            });
        });
        dm.saveVips();

        plugin.getPVConfig().getListKeys().forEach(key -> {
            String[] keyInfo = plugin.getPVConfig().getKeyInfo(key);
            dm.addRawKey(key, keyInfo[0], Long.parseLong(keyInfo[1]), Integer.parseInt(keyInfo[2]));
        });

        plugin.getPVConfig().getItemListKeys().forEach(key -> {
            dm.addRawItemKey(key, plugin.getPVConfig().getItemKeyCmds(key));
        });
        dm.saveKeys();

        plugin.getPVConfig().getAllTrans().forEach((payment, trans) -> {
            for (Map.Entry<String, String> tr : trans.entrySet()) {
                dm.addTras(payment, tr.getKey(), tr.getValue());
            }
        });

        dm.closeCon();
        plugin.saveConfig();
        plugin.reloadCmd(Bukkit.getConsoleSender());
    }

    private List<String> fixArgs(String[] args) {
        StringBuilder cmds = new StringBuilder();
        for (String arg : args) {
            cmds.append(arg).append(" ");
        }
        String[] cmdsSplit = cmds.toString().split(",");
        List<String> cmdList = new ArrayList<String>();
        for (String cmd : cmdsSplit) {
            cmd = cmd.replace("  ", " ");
            if (cmd.length() <= 0) {
                continue;
            }
            if (cmd.startsWith(" ")) {
                cmd = cmd.substring(1);
            }
            if (cmd.endsWith(" ")) {
                cmd = cmd.substring(0, cmd.length() - 1);
            }
            cmdList.add(cmd);
        }
        return cmdList;
    }

    /**
     * Command to generate new item key.
     *
     * @return boolean
     */
    private boolean addItemKey(CommandSender sender, String[] args) {
        if (args.length >= 2) {
            String key = args[0].toUpperCase();
            args[0] = "";

            List<String> cmds = fixArgs(args);
            plugin.getPVConfig().addItemKey(key, cmds);

            sender.sendMessage(plugin.getUtil().toColor("&b---------------------------------------------"));
            sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "itemsAdded")));
            plugin.getUtil().sendHoverKey(sender, key);
            for (String cmd : cmds) {
                sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("item")) + cmd);
            }
            sender.sendMessage(plugin.getUtil().toColor("&b---------------------------------------------"));
            return true;
        }

        return false;
    }

    /**
     * Command to generate new item key.
     *
     * @return boolean
     */
    private boolean newItemKey(CommandSender sender, String[] args) {
        if (args.length >= 1) {
            List<String> cmds = fixArgs(args);
            String key = plugin.getUtil().genKey(plugin.getPVConfig().getInt(10, "configs.key-size"));
            plugin.getPVConfig().addItemKey(key, cmds);

            sender.sendMessage(plugin.getUtil().toColor("&b---------------------------------------------"));
            sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "keyGenerated")));
            plugin.getUtil().sendHoverKey(sender, key);
            for (String cmd : cmds) {
                sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("item")) + cmd);
            }
            sender.sendMessage(plugin.getUtil().toColor("&b---------------------------------------------"));
            return true;
        }

        return false;
    }

    private boolean delKey(CommandSender sender, String[] args) {
        if (args.length == 1) {
            if (plugin.getPVConfig().delKey(args[0], 1) || plugin.getPVConfig().delItemKey(args[0])) {
                sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "keyRemoved") + args[0]));
            } else {
                sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "noKeyRemoved")));
            }
            return true;
        }

        return false;
    }

    /**
     * Command to generate new key.
     *
     * @return boolean
     */
    private boolean newKey(CommandSender sender, String[] args, boolean isSend) {
        if (args.length == 2) {
            String group = plugin.getPVConfig().getVipByTitle(args[0]);
            long days;

            try {
                days = Long.parseLong(args[1]);
            } catch (NumberFormatException ex) {
                return false;
            }

            if (days <= 0) {
                sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "moreThanZero")));
                return true;
            }

            if (!plugin.getPVConfig().groupExists(group)) {
                sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "noGroups") + group));
                return true;
            }
            String key = plugin.getUtil().genKey(plugin.getPVConfig().getInt(10, "configs.key-size"));
            plugin.getPVConfig().addKey(key, group, plugin.getUtil().dayToMillis(days), 1);
            if (isSend) {
                sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "keySendTo")));
            } else {
                sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "keyGenerated")));
            }
            plugin.getUtil().sendHoverKey(sender, key);
            sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("timeGroup") + plugin.getPVConfig().getVipTitle(group)));
            sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("totalTime") + days));
            return true;
        }

        if (args.length == 3) {
            String group = plugin.getPVConfig().getVipByTitle(args[0]);
            long days;
            int uses;

            try {
                days = Long.parseLong(args[1]);
                uses = Integer.parseInt(args[2]);
            } catch (NumberFormatException ex) {
                return false;
            }

            if (days <= 0) {
                sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "moreThanZero")));
                return true;
            }

            if (uses <= 0) {
                sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "moreThanZero")));
                return true;
            }

            if (!plugin.getPVConfig().groupExists(group)) {
                sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "noGroups") + group));
                return true;
            }
            String key = plugin.getUtil().genKey(plugin.getPVConfig().getInt(10, "configs.key-size"));
            plugin.getPVConfig().addKey(key, group, plugin.getUtil().dayToMillis(days), uses);
            if (isSend) {
                sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "keySendTo")));
            } else {
                sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "keyGenerated")));
            }
            plugin.getUtil().sendHoverKey(sender, key);
            sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("timeGroup") + plugin.getPVConfig().getVipTitle(group)));
            sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("totalTime") + days));
            sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("infoUses") + uses));
            return true;
        }
        return false;
    }

    /**
     * Command to send a new key to a player.
     *
     * @return boolean
     */
    private boolean sendKey(String[] args) {
        if (args.length == 2) {
            if (plugin.serv.getPlayer(args[0]) == null) {
                return false;
            }
            if (!plugin.getPVConfig().getListKeys().contains(args[1])) {
                return false;
            }
            Player play = plugin.serv.getPlayer(args[0]);
            String[] keyInfo = plugin.getPVConfig().getKeyInfo(args[1]);

            play.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "keySendTo")));
            plugin.getUtil().sendHoverKey(play, args[1]);
            play.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("timeGroup") + plugin.getPVConfig().getVipTitle(keyInfo[0])));
            play.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("totalTime") + plugin.getUtil().millisToDay(keyInfo[1])));
            play.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("infoUses") + keyInfo[2]));
            return true;
        }

        if (args.length == 3 || args.length == 4) {
            String[] nargs = new String[args.length - 1];
            String splay = args[0];
            if (plugin.serv.getPlayer(splay) == null) {
                return false;
            }
            Player play = plugin.serv.getPlayer(splay);
            for (int i = 0; i < args.length; i++) {
                if (i + 1 == args.length) {
                    break;
                }
                nargs[i] = args[i + 1];
            }
            return newKey(play, nargs, true);
        }
        return false;
    }

    /**
     * Command to list all available keys, and key's info.
     *
     * @return CommandSpec
     */
    public boolean listKeys(CommandSender sender) {
        Collection<String> keys = plugin.getPVConfig().getListKeys();
        Collection<String> itemKeys = plugin.getPVConfig().getItemListKeys();
        int i = 0;
        if (keys.size() > 0) {
            sender.sendMessage(plugin.getUtil().toColor("&b---------------------------------------------"));
            sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "listKeys")));
            for (Object key : keys) {
                String[] keyinfo = plugin.getPVConfig().getKeyInfo(key.toString());
                long days = plugin.getUtil().millisToDay(keyinfo[1]);
                sender.sendMessage(plugin.getUtil().toColor("&b- Key: &6" + key.toString() + "&b | Group: &6" + keyinfo[0] + "&b | Days: &6" + days + "&b | Uses left: &6" + keyinfo[2]));
                i++;
            }
        }

        if (itemKeys.size() > 0) {
            sender.sendMessage(plugin.getUtil().toColor("&b---------------------------------------------"));
            sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "listItemKeys")));
            for (Object key : itemKeys) {
                List<String> cmds = plugin.getPVConfig().getItemKeyCmds(key.toString());
                plugin.getUtil().sendHoverKey(sender, key.toString());
                for (String cmd : cmds) {
                    sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("item")) + cmd);
                }
                i++;
            }
        }
        if (i == 0) {
            sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "noKeys")));
        } else {
            sender.sendMessage(plugin.getUtil().toColor("&b---------------------------------------------"));
        }
        return true;
    }

    /**
     * Command to activate a vip using a key.
     *
     * @return CommandSpec
     */
    public boolean useKey(CommandSender sender, String[] args) {
        if (args.length == 1) {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                String key = args[0].toUpperCase();

                // Command alert
                if (plugin.getPVConfig().getRoot().getBoolean("configs.useKeyWarning") && p.isOnline() && !key.isEmpty()) {
                    if (!plugin.getPVConfig().comandAlert.containsKey(p.getName()) || !plugin.getPVConfig().comandAlert.get(p.getName()).equalsIgnoreCase(key)) {
                        plugin.getPVConfig().comandAlert.put(p.getName(), key);
                        p.getPlayer().sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "confirmUsekey")));
                        return true;
                    }
                    plugin.getPVConfig().comandAlert.remove(p.getName());
                }

                if (!plugin.getPayments().isEmpty()) {
                    for (PaymentModel pay : plugin.getPayments()) {
                        plugin.processTrans.put(pay.getPayname(), key);
                        if (pay.checkTransaction(p, key)) {
                            return true;
                        }
                    }
                }
                plugin.getPVConfig().activateVip(p, key, "", 0, p.getName());
            } else {
                sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "onlyPlayers")));
            }
            return true;
        }
        return false;
    }

    /**
     * Command to check the vip time.
     *
     * @return CommandSpec
     */
    public boolean vipTime(CommandSender sender, String[] args) {
        if (sender instanceof Player && args.length == 0) {
            plugin.getUtil().sendVipTime(sender, ((Player) sender).getUniqueId().toString(), sender.getName());
            return true;
        }
        if (args.length == 1 && sender.hasPermission("pixelvip.cmd.player.others")) {
            String uuid = plugin.getPVConfig().getVipUUID(args[0]);
            if (uuid != null) {
                plugin.getUtil().sendVipTime(sender, uuid, args[0]);
            } else {
                sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "noPlayersByName")));
            }
            return true;
        }
        return false;
    }

    /**
     * Command to remove a vip of player.
     *
     * @return CommandSpec
     */
    public boolean removeVip(CommandSender sender, String[] args) {
        if (args.length == 1) {
            String uuid = plugin.getPVConfig().getVipUUID(args[0]);
            if (uuid != null) {
                plugin.getPVConfig().removeVip(uuid, Optional.empty());
                sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "vipsRemoved")));
            } else {
                sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "playerNotVip")));
            }
            return true;
        }
        if (args.length == 2) {
            Optional<String> group = Optional.of(plugin.getPVConfig().getVipByTitle(args[1]));
            if (!plugin.getPVConfig().groupExists(group.get())) {
                sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "noGroups") + args[1]));
                return true;
            }

            String uuid = plugin.getPVConfig().getVipUUID(args[0]);
            if (uuid != null) {
                plugin.getPVConfig().removeVip(uuid, group);
                sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "vipsRemoved")));
            } else {
                sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "playerNotVip")));
            }
            return true;
        }
        return false;
    }

    /**
     * Command to sets the active vip, if more than one key activated.
     *
     * @return CommandSpec
     */
    public boolean setActive(CommandSender sender, String[] args) {
        if (args.length == 1) {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                String group = plugin.getPVConfig().getVipByTitle(args[0]);
                if (!plugin.getPVConfig().groupExists(group)) {
                    sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "noGroups") + args[0]));
                    return true;
                }

                if (plugin.getPVConfig().isVipActive(group, p.getUniqueId().toString())) {
                    p.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "activeVipSetTo") + plugin.getPVConfig().getVipTitle(args[0])));
                    return true;
                }

                List<String[]> vipInfo = plugin.getPVConfig().getVipInfo(p.getUniqueId().toString());

                if (vipInfo.size() > 0) {
                    for (String[] vip : vipInfo) {
                        if (vip[1].equalsIgnoreCase(group)) {
                            plugin.getPVConfig().setActive(p.getUniqueId().toString(), vip[1], Arrays.asList(vip[2].split(",")));
                            p.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "activeVipSetTo") + plugin.getPVConfig().getVipTitle(vip[1])));
                            return true;
                        }
                    }
                }
                sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "playerNotVip")));
                return true;
            } else {
                sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "onlyPlayers")));
                return true;
            }
        }
        if (args.length == 2 && sender.hasPermission("pixelvip.cmd.setactive")) {
            String uuid = plugin.getPVConfig().getVipUUID(args[1]);
            if (uuid == null) {
                sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "noPlayersByName")));
                return true;
            }
            String group = plugin.getPVConfig().getVipByTitle(args[0]);
            if (!plugin.getPVConfig().groupExists(group)) {
                sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "noGroups") + args[0]));
                return true;
            }

            if (plugin.getPVConfig().isVipActive(group, uuid)) {
                sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "activeVipSetTo") + plugin.getPVConfig().getVipTitle(args[0])));
                return true;
            }

            List<String[]> vipInfo = plugin.getPVConfig().getVipInfo(uuid);

            if (vipInfo.size() > 0) {
                for (String[] vip : vipInfo) {
                    if (vip[1].equalsIgnoreCase(group)) {
                        plugin.getPVConfig().setActive(uuid, vip[1], Arrays.asList(vip[2].split(",")));
                        sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "activeVipSetTo") + plugin.getPVConfig().getVipTitle(vip[1])));
                        return true;
                    }
                }
            }
            sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "noGroups") + args[0]));
            return true;
        }
        return false;
    }

    /**
     * Command to add a vip for a player without key.
     *
     * @return CommandSpec
     */
    @SuppressWarnings("deprecation")
    public boolean addVip(CommandSender sender, String[] args) {
        if (args.length == 3) {
            String pname = args[0];
            OfflinePlayer p = Bukkit.getOfflinePlayer(pname);
            if (p.getName() != null) {
                pname = p.getName();
            }
            String group = plugin.getPVConfig().getVipByTitle(args[1]);
            if (!plugin.getPVConfig().groupExists(group)) {
                sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "noGroups") + args[1]));
                return true;
            }
            long days;
            try {
                days = Long.parseLong(args[2]);
            } catch (NumberFormatException ex) {
                return false;
            }
            plugin.getPVConfig().activateVip(p, null, group, days, pname);
            sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "vipAdded")));
            return true;
        }
        return false;
    }

    /**
     * Command to set a vip without activation and without key.
     *
     * @return CommandSpec
     */
    @SuppressWarnings("deprecation")
    public boolean setVip(CommandSender sender, String[] args) {
        if (args.length == 3) {
            String pname = args[0];
            String uuid = plugin.getPVConfig().getVipUUID(pname);
            if (uuid == null) {
                uuid = Bukkit.getOfflinePlayer(pname).getUniqueId().toString();
            }
            String group = plugin.getPVConfig().getVipByTitle(args[1]);
            if (!plugin.getPVConfig().groupExists(group)) {
                sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "noGroups") + group));
                return true;
            }
            long days;
            try {
                days = Long.parseLong(args[2]);
            } catch (NumberFormatException ex) {
                return false;
            }
            plugin.getPVConfig().setVip(uuid, group, plugin.getUtil().dayToMillis(days), pname);
            sender.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "vipSet")));
            return true;
        }
        return false;
    }
}
