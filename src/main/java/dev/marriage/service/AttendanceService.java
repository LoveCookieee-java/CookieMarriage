package dev.marriage.service;

import dev.marriage.MarriagePlugin;
import dev.marriage.model.CoupleData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class AttendanceService {

    private final MarriagePlugin plugin;

private final Map<UUID, AtomicInteger> sessionMinutes = new ConcurrentHashMap<>();

private volatile LocalDate lastKnownDate;

    public AttendanceService(MarriagePlugin plugin) {
        this.plugin = plugin;
        this.lastKnownDate = LocalDate.now(getZone());
    }

public void initializeCouple(CoupleData couple) {
        sessionMinutes.put(couple.getPlayer1Uuid(), new AtomicInteger(0));
    }

public void removeCouple(CoupleData couple) {
        sessionMinutes.remove(couple.getPlayer1Uuid());
    }

public void resetSession(CoupleData couple) {
        AtomicInteger counter = sessionMinutes.get(couple.getPlayer1Uuid());
        if (counter != null) {
            counter.set(0);
        }
    }

public int getSessionMinutes(CoupleData couple) {
        AtomicInteger counter = sessionMinutes.get(couple.getPlayer1Uuid());
        return (counter != null) ? counter.get() : 0;
    }

public void tickMinute() {
        ZoneId zone = getZone();
        LocalDate today = LocalDate.now(zone);

if (!today.equals(lastKnownDate)) {
            processMidnightReset(lastKnownDate);
            lastKnownDate = today;
        }

for (CoupleData couple : plugin.getMarriageService().getAllCouples()) {
            tickCouple(couple, today);
        }
    }

    private void tickCouple(CoupleData couple, LocalDate today) {

        Player p1 = Bukkit.getPlayer(couple.getPlayer1Uuid());
        Player p2 = Bukkit.getPlayer(couple.getPlayer2Uuid());
        if (p1 == null || p2 == null) return;

        AtomicInteger counter = sessionMinutes.computeIfAbsent(
                couple.getPlayer1Uuid(), k -> new AtomicInteger(0));

        int minutes = counter.incrementAndGet();
        int required = plugin.getConfigManager().getRequiredMinutes();

if (minutes >= required && !today.equals(couple.getLastCheckinDate())) {
            processCheckin(couple, today, p1, p2);
        }
    }

private void processCheckin(CoupleData couple, LocalDate date, Player p1, Player p2) {
        int newStreak = couple.getStreak() + 1;
        couple.setStreak(newStreak);
        couple.setLastCheckinDate(date);

plugin.getDatabaseManager().getCoupleRepository()
                .updateStreak(couple.getPlayer1Uuid(), newStreak, date);

String msg = plugin.getMsg("attendance.streak-achieved",
                "%streak%", String.valueOf(newStreak),
                "%couple%", couple.getCoupleDisplayName());
        p1.sendMessage(msg);
        p2.sendMessage(msg);

plugin.getRewardManager().checkAndDispatch(couple);

        plugin.getLogger().info("Check-in: " + couple.getCoupleDisplayName()
                + " | streak=" + newStreak + " | date=" + date);
    }

private void processMidnightReset(LocalDate yesterday) {
        for (CoupleData couple : plugin.getMarriageService().getAllCouples()) {
            LocalDate lastCheckin = couple.getLastCheckinDate();

if (yesterday.equals(lastCheckin)) continue;

if (couple.getStreak() > 0) {
                plugin.getLogger().info("Streak reset: " + couple.getCoupleDisplayName()
                        + " (was " + couple.getStreak() + ", missed " + yesterday + ")");

                couple.setStreak(0);
                plugin.getDatabaseManager().getCoupleRepository()
                        .updateStreak(couple.getPlayer1Uuid(), 0, null);

String msg = plugin.getMsg("attendance.streak-reset",
                        "%couple%", couple.getCoupleDisplayName());
                Player p1 = Bukkit.getPlayer(couple.getPlayer1Uuid());
                Player p2 = Bukkit.getPlayer(couple.getPlayer2Uuid());
                if (p1 != null) p1.sendMessage(msg);
                if (p2 != null) p2.sendMessage(msg);
            }

resetSession(couple);
        }
    }

public void saveAll() {

    }

private ZoneId getZone() {
        return plugin.getConfigManager().getAttendanceZone();
    }
}
