package br.net.fabiozumbi12.pixelvip.sponge.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConfigSerializable
public class KeysCategory {

    @Setting(comment = "All available item keys will be here.")
    public Map<String, ItemKeysCat> itemKeys = new HashMap<>();

    @ConfigSerializable
    public static class ItemKeysCat {
        public ItemKeysCat(){}
        public ItemKeysCat(List<String> cmds){
            this.cmds = cmds;
        }
        @Setting
        public List<String> cmds;
    }

    @Setting(comment = "All available keys will be here.")
    public Map<String, KeysCat> keys = new HashMap<>();

    @ConfigSerializable
    public static class KeysCat {
        public KeysCat(){}
        public KeysCat(String duration, long group, int uses){
            this.duration = duration;
            this.group = group;
            this.uses = uses;
        }
        @Setting
        public String duration;
        @Setting
        public long group;
        @Setting
        public int uses;
    }

}
