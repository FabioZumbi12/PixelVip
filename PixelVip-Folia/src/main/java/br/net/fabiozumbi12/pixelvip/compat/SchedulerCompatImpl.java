package br.net.fabiozumbi12.pixelvip.compat;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Method;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

final class SchedulerCompatImpl implements SchedulerCompat {
    private final Plugin plugin;
    private final BukkitScheduler bukkitScheduler;
    private final Queue<TaskHandle> trackedTasks = new ConcurrentLinkedQueue<>();
    private final boolean folia;
    private final Object globalRegionScheduler;
    private final Object asyncScheduler;
    private final Method globalRun;
    private final Method globalRunDelayed;
    private final Method globalRunAtFixedRate;
    private final Method asyncRunNow;
    private final Method asyncRunDelayed;
    private final Method entityGetScheduler;
    private final Method entityRun;
    private final Method entityRunDelayed;

    SchedulerCompatImpl(Plugin plugin) {
        this.plugin = plugin;
        this.bukkitScheduler = Bukkit.getScheduler();

        Object global = null;
        Object async = null;
        Method gRun = null;
        Method gRunDelayed = null;
        Method gRunAtFixedRate = null;
        Method aRunNow = null;
        Method aRunDelayed = null;
        Method eGetScheduler = null;
        Method eRun = null;
        Method eRunDelayed = null;

        boolean isFolia = false;
        try {
            Server server = plugin.getServer();
            Method getGlobal = server.getClass().getMethod("getGlobalRegionScheduler");
            Method getAsync = server.getClass().getMethod("getAsyncScheduler");
            global = getGlobal.invoke(server);
            async = getAsync.invoke(server);

            gRun = findMethod(global.getClass(), "run", 2);
            gRunDelayed = findMethod(global.getClass(), "runDelayed", 3);
            gRunAtFixedRate = findMethod(global.getClass(), "runAtFixedRate", 4);

            aRunNow = findMethod(async.getClass(), "runNow", 2);
            aRunDelayed = findMethod(async.getClass(), "runDelayed", 3);

            eGetScheduler = Player.class.getMethod("getScheduler");
            try {
                Class<?> entitySchedulerClass = Class.forName("io.papermc.paper.threadedregions.scheduler.EntityScheduler");
                eRun = findMethod(entitySchedulerClass, "run", 3);
                eRunDelayed = findMethod(entitySchedulerClass, "runDelayed", 4);
            } catch (ClassNotFoundException ignored) {
            }
            isFolia = gRun != null && aRunNow != null;
        } catch (Throwable ignored) {
        }

        this.folia = isFolia;
        this.globalRegionScheduler = global;
        this.asyncScheduler = async;
        this.globalRun = gRun;
        this.globalRunDelayed = gRunDelayed;
        this.globalRunAtFixedRate = gRunAtFixedRate;
        this.asyncRunNow = aRunNow;
        this.asyncRunDelayed = aRunDelayed;
        this.entityGetScheduler = eGetScheduler;
        this.entityRun = eRun;
        this.entityRunDelayed = eRunDelayed;
    }

    @Override
    public TaskHandle runSync(Runnable task) {
        TaskHandle handle = folia ? runFolia(globalRun, globalRegionScheduler, task) : new BukkitTaskHandle(bukkitScheduler.runTask(plugin, task));
        track(handle);
        return handle;
    }

    @Override
    public TaskHandle runSyncLater(Runnable task, long delayTicks) {
        TaskHandle handle = folia ? runFolia(globalRunDelayed, globalRegionScheduler, task, delayTicks) : new BukkitTaskHandle(bukkitScheduler.runTaskLater(plugin, task, delayTicks));
        track(handle);
        return handle;
    }

    @Override
    public TaskHandle runSyncTimer(Runnable task, long delayTicks, long periodTicks) {
        TaskHandle handle = folia ? runFolia(globalRunAtFixedRate, globalRegionScheduler, task, delayTicks, periodTicks)
                : new BukkitTaskHandle(bukkitScheduler.runTaskTimer(plugin, task, delayTicks, periodTicks));
        track(handle);
        return handle;
    }

    @Override
    public TaskHandle runAsync(Runnable task) {
        TaskHandle handle = folia ? runFolia(asyncRunNow, asyncScheduler, task) : new BukkitTaskHandle(bukkitScheduler.runTaskAsynchronously(plugin, task));
        track(handle);
        return handle;
    }

    @Override
    public TaskHandle runAsyncLater(Runnable task, long delayTicks) {
        TaskHandle handle = folia ? runFolia(asyncRunDelayed, asyncScheduler, task, delayTicks)
                : new BukkitTaskHandle(bukkitScheduler.runTaskLaterAsynchronously(plugin, task, delayTicks));
        track(handle);
        return handle;
    }

