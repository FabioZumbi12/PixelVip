package br.net.fabiozumbi12.pixelvip.bukkit.Packages;

import br.net.fabiozumbi12.pixelvip.bukkit.PixelVip;
import br.net.fabiozumbi12.pixelvip.bukkit.bungee.SpigotText;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PVPackage {
    private PixelVip plugin;
    private String id;
    private List<String> commands;
    private HashMap<String,List<String>> variants;
    private YamlConfiguration packages;

    public PVPackage(PixelVip plugin, String id, YamlConfiguration packages){
        this.packages = packages;
        this.plugin = plugin;
        this.id = id;
        commands = packages.getStringList("packages." + id + ".commands");

        if (packages.getConfigurationSection("packages." + id + ".variants.options") != null && !packages.getConfigurationSection("packages." + id + ".variants.options").getKeys(false).isEmpty()){
            variants = new HashMap<>();
            for (String var:packages.getConfigurationSection("packages." + id + ".variants.options").getKeys(false)){
                variants.put(var, packages.getStringList("packages." + id + ".variants.options." + var));
            }
        }
    }

    public String getID(){
        return this.id;
    }

    public List<String> getCommands(){
        return this.commands;
    }

    public HashMap<String,List<String>> getVariants(){
        return this.variants;
    }

    public void runCommands(Player player){
        for (String cmd:this.commands){
            plugin.getUtil().ExecuteCmd(cmd, player);
        }
    }

    public boolean hasVariant(String variant){
        return packages.contains("packages." + id + ".variants.options." + variant);
    }

    public void giveVariant(Player player, String var){
        for (String cmd:packages.getStringList("packages." + id + ".variants.options." + var))
            plugin.getUtil().ExecuteCmd(cmd, player);

        List<String> pending = this.packages.getStringList("pending-variants." + player.getName());
        pending.remove(id);
        this.packages.set("pending-variants." + player.getName(), pending.isEmpty() ? null : pending);
        try {
            this.packages.save(new File(plugin.getDataFolder(), "packages.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}