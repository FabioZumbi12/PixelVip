package br.net.fabiozumbi12.pixelvip.compat;

import org.bukkit.entity.Player;

public interface SchedulerCompat {
    TaskHandle runSync(Runnable task);

    TaskHandle runSyncLater(Runnable task, long delayTicks);

    TaskHandle runSyncTimer(Runnable task, long delayTicks, long periodTicks);

    TaskHandle runAsync(Runnable task);

    TaskHandle runAsyncLater(Runnable task, long delayTicks);

    TaskHandle runEntity(Player player, Runnable task);

    TaskHandle runEntityLater(Player player, Runnable task, long delayTicks);

    void cancel(TaskHandle handle);

    void cancelAll();

    boolean isFolia();
}
