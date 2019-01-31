package br.net.fabiozumbi12.pixelvip.sponge.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.*;

@ConfigSerializable
public class VipsCategory {

    @Setting(comment = "Add the commands to run when the player use the key for activation \n" +
            "You can use the variables:\n" +
            "{p} = Player name, {vip} = Vip group, {days} = Vip days, {playergroup} = Player group before activate vip")
    public List<String> commands = Arrays.asList(
            "broadcast &aThe player &6{p} &ahas acquired your &6{vip} &afor &6{days} &adays",
            "give {p} minecraft:diamond 10",
            "eco give {p} 10000");

    @Setting(comment = "Add commands here to give items to players based on chances.\n" +
            "Use 1 - 100 for add chance commands.")
    public Map<String, List<String>> cmdChances = cmdChanges();
    private Map<String, List<String>> cmdChanges() {
        Map<String, List<String>> map = new HashMap<>();
        map.put("30", Collections.singletonList("give {p} minecraft:mob_spawner 1"));
        map.put("50", Collections.singletonList("give {p} minecraft:diamond_block 5"));
        return  map;
    }

    @Setting(value = "run-on-vip-end")
    public List<String> run_on_vip_end = new ArrayList<>();
}
