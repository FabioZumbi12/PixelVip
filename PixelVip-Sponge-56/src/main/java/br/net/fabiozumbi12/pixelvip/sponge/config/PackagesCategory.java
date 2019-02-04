package br.net.fabiozumbi12.pixelvip.sponge.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.*;

@ConfigSerializable
public class PackagesCategory {

    @Setting
    public Map<String, Packs> packages = defPackages();
    @Setting(value = "pending-variants")
    public Map<String, List<String>> pending_variants = new HashMap<>();
    @Setting
    public Map<String, String> strings = defStrings();

    private Map<String, Packs> defPackages() {
        Map<String, Packs> packages = new HashMap<>();
        packages.put("999", new Packs(Arrays.asList("give {p} diamond_block 10", "eco add {p} 1000"),
                new HashMap<>(), "Select a gender for your VIP: "));
        packages.put("998", new Packs(new ArrayList<>(), new HashMap<>(), "Select an option: "));
        return packages;
    }

    private Map<String, String> defStrings() {
        Map<String, String> strings = new HashMap<>();
        strings.put("choose", "&bClick in one of the available variants to choose one: ");
        strings.put("hover-info", "&eClick to select this variant!");
        strings.put("use-cmd", "&bUse the following command to select your variant: &e\\givepackage %s [variant]");
        strings.put("variants", "&bSelect a variant: ");
        strings.put("no-package", "&cThere's no package with id {id}! Use /listpackages to list all available packages.");
        strings.put("no-pendent", "&cYou don''t have any pendent variants to use!");
        return strings;
    }

    @ConfigSerializable
    public static class Packs {
        public Packs() {}

        public Packs(List<String> commands, Map<String, List<String>> options, String message) {
            this.commands = commands;
            for (Map.Entry<String, List<String>> opt : options.entrySet()) {
                this.variants.options.put(opt.getKey(), new VariantsCat.OptionsCat(opt.getValue()));
            }
            this.variants.message = message;
        }

        @Setting
        public List<String> commands = new ArrayList<>();

        @Setting
        public VariantsCat variants = new VariantsCat();
        @ConfigSerializable
        public static class VariantsCat {
            @Setting
            public String message = "Select an option: ";
            @Setting
            public Map<String, OptionsCat> options = defOpts();

            private Map<String, OptionsCat> defOpts() {
                Map<String, OptionsCat> options = new HashMap<>();
                options.put("Masculino", new OptionsCat(Collections.singletonList("addvip {p} vip2m 10")));
                options.put("Feminino", new OptionsCat(Collections.singletonList("addvip {p} vip2f 10")));
                return options;
            }

            @ConfigSerializable
            public static class OptionsCat {
                @Setting
                public List<String> commands = new ArrayList<>();

                public OptionsCat() {}
                public OptionsCat(List<String> commands) {
                    this.commands = commands;
                }
            }
        }
    }
}
