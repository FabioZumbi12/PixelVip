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

    public PackageManager(PixelVip plugin){
        this.plugin = plugin;

        packages = new YamlConfiguration();
        File fPack = new File(plugin.getDataFolder(), "packages.yml");
        try {
            if (!fPack.exists()){
                fPack.createNewFile();
            }

            packages.load(fPack);
            if (!packages.contains("packages")){
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
            if (!packages.contains("strings")){
                packages.set("strings.choose", "&bClick in one of the available variants to choose one: ");
                packages.set("strings.hover-info", "&eClick to select this variant!");
                packages.set("strings.use-cmd", "&bUse the following command to select your variant: &e\\givepackage %s [variant]");
                packages.set("strings.variants", "&bSelect a variant: ");
                packages.set("strings.no-package", "&cThere's no package with id {id}");
                packages.set("strings.no-pendent", "&cYou don't have any pendent variants to use!");
                packages.save(fPack);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean hasPendingPlayer(Player player){
        return !packages.getStringList("pending-variants." + player.getName()).isEmpty();
    }

    public List<String> getPendingVariant(Player player){
        return this.packages.getStringList("pending-variants." + player.getName());
    }

    public YamlConfiguration getPackages(){
        return this.packages;
    }

    public PVPackage getPackage(String id){
        if (packages.contains("packages." + id))
            return new PVPackage(plugin, id, packages);
        return null;
    }
}
