    @Override
    public TaskHandle runEntity(Player player, Runnable task) {
        if (player == null) {
            return runSync(task);
        }
        if (!folia || entityGetScheduler == null || entityRun == null) {
            return runSync(task);
        }
        TaskHandle handle = runEntityFolia(player, entityRun, task);
        track(handle);
        return handle;
    }

    @Override
    public TaskHandle runEntityLater(Player player, Runnable task, long delayTicks) {
        if (player == null) {
            return runSyncLater(task, delayTicks);
        }
        if (!folia || entityGetScheduler == null || entityRunDelayed == null) {
            return runSyncLater(task, delayTicks);
        }
        TaskHandle handle = runEntityFolia(player, entityRunDelayed, task, delayTicks);
        track(handle);
        return handle;
    }

    @Override
    public void cancel(TaskHandle handle) {
        if (handle == null) {
            return;
        }
        handle.cancel();
        trackedTasks.remove(handle);
    }

    @Override
    public void cancelAll() {
        for (TaskHandle handle : trackedTasks) {
            handle.cancel();
        }
        trackedTasks.clear();
    }

    @Override
    public boolean isFolia() {
        return folia;
    }

    private void track(TaskHandle handle) {
        if (handle != null) {
            trackedTasks.add(handle);
        }
    }

    private TaskHandle runFolia(Method method, Object scheduler, Runnable task, Object... extraArgs) {
        if (method == null || scheduler == null) {
            return new NoopTaskHandle();
        }
        try {
            Consumer<Object> consumer = scheduledTask -> task.run();
            Object[] args = new Object[2 + extraArgs.length];
            args[0] = plugin;
            args[1] = consumer;
            System.arraycopy(extraArgs, 0, args, 2, extraArgs.length);
            Object scheduled = method.invoke(scheduler, args);
            return new ReflectiveTaskHandle(scheduled);
        } catch (Throwable ex) {
            task.run();
            return new NoopTaskHandle();
        }
    }

    private TaskHandle runEntityFolia(Player player, Method method, Runnable task, Object... extraArgs) {
        try {
            Object scheduler = entityGetScheduler.invoke(player);
            Consumer<Object> consumer = scheduledTask -> task.run();
            Object[] args = new Object[3 + extraArgs.length];
            args[0] = plugin;
            args[1] = consumer;
            args[2] = null;
            System.arraycopy(extraArgs, 0, args, 3, extraArgs.length);
            Object scheduled = method.invoke(scheduler, args);
            return new ReflectiveTaskHandle(scheduled);
        } catch (Throwable ex) {
            task.run();
            return new NoopTaskHandle();
        }
    }

    private Method findMethod(Class<?> type, String name, int paramCount) {
        for (Method method : type.getMethods()) {
            if (method.getName().equals(name) && method.getParameterCount() == paramCount) {
                return method;
            }
        }
        return null;
    }

    private static final class BukkitTaskHandle implements TaskHandle {
        private final BukkitTask task;

        private BukkitTaskHandle(BukkitTask task) {
            this.task = task;
        }

        @Override
        public void cancel() {
            task.cancel();
        }

        @Override
        public boolean isCancelled() {
            return task.isCancelled();
        }
    }

    private static final class ReflectiveTaskHandle implements TaskHandle {
        private final Object scheduled;
        private final Method cancelMethod;
        private final Method isCancelledMethod;

        private ReflectiveTaskHandle(Object scheduled) {
            this.scheduled = scheduled;
            this.cancelMethod = findMethodSafe(scheduled, "cancel", 0);
            this.isCancelledMethod = findMethodSafe(scheduled, "isCancelled", 0);
        }

        @Override
        public void cancel() {
            if (cancelMethod == null) {
                return;
            }
            try {
                cancelMethod.invoke(scheduled);
            } catch (Throwable ignored) {
            }
        }

        @Override
        public boolean isCancelled() {
            if (isCancelledMethod == null) {
                return false;
            }
            try {
                Object result = isCancelledMethod.invoke(scheduled);
                return result instanceof Boolean && (Boolean) result;
            } catch (Throwable ignored) {
                return false;
            }
        }

        private static Method findMethodSafe(Object target, String name, int paramCount) {
            if (target == null) {
                return null;
            }
            for (Method method : target.getClass().getMethods()) {
                if (method.getName().equals(name) && method.getParameterCount() == paramCount) {
                    return method;
                }
            }
            return null;
        }
    }

    private static final class NoopTaskHandle implements TaskHandle {
        @Override
        public void cancel() {
        }

        @Override
        public boolean isCancelled() {
            return true;
        }
    }
}
