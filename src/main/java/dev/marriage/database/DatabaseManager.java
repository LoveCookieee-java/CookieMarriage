package dev.marriage.database;

import dev.marriage.MarriagePlugin;

import java.io.File;
import java.sql.*;
import java.util.logging.Level;

public final class DatabaseManager {

    private final MarriagePlugin plugin;
    private Connection connection;
    private CoupleRepository coupleRepository;

    public DatabaseManager(MarriagePlugin plugin) {
        this.plugin = plugin;
    }

public boolean initialize() {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists() && !dataFolder.mkdirs()) {
                plugin.getLogger().severe("Could not create plugin data folder!");
                return false;
            }

            File dbFile = new File(dataFolder, "marriage.db");

            // JDBC 4.0+ auto-registers drivers via ServiceLoader — no Class.forName() needed
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());

try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA journal_mode=WAL");
                stmt.execute("PRAGMA synchronous=NORMAL");
                stmt.execute("PRAGMA foreign_keys=ON");
                stmt.execute("PRAGMA cache_size=-8000"); 
            }

            createTables();

            coupleRepository = new CoupleRepository(plugin, this);

            plugin.getLogger().info("SQLite database initialised: " + dbFile.getName());
            return true;

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to open SQLite database", e);
        }
        return false;
    }

    private void createTables() throws SQLException {
        final String couples = """
                CREATE TABLE IF NOT EXISTS couples (
                    id                          INTEGER PRIMARY KEY AUTOINCREMENT,
                    player1_uuid                TEXT    NOT NULL,
                    player2_uuid                TEXT    NOT NULL,
                    player1_name                TEXT    NOT NULL,
                    player2_name                TEXT    NOT NULL,
                    married_at                  INTEGER NOT NULL,
                    streak                      INTEGER NOT NULL DEFAULT 0,
                    last_checkin_date           TEXT,
                    last_known_combined_balance REAL    NOT NULL DEFAULT 0.0
                )
                """;

final String idx1 = "CREATE UNIQUE INDEX IF NOT EXISTS idx_couples_p1 ON couples(player1_uuid)";
        final String idx2 = "CREATE UNIQUE INDEX IF NOT EXISTS idx_couples_p2 ON couples(player2_uuid)";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(couples);
            stmt.execute(idx1);
            stmt.execute(idx2);
        }
    }

public Connection getConnection() {
        return connection;
    }

public CoupleRepository getCoupleRepository() {
        return coupleRepository;
    }

public void close() {
        if (connection != null) {
            try {

                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("PRAGMA wal_checkpoint(TRUNCATE)");
                }
                connection.close();
                plugin.getLogger().info("Database connection closed.");
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Error closing database connection", e);
            } finally {
                connection = null;
            }
        }
    }
}
