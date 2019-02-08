package br.net.fabiozumbi12.pixelvip.bukkit.Packages;

import br.net.fabiozumbi12.pixelvip.bukkit.PixelVip;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PackageManager {

    private YamlConfiguration packages;
    private PixelVip plugin;
    private File fPack;

    public PackageManager(PixelVip plugin) {
        this.plugin = plugin;

        packages = new YamlConfiguration();
        fPack = new File(plugin.getDataFolder(), "packages.yml");
        try {
            if (!fPack.exists()) {
                fPack.createNewFile();
            }

            packages.load(fPack);
            if (!packages.contains("packages")) {
                packages.set("pending-variants", new HashMap<>());
                packages.set("packages.999.commands", Arrays.asList("givevip {p} vip1 10", "eco add {p} 1000"));
                packages.set("packages.999.variants.options", new HashMap<>());
                packages.set("packages.999.variants.message", "");

                packages.set("packages.998.commands", new ArrayList<>());
                packages.set("packages.998.variants.options.Masculino", Collections.singletonList("addvip {p} vip2m 10"));
                packages.set("packages.998.variants.options.Feminino", Collections.singletonList("addvip {p} vip2f 10"));
                packages.set("packages.998.variants.message", "&aSelect your tag gender (click): ");
                packages.save(fPack);
            }

            packages.set("hand.cmd", packages.getString("hand.cmd", "give {p} {item} {amount}"));

            packages.set("strings.choose", packages.getString("strings.choose", "&bClick in one of the available variants to choose one: "));
            packages.set("strings.hover-info", packages.getString("strings.hover-info","&eClick to select this variant!"));
            packages.set("strings.use-cmd", packages.getString("strings.use-cmd","&bUse the following command to select your variant: &e\\givepackage %s [variant]"));
            packages.set("strings.variants", packages.getString("strings.variants","&bSelect a variant: "));
            packages.set("strings.no-package", packages.getString("strings.no-package","&cThere's no package with id {id}"));
            packages.set("strings.no-pendent", packages.getString("strings.no-pendent","&cYou don't have any pendent variants to use!"));
            packages.set("strings.removed", packages.getString("strings.removed","&aPackage removed with success!"));
            packages.set("strings.added", packages.getString("strings.added","&aPackage added with success!"));
            packages.set("strings.exists", packages.getString("strings.exists","&cA package with id {id} already exists!"));
            packages.set("strings.hand-empty", packages.getString("strings.hand-empty","&cYour hand is empty or an invalid item!"));
            packages.set("strings.only-players", packages.getString("strings.only-players","&cOnly players can use this command to add a item from hand!"));
            packages.save(fPack);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save(){
        try {
            packages.save(fPack);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean hasPendingPlayer(Player player) {
        return !packages.getStringList("pending-variants." + player.getName()).isEmpty();
    }

    public List<String> getPendingVariant(Player player) {
        return this.packages.getStringList("pending-variants." + player.getName());
    }

    public YamlConfiguration getPackages() {
        return this.packages;
    }

    public PVPackage getPackage(String id) {
        if (packages.contains("packages." + id))
            return new PVPackage(plugin, id, packages);
        return null;
    }
}
















