package br.net.fabiozumbi12.pixelvip.sponge.config.DataCategories;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConfigSerializable
public class KeysCategory {

    @Setting(comment = "All available item keys will be here.")
    public Map<String, List<String>> itemKeys = new HashMap<>();
    @Setting(comment = "All available keys will be here.")
    public Map<String, KeysCat> keys = new HashMap<>();

    @ConfigSerializable
    public static class KeysCat {
        @Setting
        public long duration;
        @Setting
        public String group;
        @Setting
        public int uses;

        public KeysCat() {
        }

        public KeysCat(long duration, String group, int uses) {
            this.duration = duration;
            this.group = group;
            this.uses = uses;
        }
    }

}
