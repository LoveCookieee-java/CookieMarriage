package dev.marriage.database;

import dev.marriage.MarriagePlugin;
import dev.marriage.model.CoupleData;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

public final class CoupleRepository {

    private final MarriagePlugin plugin;
    private final DatabaseManager db;

    public CoupleRepository(MarriagePlugin plugin, DatabaseManager db) {
        this.plugin = plugin;
        this.db = db;
    }

public void save(CoupleData couple) {
        final String sql = """
                INSERT INTO couples
                    (player1_uuid, player2_uuid, player1_name, player2_name,
                     married_at, streak, last_checkin_date, last_known_combined_balance)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, couple.getPlayer1Uuid().toString());
            ps.setString(2, couple.getPlayer2Uuid().toString());
            ps.setString(3, couple.getPlayer1Name());
            ps.setString(4, couple.getPlayer2Name());
            ps.setLong(5, couple.getMarriedAtEpoch());
            ps.setInt(6, couple.getStreak());
            ps.setString(7, dateToString(couple.getLastCheckinDate()));
            ps.setDouble(8, couple.getLastKnownCombinedBalance());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save couple: " + couple, e);
        }
    }

public void deleteByPlayer(UUID playerUuid) {
        final String sql = "DELETE FROM couples WHERE player1_uuid = ? OR player2_uuid = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            String uuidStr = playerUuid.toString();
            ps.setString(1, uuidStr);
            ps.setString(2, uuidStr);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to delete couple for player: " + playerUuid, e);
        }
    }

public void updateStreak(UUID playerUuid, int streak, LocalDate lastCheckinDate) {
        final String sql = """
                UPDATE couples
                SET    streak = ?, last_checkin_date = ?
                WHERE  player1_uuid = ? OR player2_uuid = ?
                """;
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, streak);
            ps.setString(2, dateToString(lastCheckinDate));
            String uuidStr = playerUuid.toString();
            ps.setString(3, uuidStr);
            ps.setString(4, uuidStr);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to update streak for player: " + playerUuid, e);
        }
    }

public void updateLastKnownBalance(UUID playerUuid, double balance) {
        final String sql = """
                UPDATE couples
                SET    last_known_combined_balance = ?
                WHERE  player1_uuid = ? OR player2_uuid = ?
                """;
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setDouble(1, balance);
            String uuidStr = playerUuid.toString();
            ps.setString(2, uuidStr);
            ps.setString(3, uuidStr);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to update balance cache for player: " + playerUuid, e);
        }
    }

public Optional<CoupleData> findByPlayer(UUID playerUuid) {
        final String sql =
                "SELECT * FROM couples WHERE player1_uuid = ? OR player2_uuid = ? LIMIT 1";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            String uuidStr = playerUuid.toString();
            ps.setString(1, uuidStr);
            ps.setString(2, uuidStr);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to find couple for player: " + playerUuid, e);
        }
        return Optional.empty();
    }

public List<CoupleData> findAll() {
        final String sql = "SELECT * FROM couples";
        List<CoupleData> result = new ArrayList<>();
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load all couples", e);
        }
        return result;
    }

private CoupleData mapRow(ResultSet rs) throws SQLException {
        UUID p1 = UUID.fromString(rs.getString("player1_uuid"));
        UUID p2 = UUID.fromString(rs.getString("player2_uuid"));
        String p1Name = rs.getString("player1_name");
        String p2Name = rs.getString("player2_name");
        long marriedAt = rs.getLong("married_at");
        int streak = rs.getInt("streak");
        String dateStr = rs.getString("last_checkin_date");
        LocalDate lastCheckin = (dateStr != null && !dateStr.isBlank()) ? LocalDate.parse(dateStr) : null;
        double balance = rs.getDouble("last_known_combined_balance");

        return new CoupleData(p1, p2, p1Name, p2Name, marriedAt, streak, lastCheckin, balance);
    }

    private static String dateToString(LocalDate date) {
        return (date != null) ? date.toString() : null;
    }

    public void saveDivorceCooldown(UUID playerUuid, long cooldownUntil) {
        final String sql = "REPLACE INTO divorce_cooldowns (player_uuid, cooldown_until) VALUES (?, ?)";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            ps.setLong(2, cooldownUntil);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save divorce cooldown for player: " + playerUuid, e);
        }
    }

    public long getDivorceCooldown(UUID playerUuid) {
        final String sql = "SELECT cooldown_until FROM divorce_cooldowns WHERE player_uuid = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("cooldown_until");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get divorce cooldown for player: " + playerUuid, e);
        }
        return 0L;
    }

    public void removeDivorceCooldown(UUID playerUuid) {
        final String sql = "DELETE FROM divorce_cooldowns WHERE player_uuid = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to remove divorce cooldown for player: " + playerUuid, e);
        }
    }
}
