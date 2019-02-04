package br.net.fabiozumbi12.pixelvip.sponge;

import org.spongepowered.api.entity.living.player.User;

import java.util.List;

public interface PVPermsAPI {
    List<String> getPlayerGroups(User player);

    String getHighestGroup(User player);
}
