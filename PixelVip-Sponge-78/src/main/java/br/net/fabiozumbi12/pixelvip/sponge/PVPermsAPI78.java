package br.net.fabiozumbi12.pixelvip.sponge;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectReference;

import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class PVPermsAPI78 implements PVPermsAPI {
    private PermissionService permissionService;

    public PVPermsAPI78() {
        this.permissionService = Sponge.getGame().getServiceManager().getRegistration(PermissionService.class).get().getProvider();
    }

    public String getGroup(User player) {
        HashMap<Integer, Subject> subs = new HashMap<>();
        for (SubjectReference sub : player.getParents()) {
            if (sub.getCollectionIdentifier().equals(getGroups().getIdentifier()) && (sub.getSubjectIdentifier() != null)) {
                Subject subj = null;
                try {
                    subj = sub.resolve().get();
                    subs.put(subj.getParents().size(), subj);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
        return subs.isEmpty() ? null : subs.get(Collections.max(subs.keySet())).getIdentifier();
    }
/*
	public Subject getGroups(User player){
		for (SubjectReference sub:player.getParents()){
			if (sub.getCollectionIdentifier().equals(getGroups().getIdentifier()) && (sub.getSubjectIdentifier() != null)){
				return sub.resolve().get();
			}
		}
		return null;
	}*/

    public SubjectCollection getGroups() {
        return permissionService.getGroupSubjects();
    }
}
