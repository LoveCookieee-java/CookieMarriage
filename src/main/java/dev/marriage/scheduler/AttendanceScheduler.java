package dev.marriage.scheduler;

import dev.marriage.MarriagePlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public final class AttendanceScheduler {

    private final MarriagePlugin plugin;
    private BukkitTask task;

    public AttendanceScheduler(MarriagePlugin plugin) {
        this.plugin = plugin;
    }

public void start() {
        long interval = plugin.getConfigManager().getCheckIntervalTicks();
        task = new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getAttendanceService().tickMinute();
            }
        }.runTaskTimer(plugin, interval, interval);

        plugin.getLogger().info("AttendanceScheduler started (every " + interval + " ticks).");
    }

public void cancel() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
        task = null;
    }
}
