package br.net.fabiozumbi12.pixelvip.sponge;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;

public class PVPermsAPI56 implements PVPermsAPI {
    private PermissionService permissionService;

    public PVPermsAPI56() {
        this.permissionService = Sponge.getGame().getServiceManager().getRegistration(PermissionService.class).get().getProvider();
    }

    public String getGroup(User player) {
        for (Subject sub : player.getParents()) {
            if (sub.getContainingCollection().equals(getGroups()) && (sub.getIdentifier() != null)) {
                return sub.getIdentifier();
            }
        }
        return null;
    }
	/*
	public Subject getGroups(User player){
		for (Subject sub:player.getParents()){
			if (sub.getContainingCollection().equals(getGroups()) && (sub.getIdentifier() != null)){				
				return sub;
			}
		}
		return null;	
	}*/

    public SubjectCollection getGroups() {
        return permissionService.getGroupSubjects();
    }
}
