package br.net.fabiozumbi12.pixelvip.sponge.Packages;

import br.net.fabiozumbi12.pixelvip.sponge.PixelVip;
import br.net.fabiozumbi12.pixelvip.sponge.config.PackagesCategory;
import org.spongepowered.api.entity.living.player.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PVPackage {
    private PixelVip plugin;
    private String id;
    private HashMap<String,List<String>> variants;
    private PackagesCategory packages;
    private PackagesCategory.Packs pack;

    public PVPackage(PixelVip plugin, String id, PackagesCategory packages){
        this.packages = packages;
        this.plugin = plugin;
        this.id = id;
        this.pack = packages.packages.get(id);

        if (!packages.packages.get(id).variants.options.isEmpty()){
            variants = new HashMap<>();
            for (Map.Entry<String, PackagesCategory.Packs.VariantsCat.OptionsCat> var:packages.packages.get(id).variants.options.entrySet()){
                variants.put(var.getKey(), var.getValue().commands);
            }
        }
    }

    public String getVarMessage(){
        return this.pack.variants.message;
    }

    public String getID(){
        return this.id;
    }

    public List<String> getCommands(){
        return this.pack.commands;
    }

    public HashMap<String,List<String>> getVariants(){
        return this.variants;
    }

    public void runCommands(Player player){
        for (String cmd:this.pack.commands){
            plugin.getUtil().ExecuteCmd(cmd, player);
        }
    }

    public boolean hasVariant(String variant){
        return variants != null && variants.containsKey(variant);
    }

    public void giveVariant(Player player, String var){
        for (String cmd:variants.get(var))
            plugin.getUtil().ExecuteCmd(cmd, player);

        List<String> pending = this.packages.pending_variants.get(player.getName());
        pending.remove(id);
        if (!pending.isEmpty())
            this.packages.pending_variants.put(player.getName(), pending);
        else
            this.packages.pending_variants.remove(player.getName());
        PixelVip.get().getPackageManager().savePackages(this.packages);
    }
}