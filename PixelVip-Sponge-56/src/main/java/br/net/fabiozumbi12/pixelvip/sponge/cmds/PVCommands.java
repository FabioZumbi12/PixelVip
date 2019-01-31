package br.net.fabiozumbi12.pixelvip.sponge.cmds;

import br.net.fabiozumbi12.pixelvip.sponge.Packages.PVPackage;
import br.net.fabiozumbi12.pixelvip.sponge.PaymentsAPI.PaymentModel;
import br.net.fabiozumbi12.pixelvip.sponge.PixelVip;
import br.net.fabiozumbi12.pixelvip.sponge.config.PackagesCategory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PVCommands {

    private PixelVip plugin;

    public PVCommands(PixelVip plugin) {
        this.plugin = plugin;

        Sponge.getCommandManager().register(plugin, delKey(), "delkey");
        Sponge.getCommandManager().register(plugin, newKey(), "newkey", "genkey", "gerarkey");
        Sponge.getCommandManager().register(plugin, newKey(), "sendkey");
        Sponge.getCommandManager().register(plugin, newItemKey(), "newitemkey", "newikey", "gerarikey");
        Sponge.getCommandManager().register(plugin, addItemKey(), "additemkey", "addikey");
        Sponge.getCommandManager().register(plugin, listKeys(), "listkeys", "listarkeys");
        Sponge.getCommandManager().register(plugin, useKey(), "usekey", "usarkey");
        Sponge.getCommandManager().register(plugin, vipTime(), "viptime", "tempovip");
        Sponge.getCommandManager().register(plugin, removeVip(), "removevip", "delvip");
        Sponge.getCommandManager().register(plugin, setActive(), "changevip", "setctive", "trocarvip");
        Sponge.getCommandManager().register(plugin, addVip(), "givevip", "addvip", "darvip");
        Sponge.getCommandManager().register(plugin, setVip(), "setvip");
        Sponge.getCommandManager().register(plugin, setListVips(), "listvips");
        Sponge.getCommandManager().register(plugin, givePackage(), "givepackage", "gpkg", "gpackage");
        Sponge.getCommandManager().register(plugin, getVariant(), "getvariant", "getv", "getvar");
        Sponge.getCommandManager().register(plugin, listPackages(), "listpackages", "listp");
    }


    private CommandSpec listPackages() {
        return CommandSpec.builder()
                .description(Text.of("List all available packages."))
                .permission("pixelvip.cmd.listpackages")
                .executor((src, args) -> {
                    for (String pkg:plugin.getPackageManager().getPackages().packages.keySet()){
                        src.sendMessage(plugin.getUtil().toText("&aID: " + pkg + " - Variants: " + (plugin.getPackageManager().getPackage(pkg).getVariants() != null)));
                    }
                    return CommandResult.success();
                        }).build();
    }

    private CommandSpec getVariant() {
        return CommandSpec.builder()
                .description(Text.of("Allow player to get a pendent variant from a previous key activation"))
                .permission("pixelvip.cmd.player")
                .arguments(GenericArguments.string(Text.of("id")), GenericArguments.string(Text.of("variant")))
                .executor((src, args) -> {
                    PackagesCategory packages = PixelVip.get().getPackageManager().getPackages();
                    if (src instanceof Player){
                        Player p = (Player)src;
                        String id = args.<String>getOne("id").get();
                        String variant = args.<String>getOne("variant").get();
                        if (plugin.getPackageManager().hasPendingPlayer(p)){
                            for (String idv:plugin.getPackageManager().getPendingVariant(p)){
                                if (idv.equalsIgnoreCase(id)){
                                    PVPackage pkg = plugin.getPackageManager().getPackage(idv);
                                    if (pkg.hasVariant(variant)){
                                        pkg.giveVariant(p, variant);
                                    } else {
                                        throw new CommandException(plugin.getUtil().toText(plugin.getConfig().getLang("_pluginTag") + packages.strings.get("no-pendent")), true);
                                    }
                                    break;
                                }
                            }
                            return CommandResult.success();
                        } else {
                            throw new CommandException(plugin.getUtil().toText(plugin.getConfig().getLang("_pluginTag") + packages.strings.get("no-pendent")), true);
                        }
                    }
                    throw new CommandException(plugin.getUtil().toText(plugin.getConfig().getLang("_pluginTag") + packages.strings.get("no-package")), true);
                }).build();
    }

    private CommandSpec givePackage() {
        return CommandSpec.builder()
                .description(Text.of("Gives a package to player."))
                .permission("pixelvip.cmd.givepackage")
                .arguments(GenericArguments.player(Text.of("player")), GenericArguments.string(Text.of("id")))
                .executor((src, args) -> {
                    Player p = args.<Player>getOne("player").get();
                    String id = args.<String>getOne("id").get();
                    PackagesCategory packages = plugin.getPackageManager().getPackages();
                    if (plugin.getPackageManager().getPackage(id) != null){
                        PVPackage pkg = plugin.getPackageManager().getPackage(id);
                        pkg.runCommands(p);
                        if (pkg.getVariants() != null){

                            //add for usage
                            List<String> pending = packages.pending_variants.getOrDefault(p.getName(), new ArrayList<>());
                            pending.add(pkg.getID());
                            packages.pending_variants.put(p.getName(),pending);
                            PixelVip.get().getPackageManager().savePackages(packages);

                            Text.Builder text = Text.builder().append(plugin.getUtil().toText(
                                    plugin.getConfig().getLang("_pluginTag") +
                                    pkg.getVarMessage()));
                            String start = "";
                            for (String var : pkg.getVariants().keySet()) {
                                text.append(plugin.getUtil().toText(start + "&e"+var))
                                        .onClick(TextActions.runCommand(String.format("/getvariant %s %s", pkg.getID(), var)))
                                        .onHover(TextActions.showText(plugin.getUtil().toText(packages.strings.get("hover-info"))))
                                        .build();
                                if (start.equals("")) start = ", ";
                            }
                            p.sendMessage(text.build());
                        }
                    }
                    throw new CommandException(plugin.getUtil().toText(plugin.getConfig().getLang("_pluginTag") + packages.strings.get("no-package")), true);
                }).build();
    }

    private CommandSpec delKey() {
        return CommandSpec.builder()
                .description(Text.of("Remove a generated key."))
                .permission("pixelvip.cmd.delkey")
                .arguments(GenericArguments.string(Text.of("key")))
                .executor((src, args) -> {
                    {
                        if (args.getOne("key").isPresent()) {
                            String key = args.<String>getOne("key").get();
                            if (plugin.getConfig().delKey(key, 1) || plugin.getConfig().delItemKey(key)) {
                                src.sendMessage(plugin.getUtil().toText(plugin.getConfig().getLang("_pluginTag", "keyRemoved") + key));
                            } else {
                                src.sendMessage(plugin.getUtil().toText(plugin.getConfig().getLang("_pluginTag", "noKeyRemoved")));
                            }
                            return CommandResult.success();
                        }
                        throw new CommandException(plugin.getUtil().toText(plugin.getConfig().getLang("_pluginTag", "noKeyRemoved")), true);
                    }
                }).build();
    }

    /**
     * Command to generate new item key.
     *
     * @return boolean
     */
    private CommandSpec newItemKey() {
        return CommandSpec.builder()
                .description(Text.of("Generate keys to give items."))
                .permission("pixelvip.cmd.newitemkey")
                .arguments(GenericArguments.remainingJoinedStrings(Text.of("cmds")))
                .executor((src, args) -> {
                    {
                        String[] cmdLine = args.<String>getOne("cmds").get().replace(", ", ",").split(",");
                        String key = plugin.getUtil().genKey(plugin.getConfig().root().configs.key_size);
                        plugin.getConfig().addItemKey(key, Arrays.asList(cmdLine));

                        src.sendMessage(plugin.getUtil().toText("&b---------------------------------------------"));
                        src.sendMessage(plugin.getUtil().toText(plugin.getConfig().getLang("_pluginTag", "keyGenerated")));
                        plugin.getUtil().sendHoverKey(src, key);
                        for (String cmd : cmdLine) {
                            src.sendMessage(plugin.getUtil().toText(plugin.getConfig().getLang("item") + cmd));
                        }
                        src.sendMessage(plugin.getUtil().toText("&b---------------------------------------------"));
                        return CommandResult.success();
                    }
                }).build();
    }

    /**
     * Command to generate new item key.
     *
     * @return boolean
     */
    private CommandSpec addItemKey() {
        return CommandSpec.builder()
                .description(Text.of("Add items to keys."))
                .permission("pixelvip.cmd.additemkey  ")
                .arguments(GenericArguments.string(Text.of("key")), GenericArguments.remainingJoinedStrings(Text.of("cmds")))
                .executor((src, args) -> {
                    {
                        String key = args.<String>getOne("key").get();
                        String[] cmdLine = args.<String>getOne("cmds").get().replace(", ", ",").split(",");
                        plugin.getConfig().addItemKey(key, new ArrayList<String>(Arrays.asList(cmdLine)));

                        src.sendMessage(plugin.getUtil().toText("&b---------------------------------------------"));
                        src.sendMessage(plugin.getUtil().toText(plugin.getConfig().getLang("_pluginTag", "itemsAdded")));
                        plugin.getUtil().sendHoverKey(src, key);
                        for (String cmd : cmdLine) {
                            src.sendMessage(plugin.getUtil().toText(plugin.getConfig().getLang("item") + cmd));
                        }
                        src.sendMessage(plugin.getUtil().toText("&b---------------------------------------------"));
                        return CommandResult.success();
                    }
                }).build();
    }

    private CommandSpec setListVips() {
        return CommandSpec.builder()
                .description(Text.of("List all player vips."))
                .permission("pixelvip.cmd.newkey")
                .executor((src, args) -> {
                    {
                        HashMap<String, List<String[]>> vips = plugin.getConfig().getVipList();
                        src.sendMessage(plugin.getUtil().toText(plugin.getConfig().getLang("_pluginTag", "list-of-vips")));
                        src.sendMessage(plugin.getUtil().toText("&b---------------------------------------------"));
                        vips.forEach((uuid, vipinfolist) -> vipinfolist.forEach((vipinfo) -> {
                            try {
                                String pname = vipinfo[4];
                                Optional<String> optPname;
                                optPname = Sponge.getServer().getGameProfileManager().get(UUID.fromString(uuid)).get().getName();
                                if (optPname.isPresent()) {
                                    pname = optPname.get();
                                }
                                src.sendMessage(plugin.getUtil().toText("&7> Player &3" + pname + "&7:"));
                                src.sendMessage(plugin.getUtil().toText("  " + plugin.getConfig().getLang("timeGroup") + vipinfo[1]));
                                src.sendMessage(plugin.getUtil().toText("  " + plugin.getConfig().getLang("timeLeft") + plugin.getUtil().millisToMessage(Long.parseLong(vipinfo[0]) - plugin.getUtil().getNowMillis())));
                            } catch (Exception e) {
                            }
                        }));
                        src.sendMessage(plugin.getUtil().toText("&b---------------------------------------------"));
                        return CommandResult.success();
                    }
                })
                .build();

    }

    public void reload() {
        if (Sponge.getCommandManager().containsAlias("newkey")) {
            Sponge.getCommandManager().removeMapping(Sponge.getCommandManager().get("newkey").get());
        }
        Sponge.getCommandManager().register(plugin, newKey(), "newkey", "genkey", "gerarkey");

        if (Sponge.getCommandManager().containsAlias("givevip")) {
            Sponge.getCommandManager().removeMapping(Sponge.getCommandManager().get("givevip").get());
        }
        Sponge.getCommandManager().register(plugin, addVip(), "givevip", "addvip", "darvip");

        if (Sponge.getCommandManager().containsAlias("setvip")) {
            Sponge.getCommandManager().removeMapping(Sponge.getCommandManager().get("setvip").get());
        }
        Sponge.getCommandManager().register(plugin, setVip(), "setvip");
    }

    /**
     * Command to generate new key.
     *
     * @return CommandSpec
     */
    private CommandSpec newKey() {
        return CommandSpec.builder()
                .description(Text.of("Generate new vip key for groups with optional uses."))
                .permission("pixelvip.cmd.newkey")
                .arguments(
                        GenericArguments.choices(Text.of("group"), plugin.getConfig().getCmdChoices()),
                        GenericArguments.longNum(Text.of("days")),
                        GenericArguments.optional(GenericArguments.integer(Text.of("uses"))))
                .executor((src, args) -> {
                    {
                        if (args.hasAny("uses")) {
                            String group = args.<String>getOne("group").get();
                            long days = args.<Long>getOne("days").get();
                            int uses = args.<Integer>getOne("uses").get();

                            if (days <= 0) {
                                throw new CommandException(plugin.getUtil().toText(plugin.getConfig().getLang("_pluginTag", "moreThanZero")));
                            }

                            if (uses <= 0) {
                                throw new CommandException(plugin.getUtil().toText(plugin.getConfig().getLang("_pluginTag", "moreThanZero")));
                            }

                            if (!plugin.getConfig().groupExists(group)) {
                                throw new CommandException(plugin.getUtil().toText(plugin.getConfig().getLang("_pluginTag", "noGroups") + group));
                            }
                            String key = plugin.getUtil().genKey(plugin.getConfig().root().configs.key_size);
                            plugin.getConfig().addKey(key, group, plugin.getUtil().dayToMillis(days), uses);
                            src.sendMessage(plugin.getUtil().toText(plugin.getConfig().getLang("_pluginTag", "keyGenerated")));
                            src.sendMessage(plugin.getUtil().toText(plugin.getConfig().getLang("timeKey") + key));
                            src.sendMessage(plugin.getUtil().toText(plugin.getConfig().getLang("timeGroup") + group));
                            src.sendMessage(plugin.getUtil().toText(plugin.getConfig().getLang("totalTime") + days));
                            src.sendMessage(plugin.getUtil().toText(plugin.getConfig().getLang("infoUses") + uses));
                            return CommandResult.success();
                        } else {
                            String group = args.<String>getOne("group").get();
                            long days = args.<Long>getOne("days").get();

                            if (days <= 0) {
                                throw new CommandException(plugin.getUtil().toText(plugin.getConfig().getLang("_pluginTag", "moreThanZero")));
                            }

                            if (!plugin.getConfig().groupExists(group)) {
                                throw new CommandException(plugin.getUtil().toText(plugin.getConfig().getLang("_pluginTag", "noGroups") + group));
                            }
                            String key = plugin.getUtil().genKey(plugin.getConfig().root().configs.key_size);
                            plugin.getConfig().addKey(key, group, plugin.getUtil().dayToMillis(days), 1);
                            src.sendMessage(plugin.getUtil().toText(plugin.getConfig().getLang("_pluginTag", "keyGenerated")));
                            src.sendMessage(plugin.getUtil().toText(plugin.getConfig().getLang("timeKey") + key));
                            src.sendMessage(plugin.getUtil().toText(plugin.getConfig().getLang("timeGroup") + group));
                            src.sendMessage(plugin.getUtil().toText(plugin.getConfig().getLang("totalTime") + days));
                            return CommandResult.success();
                        }
                    }
                })
                .build();
    }


    /**
     * Command to list all available keys, and key's info.
     *
     * @return CommandSpec
     */
    public CommandSpec listKeys() {
        return CommandSpec.builder()
                .description(Text.of("List all available keys."))
                .permission("pixelvip.cmd.listkeys")
                .executor((src, args) -> {
                    {
                        Collection<String> keys = plugin.getConfig().getListKeys();
                        Collection<String> itemKeys = plugin.getConfig().getItemListKeys();

                        int i = 0;
                        if (keys.size() > 0) {
                            src.sendMessage(plugin.getUtil().toText("&b---------------------------------------------"));
                            src.sendMessage(plugin.getUtil().toText(plugin.getConfig().getLang("_pluginTag", "listKeys")));
                            for (Object key : keys) {
                                String[] keyinfo = plugin.getConfig().getKeyInfo(key.toString());
                                long days = plugin.getUtil().millisToDay(keyinfo[1]);
                                src.sendMessage(plugin.getUtil().toText("&b- Key: &6" + key.toString() + "&b | Group: &6" + keyinfo[0] + "&b | Days: &6" + days + "&b | Uses left: &6" + keyinfo[2]));
                                i++;
                            }
                        }

                        if (itemKeys.size() > 0) {
                            src.sendMessage(plugin.getUtil().toText("&b---------------------------------------------"));
                            src.sendMessage(plugin.getUtil().toText(plugin.getConfig().getLang("_pluginTag", "listItemKeys")));
                            for (Object key : itemKeys) {
                                List<String> cmds = plugin.getConfig().getItemKeyCmds(key.toString());
                                src.sendMessage(plugin.getUtil().toText(plugin.getConfig().getLang("timeKey") + key.toString()));
                                for (String cmd : cmds) {
                                    src.sendMessage(plugin.getUtil().toText(plugin.getConfig().getLang("item") + cmd));
                                }
                                i++;
                            }
                        }

                        if (i == 0) {
                            src.sendMessage(plugin.getUtil().toText(plugin.getConfig().getLang("_pluginTag", "noKeys")));
                        } else {
                            src.sendMessage(plugin.getUtil().toText("&b---------------------------------------------"));
                        }
                        return CommandResult.success();
                    }
                })
                .build();
    }

    /**
     * Command to activate a vip using a key.
     *
     * @return CommandSpec
     */
    public CommandSpec useKey() {
        return CommandSpec.builder()
                .description(Text.of("Use a key to activate the Vip."))
                .permission("pixelvip.cmd.player")
                .arguments(GenericArguments.string(Text.of("key")))
                .executor((src, args) -> {
                    {
                        if (src instanceof Player) {
                            Player p = (Player) src;
                            String key = args.<String>getOne(Text.of("key")).get().toUpperCase();
                            if (!plugin.getPayments().isEmpty()) {
                                for (PaymentModel pay : plugin.getPayments()) {
                                    plugin.processTrans.put(pay.getPayname(), key);
                                    if (pay.checkTransaction(p, key)) {
                                        return CommandResult.success();
                                    }
                                }
                                plugin.processTrans.remove(key);
                            }
                            return plugin.getConfig().activateVip(p, key, "", 0, p.getName());
                        }
                        throw new CommandException(plugin.getUtil().toText(plugin.getConfig().getLang("_pluginTag", "onlyPlayers")));
                    }
                })
                .build();
    }

    /**
     * Command to check the vip time.
     *
     * @return CommandSpec
     */
    public CommandSpec vipTime() {
        return CommandSpec.builder()
                .description(Text.of("Use to check the vip time."))
                .permission("pixelvip.cmd.player")
                .arguments(GenericArguments.optional(GenericArguments.user(Text.of("player"))))
                .executor((src, args) -> {
                    {
                        if (!(src instanceof Player) && !args.hasAny("player")) {
                            throw new CommandException(plugin.getUtil().toText(plugin.getConfig().getLang("_pluginTag", "onlyPlayers")), true);
                        }
                        if (src.hasPermission("pixelvip.cmd.player.others") && args.hasAny("player")) {
                            Optional<User> optp = args.getOne("player");
                            if (optp.isPresent()) {
                                User p = optp.get();
                                return plugin.getUtil().sendVipTime(src, p.getUniqueId().toString(), p.getName());
                            } else {
                                throw new CommandException(plugin.getUtil().toText(plugin.getConfig().getLang("_pluginTag", "noPlayersByName")));
                            }
                        } else {
                            return plugin.getUtil().sendVipTime(src, ((Player) src).getUniqueId().toString(), src.getName());
                        }
                    }
                })
                .build();
    }

    /**
     * Command to remove a vip of player.
     *
     * @return CommandSpec
     */
    public CommandSpec removeVip() {
        return CommandSpec.builder()
                .description(Text.of("Use to remove a vip of player."))
                .permission("pixelvip.cmd.removevip")
                .arguments(GenericArguments.user(Text.of("player")), GenericArguments.optional(GenericArguments.string(Text.of("vip"))))
                .executor((src, args) -> {
                    {
                        if (args.hasAny("vip")) {
                            User optp = args.<User>getOne("player").get();
                            Optional<String> group = args.getOne("vip");
                            if (!plugin.getConfig().groupExists(group.get())) {
                                throw new CommandException(plugin.getUtil().toText(plugin.getConfig().getLang("_pluginTag", "noGroups") + group.get()));
                            }

                            if (optp != null) {
                                plugin.getConfig().removeVip(optp, group);
                                src.sendMessage(plugin.getUtil().toText(plugin.getConfig().getLang("_pluginTag", "vipsRemoved")));
                            } else {
                                src.sendMessage(plugin.getUtil().toText(plugin.getConfig().getLang("_pluginTag", "noPlayersByName")));
                            }
                            return CommandResult.success();
                        } else {
                            User optp = args.<User>getOne("player").get();
                            plugin.getConfig().removeVip(optp, Optional.empty());
                            src.sendMessage(plugin.getUtil().toText(plugin.getConfig().getLang("_pluginTag", "vipsRemoved")));
                            return CommandResult.success();
                        }
                    }
                })
                .build();
    }

    /**
     * Command to sets the active vip, if more than one key activated.
     *
     * @return CommandSpec
     */
    public CommandSpec setActive() {
        return CommandSpec.builder()
                .description(Text.of("Use to change your active VIP, if more keys activated."))
                .permission("pixelvip.cmd.player")
                .arguments(GenericArguments.string(Text.of("vip")))
                .executor((src, args) -> {
                    {
                        if (src instanceof Player) {
                            Player p = (Player) src;
                            String group = args.<String>getOne("vip").get();
                            if (!plugin.getConfig().groupExists(group)) {
                                throw new CommandException(plugin.getUtil().toText(plugin.getConfig().getLang("_pluginTag", "noGroups") + group));
                            }

                            List<String[]> vipInfo = plugin.getConfig().getVipInfo(p.getUniqueId().toString());

                            if (vipInfo.size() > 0) {
                                for (String[] vip : vipInfo) {
                                    if (vip[1].equalsIgnoreCase(group)) {
                                        plugin.getConfig().setActive(p.getUniqueId().toString(), vip[1], vip[2]);
                                        p.sendMessage(plugin.getUtil().toText(plugin.getConfig().getLang("_pluginTag", "activeVipSetTo") + vip[1]));
                                        return CommandResult.success();
                                    }
                                }
                            }
                            throw new CommandException(plugin.getUtil().toText(plugin.getConfig().getLang("_pluginTag", "noGroups") + group));
                        }
                        throw new CommandException(plugin.getUtil().toText(plugin.getConfig().getLang("_pluginTag", "onlyPlayers")));
                    }
                })
                .build();
    }

    /**
     * Command to add a vip for a player without key.
     *
     * @return CommandSpec
     */
    public CommandSpec addVip() {
        return CommandSpec.builder()
                .description(Text.of("Use to add a vip for a player without key."))
                .permission("pixelvip.cmd.addvip")
                .arguments(
                        GenericArguments.user(Text.of("player")),
                        GenericArguments.choices(Text.of("vip"), plugin.getConfig().getCmdChoices()),
                        GenericArguments.longNum(Text.of("days")))
                .executor((src, args) -> {
                    {
                        User p = args.<User>getOne("player").get();
                        String group = args.<String>getOne(Text.of("vip")).get();
                        long days = args.<Long>getOne(Text.of("days")).get();

                        return plugin.getConfig().activateVip(p, null, group, days, p.getName());
                    }
                })
                .build();
    }

    /**
     * Command to set a vip without activation and without key.
     *
     * @return CommandSpec
     */
    public CommandSpec setVip() {
        return CommandSpec.builder()
                .description(Text.of("Use to set a vip without activation and without key."))
                .permission("pixelvip.cmd.setvip")
                .arguments(GenericArguments.user(Text.of("player")), GenericArguments.choices(Text.of("vip"), plugin.getConfig().getCmdChoices()), GenericArguments.longNum(Text.of("days")))
                .executor((src, args) -> {
                    {
                        User p = args.<User>getOne("player").get();
                        String group = args.<String>getOne(Text.of("vip")).get();
                        long days = args.<Long>getOne(Text.of("days")).get();

                        plugin.getConfig().setVip(p.getUniqueId().toString(), group, plugin.getUtil().dayToMillis(days), p.getName());
                        src.sendMessage(plugin.getUtil().toText(plugin.getConfig().getLang("_pluginTag", "vipSet")));
                        return CommandResult.success();
                    }
                })
                .build();
    }
}
