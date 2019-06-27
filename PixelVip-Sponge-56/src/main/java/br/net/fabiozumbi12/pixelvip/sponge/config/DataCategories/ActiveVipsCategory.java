package br.net.fabiozumbi12.pixelvip.sponge.config.DataCategories;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConfigSerializable
public class ActiveVipsCategory {
    //group, players
    @Setting
    public Map<String, Map<String, VipInfo>> activeVips = new HashMap<>();

    @ConfigSerializable
    public static class VipInfo {
        @Setting
        public boolean active;
        @Setting
        public long duration;
        @Setting
        public String expires;
        @Setting
        public String nick;
        @Setting
        public List<String> playerGroup;
        @Setting(value = "kit-cooldown")
        public long kit_cooldown;

        public VipInfo() {
        }

        public VipInfo(boolean active, long duration, String expires, String nick, List<String> playerGroup) {
            this.active = active;
            this.duration = duration;
            this.expires = expires;
            this.nick = nick;
            this.playerGroup = playerGroup;
        }
    }
}
