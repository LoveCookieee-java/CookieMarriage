package dev.marriage.service;

import dev.marriage.MarriagePlugin;
import dev.marriage.model.CoupleData;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class EconomyService {

    private final MarriagePlugin plugin;
    private final Economy economy;

private final Set<CoupleData> trackedCouples = ConcurrentHashMap.newKeySet();

    private BukkitTask pollTask;

    public EconomyService(MarriagePlugin plugin, Economy economy) {
        this.plugin = plugin;
        this.economy = economy;
    }

public void startPolling() {
        if (!plugin.getConfigManager().isNotifyTransactions()) {
            plugin.getLogger().info("Economy transaction notifications are disabled.");
            return;
        }
        long interval = plugin.getConfigManager().getBalancePollIntervalTicks();
        pollTask = Bukkit.getScheduler().runTaskTimerAsynchronously(
                plugin, this::pollAllCouples, interval, interval);
        plugin.getLogger().info("Economy polling started (every " + interval + " ticks).");
    }

    public void stopPolling() {
        if (pollTask != null && !pollTask.isCancelled()) {
            pollTask.cancel();
        }
        pollTask = null;
    }

public void initializeCouple(CoupleData couple) {
        OfflinePlayer op1 = Bukkit.getOfflinePlayer(couple.getPlayer1Uuid());
        OfflinePlayer op2 = Bukkit.getOfflinePlayer(couple.getPlayer2Uuid());
        double combined = economy.getBalance(op1) + economy.getBalance(op2);
        couple.setLastKnownCombinedBalance(combined);
        trackedCouples.add(couple);
    }

public void removeCouple(CoupleData couple) {
        trackedCouples.remove(couple);
    }

public double getCombinedBalance(CoupleData couple) {
        OfflinePlayer op1 = Bukkit.getOfflinePlayer(couple.getPlayer1Uuid());
        OfflinePlayer op2 = Bukkit.getOfflinePlayer(couple.getPlayer2Uuid());
        return economy.getBalance(op1) + economy.getBalance(op2);
    }

private void pollAllCouples() {
        double threshold = plugin.getConfigManager().getNotifyThreshold();

        for (CoupleData couple : trackedCouples) {
            try {
                pollCouple(couple, threshold);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING,
                        "Error polling balance for couple " + couple.getCoupleDisplayName(), e);
            }
        }
    }

    private void pollCouple(CoupleData couple, double threshold) {

        boolean p1Online = Bukkit.getPlayer(couple.getPlayer1Uuid()) != null;
        boolean p2Online = Bukkit.getPlayer(couple.getPlayer2Uuid()) != null;
        if (!p1Online && !p2Online) return;

        OfflinePlayer op1 = Bukkit.getOfflinePlayer(couple.getPlayer1Uuid());
        OfflinePlayer op2 = Bukkit.getOfflinePlayer(couple.getPlayer2Uuid());

        double currentCombined = economy.getBalance(op1) + economy.getBalance(op2);
        double lastKnown       = couple.getLastKnownCombinedBalance();
        double delta           = currentCombined - lastKnown;

        if (Math.abs(delta) < threshold) return;  

couple.setLastKnownCombinedBalance(currentCombined);

final double finalCurrent = currentCombined;
        final double finalDelta   = delta;

        Bukkit.getScheduler().runTask(plugin, () -> {

            plugin.getDatabaseManager().getCoupleRepository()
                    .updateLastKnownBalance(couple.getPlayer1Uuid(), finalCurrent);

sendBalanceChangeNotification(couple, finalDelta, finalCurrent);
        });
    }

    private void sendBalanceChangeNotification(CoupleData couple, double delta, double total) {
        String key = delta > 0 ? "economy.combined-increased" : "economy.combined-decreased";
        String msg = plugin.getMsg(key,
                "%amount%",  formatMoney(Math.abs(delta)),
                "%balance%", formatMoney(total));

        UUID p1Uuid = couple.getPlayer1Uuid();
        UUID p2Uuid = couple.getPlayer2Uuid();

        Player p1 = Bukkit.getPlayer(p1Uuid);
        Player p2 = Bukkit.getPlayer(p2Uuid);

        if (p1 != null) p1.sendMessage(msg);
        if (p2 != null) p2.sendMessage(msg);
    }

    private static String formatMoney(double amount) {
        return String.format("%.2f", amount);
    }
}
