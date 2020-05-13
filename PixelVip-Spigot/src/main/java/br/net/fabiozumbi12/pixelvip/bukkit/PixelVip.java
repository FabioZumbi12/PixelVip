package br.net.fabiozumbi12.pixelvip.bukkit;

import br.net.fabiozumbi12.pixelvip.bukkit.Packages.PackageManager;
import br.net.fabiozumbi12.pixelvip.bukkit.PaymentsAPI.MercadoPagoHook;
import br.net.fabiozumbi12.pixelvip.bukkit.PaymentsAPI.PagSeguroHook;
import br.net.fabiozumbi12.pixelvip.bukkit.PaymentsAPI.PayPalHook;
import br.net.fabiozumbi12.pixelvip.bukkit.PaymentsAPI.PaymentModel;
import br.net.fabiozumbi12.pixelvip.bukkit.bungee.PixelVipBungee;
import br.net.fabiozumbi12.pixelvip.bukkit.cmds.PVCommands;
import br.net.fabiozumbi12.pixelvip.bukkit.config.PVConfig;
import br.net.fabiozumbi12.pixelvip.bukkit.metrics.Metrics;
import com.earth2me.essentials.Essentials;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class PixelVip extends JavaPlugin implements Listener {

    public PixelVip plugin;
    public Server serv;
    public PluginDescriptionFile pdf;
    public Essentials ess;
    public HashMap<String, String> processTrans;
    private PVLogger logger;
    private int task = 0;
    private List<PaymentModel> payments;
    private PVUtil util;
    private Permission perms;
    private PVConfig config;
    private PermsAPI permApi;
    private PixelVipBungee pvBungee;
    private PackageManager packageManager;

    public PackageManager getPackageManager() {
        return this.packageManager;
    }

    public List<PaymentModel> getPayments() {
        return this.payments;
    }

    public PVUtil getUtil() {
        return this.util;
    }

    public PVLogger getPVLogger() {
        return this.logger;
    }

    public PVConfig getPVConfig() {
        return this.config;
    }

    public void reloadCmd(CommandSender sender) {
        logger.info("Reloading config module...");
        if (config != null) {
            config.closeCon();
        }
        if (this.permApi != null) {
            Bukkit.getScheduler().cancelTask(this.permApi.taskId);
            this.permApi = new PermsAPI(perms, this);
        }
        this.config = new PVConfig(this);

        //init database
        this.config.reloadVips();

        reloadVipTask();
        this.config.getCommConfig().saveConfig();

        //payment apis
        setupPayments();

        //package manager
        packageManager = new PackageManager(this);

        sender.sendMessage(plugin.getUtil().toColor(config.getRoot().getString("strings.reload")));
        logger.warning(util.toColor("We have " + config.getVipList().size() + " active Vips on " + config.getRoot().getString("configs.database.type")));
    }

    public PermsAPI getPerms() {
        return this.permApi;
    }

    public PixelVipBungee getPVBungee() {
        return this.pvBungee;
    }

    public void onEnable() {
        plugin = this;
        serv = getServer();
        serv.getPluginManager().registerEvents(this, this);
        processTrans = new HashMap<>();

        //register bungee
        pvBungee = new PixelVipBungee(this);
        serv.getPluginManager().registerEvents(pvBungee, this);
        serv.getMessenger().registerOutgoingPluginChannel(this, "bungee:pixelvip");
        serv.getMessenger().registerIncomingPluginChannel(this, "bungee:pixelvip", pvBungee);

        logger = new PVLogger();
        pdf = getDescription();

        logger.info("Init config module...");
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        this.config = new PVConfig(this);

        //init database
        this.config.reloadVips();

        logger.info("Init utils module...");
        this.util = new PVUtil(this);

        logger.info("Init essentials module...");
        Plugin essPl = Bukkit.getServer().getPluginManager().getPlugin("Essentials");
        if (essPl != null && essPl.isEnabled()) {
            logger.info(util.toColor("Essentials found. Hooked!"));
            ess = (Essentials) essPl;
        }

        logger.info("Init economy module...");
        if (checkVault()) {
            RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
            if (rsp == null) {
                super.setEnabled(false);
                logger.severe("-> Vault not found. This plugin needs Vault to work! Disabling...");
                return;
            } else {
                perms = rsp.getProvider();
                logger.info("-> Vault found. Hooked.");
            }
            this.permApi = new PermsAPI(perms, this);
        } else {
            super.setEnabled(false);
            logger.info("-> Vault not found. This plugin needs Vault to work! Disabling...");
            return;
        }

        //payment apis
        setupPayments();

        //package manager
        packageManager = new PackageManager(this);

        logger.info("Init commands module...");
        new PVCommands(this);

        logger.info("Init scheduler module...");
        reloadVipTask();

        if (checkPHAPI()) {
            new PixelPHAPI(this).register();
            logger.info("-> PlaceHolderAPI found. Hooked.");
        }

        logger.warning(util.toColor("We have " + config.getVipList().size() + " active Vips on " + getPVConfig().getRoot().getString("configs.database.type")));
        logger.sucess(util.toColor("PixelVip enabled!"));

        // Metrics
        try {
            Metrics metrics = new Metrics(this);
            metrics.addCustomChart(new Metrics.SingleLineChart("active_vips", () -> config.getVipList().size()));
            if (metrics.isEnabled())
                getLogger().info("Metrics enabled! See our stats here: https://bstats.org/plugin/bukkit/PixelVip");
        } catch (Exception ex) {
            getLogger().info("Metrics not enabled due errors: " + ex.getLocalizedMessage());
        }
    }

    private void setupPayments() {
        payments = new ArrayList<>();
        //pagseguro
        if (getPVConfig().getApiRoot().getBoolean("apis.pagseguro.use") && Bukkit.getPluginManager().getPlugin("PagSeguroAPI") != null) {
            this.payments.add(new PagSeguroHook(this));
            logger.info("-> PagSeguroAPI found and hooked.");
        }

        //mercadopago
        if (getPVConfig().getApiRoot().getBoolean("apis.mercadopago.use") && Bukkit.getPluginManager().getPlugin("MercadoPagoAPI") != null) {
            this.payments.add(new MercadoPagoHook(this));
            logger.info("-> MercadoPagoAPI found and hooked.");
        }

        //paypal
        if (getPVConfig().getApiRoot().getBoolean("apis.paypal.use") && Bukkit.getPluginManager().getPlugin("PayPalAPI") != null) {
            this.payments.add(new PayPalHook(this));
            logger.info("-> PayPalAPI found and hooked.");
        }
    }

    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(plugin);
        logger.severe(util.toColor("PixelVip disabled!"));
    }

    private void reloadVipTask() {
        logger.info("Reloading tasks...");
        if (task != 0) {
            Bukkit.getScheduler().cancelTask(task);
            logger.info("-> Task stopped");
        }

        task = serv.getScheduler().runTaskTimer(plugin, () -> getPVConfig().getVipList().forEach((uuid, value) -> {
            OfflinePlayer p = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
            value.forEach((vipInfo) -> {
                long dur = new Long(vipInfo[0]);
                if (p.getName() != null && permApi.getGroups(p) != null && !Arrays.asList(permApi.getGroups(p)).contains(vipInfo[1])) {
                    config.runChangeVipCmds(p, vipInfo[1], permApi.getGroup(p));
                }
                if (dur <= util.getNowMillis()) {
                    getPVConfig().removeVip(uuid, Optional.of(vipInfo[1]));
                    if (p.isOnline()) {
                        p.getPlayer().sendMessage(util.toColor(config.getLang("_pluginTag", "vipEnded").replace("{vip}", vipInfo[1])));
                    }
                    Bukkit.getConsoleSender().sendMessage(util.toColor(config.getLang("_pluginTag") + "&bThe vip &6" + vipInfo[1] + "&b of player &6" + vipInfo[4] + " &bhas ended!"));
                }
            });
        }), 0, 20 * 60).getTaskId();
        logger.info("-> Task started");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        //check player uuid async
        Bukkit.getScheduler().runTaskAsynchronously(this, () ->
                getPVConfig().getVipList().forEach((key, value) -> {
                    for (String[] vipInfo : value) {
                        String oldUUid = getPVConfig().getVipUUID(p.getName());
                        if (vipInfo[4].equals(p.getName()) && !p.getUniqueId().toString().equals(oldUUid)) {
                            getPVConfig().changeUUIDs(oldUUid, p.getUniqueId().toString());
                        }
                    }
                }));

        //check player groups if is on vip group without vip info
        serv.getScheduler().runTaskLater(plugin, () -> {
            if (permApi.getGroups(p) != null) {
                for (String g : permApi.getGroups(p)) {
                    if (getPVConfig().getGroupList(true).contains(g) && getPVConfig().getVipInfo(p.getUniqueId().toString()).isEmpty()) {
                        permApi.removeGroup(p.getUniqueId().toString(), g);
                    }
                }
            }
        }, 40);

        if (getPVConfig().queueCmds()) {
            plugin.serv.getScheduler().runTaskLater(plugin, () -> {
                List<String> qcmds = getPVConfig().getQueueCmds(p.getUniqueId().toString());
                qcmds.forEach((cmd) -> {
                    plugin.serv.getScheduler().runTaskLater(plugin, () -> plugin.serv.dispatchCommand(plugin.serv.getConsoleSender(), cmd), 10);
                });
            }, 60);
        }
    }

    //check if plugin Vault is installed
    private boolean checkVault() {
        Plugin pVT = Bukkit.getPluginManager().getPlugin("Vault");
        return pVT != null && pVT.isEnabled();
    }

    private boolean checkPHAPI() {
        Plugin p = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
        return p != null && p.isEnabled();
    }

    public void addLog(String log) {
        String timeStamp = new SimpleDateFormat("dd.MM.yyyy - HH:mm:ss").format(Calendar.getInstance().getTime());
        try {
            File folder = new File(this.getDataFolder() + File.separator + "logs");
            if (!folder.exists()) {
                folder.mkdir();
            }
            File logs = new File(folder + File.separator + "logs.log");

            FileWriter fw = new FileWriter(logs, true);
            fw.append(timeStamp).append(" - PixelVip Log: ").append(log);
            fw.append("\n");
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}