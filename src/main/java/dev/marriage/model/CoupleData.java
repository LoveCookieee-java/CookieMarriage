package dev.marriage.model;

import java.time.LocalDate;
import java.util.UUID;

public final class CoupleData {

    private final UUID player1Uuid;
    private final UUID player2Uuid;
    private final String player1Name;
    private final String player2Name;
    private final long marriedAtEpoch;          

private volatile int streak;
    private volatile LocalDate lastCheckinDate; 
    private volatile double lastKnownCombinedBalance;

    public CoupleData(
            UUID player1Uuid,
            UUID player2Uuid,
            String player1Name,
            String player2Name,
            long marriedAtEpoch,
            int streak,
            LocalDate lastCheckinDate,
            double lastKnownCombinedBalance) {

        this.player1Uuid = player1Uuid;
        this.player2Uuid = player2Uuid;
        this.player1Name = player1Name;
        this.player2Name = player2Name;
        this.marriedAtEpoch = marriedAtEpoch;
        this.streak = streak;
        this.lastCheckinDate = lastCheckinDate;
        this.lastKnownCombinedBalance = lastKnownCombinedBalance;
    }

public UUID getPlayer1Uuid() { return player1Uuid; }
    public UUID getPlayer2Uuid() { return player2Uuid; }
    public String getPlayer1Name() { return player1Name; }
    public String getPlayer2Name() { return player2Name; }
    public long getMarriedAtEpoch() { return marriedAtEpoch; }

public int getStreak() { return streak; }
    public void setStreak(int streak) { this.streak = streak; }

    public LocalDate getLastCheckinDate() { return lastCheckinDate; }
    public void setLastCheckinDate(LocalDate date) { this.lastCheckinDate = date; }

    public double getLastKnownCombinedBalance() { return lastKnownCombinedBalance; }
    public void setLastKnownCombinedBalance(double balance) { this.lastKnownCombinedBalance = balance; }

public boolean contains(UUID uuid) {
        return player1Uuid.equals(uuid) || player2Uuid.equals(uuid);
    }

public UUID getPartnerOf(UUID uuid) {
        if (player1Uuid.equals(uuid)) return player2Uuid;
        if (player2Uuid.equals(uuid)) return player1Uuid;
        return null;
    }

public String getPartnerNameOf(UUID uuid) {
        if (player1Uuid.equals(uuid)) return player2Name;
        if (player2Uuid.equals(uuid)) return player1Name;
        return null;
    }

public String getCoupleDisplayName() {
        return player1Name + " & " + player2Name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CoupleData other)) return false;
        return player1Uuid.equals(other.player1Uuid) && player2Uuid.equals(other.player2Uuid);
    }

    @Override
    public int hashCode() {
        return player1Uuid.hashCode() * 31 + player2Uuid.hashCode();
    }

    @Override
    public String toString() {
        return "CoupleData{" + player1Name + " & " + player2Name + ", streak=" + streak + "}";
    }
}
