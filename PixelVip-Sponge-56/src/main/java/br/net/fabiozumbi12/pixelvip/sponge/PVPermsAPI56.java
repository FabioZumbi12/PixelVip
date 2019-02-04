package br.net.fabiozumbi12.pixelvip.sponge;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class PVPermsAPI56 implements PVPermsAPI {
    private PermissionService permissionService;

    public PVPermsAPI56() {
        this.permissionService = Sponge.getGame().getServiceManager().getRegistration(PermissionService.class).get().getProvider();
    }

    public List<String> getPlayerGroups(User player) {
        List<String> subs = new ArrayList<>();
        for (Subject sub : player.getParents()) {
            if (sub.getContainingCollection().equals(getGroups()) && sub.getIdentifier() != null) {
                subs.add(sub.getIdentifier());
            }
        }
        return subs;
    }

    public String getHighestGroup(User player) {
        HashMap<Integer, String> subs = new HashMap<>();
        for (Subject sub : player.getParents()) {
            if (sub.getContainingCollection().equals(getGroups()) && sub.getIdentifier() != null) {
                subs.put(sub.getParents().size(), sub.getIdentifier());
            }
        }
        if (!subs.isEmpty()) {
            return subs.get(Collections.max(subs.keySet()));
        }
        return null;
    }

    public SubjectCollection getGroups() {
        return permissionService.getGroupSubjects();
    }
}
