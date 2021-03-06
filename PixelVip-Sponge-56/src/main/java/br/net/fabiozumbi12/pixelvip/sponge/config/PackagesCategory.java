package br.net.fabiozumbi12.pixelvip.sponge.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.*;

@ConfigSerializable
public class PackagesCategory {

    @Setting
    public HandCat hand = new HandCat();
    @Setting
    public Map<String, Packs> packages = defPackages();
    @Setting(value = "pending-variants")
    public Map<String, List<String>> pending_variants = new HashMap<>();
    @Setting
    public Map<String, String> strings = defStrings();

    private Map<String, Packs> defPackages() {
        Map<String, Packs> packages = new HashMap<>();
        packages.put("999", new Packs(Arrays.asList("give {p} diamond_block 10", "eco add {p} 1000"),
                new HashMap<>(), ""));
        packages.put("998", new Packs(new ArrayList<>(), defOpts(), "Select a gender for your VIP: "));
        return packages;
    }

    private Map<String, List<String>> defOpts() {
        Map<String, List<String>> options = new HashMap<>();
        options.put("Masculino", Collections.singletonList("addvip {p} vip2m 10"));
        options.put("Feminino", Collections.singletonList("addvip {p} vip2f 10"));
        return options;
    }

    private Map<String, String> defStrings() {
        Map<String, String> strings = new HashMap<>();
        strings.put("choose", "&bClick in one of the available variants to choose one: ");
        strings.put("hover-info", "&eClick to select this variant!");
        strings.put("use-cmd", "&bUse the following command to select your variant: &e\\givepackage %s [variant]");
        strings.put("variants", "&bSelect a variant: ");
        strings.put("no-package", "&cThere's no package with id {id}! Use /listpackages to list all available packages.");
        strings.put("no-pendent", "&cYou don''t have any pendent variants to use!");
        strings.put("added", "&aPackage added with success!");
        strings.put("removed", "&aPackage removed with success!");
        strings.put("exists", "&cA package with id {id} already exists!");
        strings.put("hand-empty", "&cYour hand is empty or an invalid item!");
        return strings;
    }

    @ConfigSerializable
    public static class HandCat {

        @Setting
        public String command = "give {p} {item} {amount}";
    }

    @ConfigSerializable
    public static class Packs {
        @Setting
        public List<String> commands = new ArrayList<>();
        @Setting
        public VariantsCat variants = new VariantsCat();

        public Packs() {
        }

        public Packs(List<String> commands, Map<String, List<String>> options, String message) {
            this.commands = commands;
            for (Map.Entry<String, List<String>> opt : options.entrySet()) {
                this.variants.options.put(opt.getKey(), new VariantsCat.OptionsCat(opt.getValue()));
            }
            this.variants.message = message;
        }

        @ConfigSerializable
        public static class VariantsCat {
            @Setting
            public String message = "Select an option: ";
            @Setting
            public Map<String, OptionsCat> options = new HashMap<>();

            @ConfigSerializable
            public static class OptionsCat {
                @Setting
                public List<String> commands = new ArrayList<>();

                public OptionsCat() {
                }

                public OptionsCat(List<String> commands) {
                    this.commands = commands;
                }
            }
        }
    }
}
