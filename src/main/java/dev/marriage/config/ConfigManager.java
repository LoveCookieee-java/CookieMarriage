package dev.marriage.config;

import dev.marriage.MarriagePlugin;

import java.time.ZoneId;

public final class ConfigManager {

    private final MarriagePlugin plugin;

    public ConfigManager(MarriagePlugin plugin) {
        this.plugin = plugin;
    }

public boolean isNotifyTransactions() {
        return plugin.getConfig().getBoolean("economy.notify-transactions", true);
    }

public int getBalancePollIntervalTicks() {
        int value = plugin.getConfig().getInt("economy.poll-interval-ticks", 200);
        return Math.max(20, value); 
    }

public double getNotifyThreshold() {
        return plugin.getConfig().getDouble("economy.notify-threshold", 0.01);
    }

    public boolean isSplitOnDivorce() {
        return plugin.getConfig().getBoolean("economy.split-on-divorce", true);
    }

public int getProposalTimeoutSeconds() {
        int value = plugin.getConfig().getInt("proposal.timeout-seconds", 60);
        return Math.max(10, value);
    }

public int getRequiredMinutes() {
        int value = plugin.getConfig().getInt("attendance.required-minutes", 60);
        return Math.max(1, value);
    }

public int getCheckIntervalTicks() {
        int value = plugin.getConfig().getInt("attendance.check-interval-ticks", 1200);
        return Math.max(200, value);
    }

public ZoneId getAttendanceZone() {
        String raw = plugin.getConfig().getString("attendance.timezone", "Asia/Ho_Chi_Minh");
        try {
            return ZoneId.of(raw);
        } catch (Exception e) {
            plugin.getLogger().warning("Invalid timezone '" + raw + "', using Asia/Ho_Chi_Minh.");
            return ZoneId.of("Asia/Ho_Chi_Minh");
        }
    }
}
