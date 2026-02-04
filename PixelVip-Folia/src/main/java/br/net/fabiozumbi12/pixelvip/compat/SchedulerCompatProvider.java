package br.net.fabiozumbi12.pixelvip.compat;

import org.bukkit.plugin.Plugin;

public final class SchedulerCompatProvider {
    private SchedulerCompatProvider() {
    }

    public static SchedulerCompat create(Plugin plugin) {
        return new SchedulerCompatImpl(plugin);
    }
}
