package br.net.fabiozumbi12.pixelvip.sponge.Packages;


import br.net.fabiozumbi12.pixelvip.sponge.PixelVip;
import br.net.fabiozumbi12.pixelvip.sponge.config.PackagesCategory;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapperFactory;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.entity.living.player.Player;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class PackageManager {

    private PixelVip plugin;
    private PackagesCategory packages;
    private CommentedConfigurationNode packRoot;
    private ConfigurationLoader<CommentedConfigurationNode> packLoader;

    public PackageManager(PixelVip plugin, ObjectMapperFactory factory) {
        this.plugin = plugin;

        File fPack = new File(plugin.configDir(), "packages.conf");
        try {
            if (!fPack.exists()) {
                fPack.createNewFile();
            }

            packLoader = HoconConfigurationLoader.builder().setFile(fPack).build();
            packRoot = packLoader.load(ConfigurationOptions.defaults().setObjectMapperFactory(factory).setShouldCopyDefaults(true));
            packages = packRoot.getValue(TypeToken.of(PackagesCategory.class), new PackagesCategory());

            savePackages(packages);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void savePackages(PackagesCategory packages) {
        this.packages = packages;
        try {
            packRoot.setValue(TypeToken.of(PackagesCategory.class), packages);
            packLoader.save(packRoot);
        } catch (ObjectMappingException | IOException e) {
            e.printStackTrace();
        }
    }

    public boolean hasPendingPlayer(Player player) {
        return packages.pending_variants.containsKey(player.getName());
    }

    public List<String> getPendingVariant(Player player) {
        return this.packages.pending_variants.get(player.getName());
    }

    public PackagesCategory getPackages() {
        return this.packages;
    }

    public PVPackage getPackage(String id) {
        if (packages.packages.containsKey(id))
            return new PVPackage(plugin, id, packages);
        return null;
    }
}
















