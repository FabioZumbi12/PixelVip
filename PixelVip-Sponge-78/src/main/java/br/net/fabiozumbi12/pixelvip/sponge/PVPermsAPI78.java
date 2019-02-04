package br.net.fabiozumbi12.pixelvip.sponge;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class PVPermsAPI78 implements PVPermsAPI {
    private PermissionService permissionService;

    public PVPermsAPI78() {
        this.permissionService = Sponge.getGame().getServiceManager().getRegistration(PermissionService.class).get().getProvider();
    }

    public List<String> getPlayerGroups(User player) {
        List<String> subs = new ArrayList<>();
        try {
            for (SubjectReference sub : player.getParents()) {
                if (sub.getCollectionIdentifier().equals(getGroups().getIdentifier()) && (sub.getSubjectIdentifier() != null)) {
                    Subject subj = sub.resolve().get();
                    subs.add(subj.getIdentifier());
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return subs;
    }

    public String getHighestGroup(User player) {
        HashMap<Integer, String> subs = new HashMap<>();
        try {
            for (SubjectReference sub : player.getParents()) {
                if (sub.getCollectionIdentifier().equals(getGroups().getIdentifier()) && (sub.getSubjectIdentifier() != null)) {
                    Subject subj = sub.resolve().get();
                    subs.put(subj.getParents().size(), subj.getIdentifier());
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
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
