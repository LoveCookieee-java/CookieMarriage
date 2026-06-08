================================================================================
                                🍪 CookieMarriage
================================================================================

CookieMarriage is a Paper 1.21+ Minecraft plugin that lets players marry, share 
economy balances, and build attendance streaks together.

Supported Version: Paper 1.21+
Java Requirement: Java 21+
License: MIT License

--------------------------------------------------------------------------------
FEATURES
--------------------------------------------------------------------------------

- Marriage System: Propose, accept, or deny marriage between two players.
- Shared Economy: Married couples share a combined Vault balance with real-time
  change notifications.
- Divorce: Split the combined balance 50/50 on divorce.
- Daily Streak: Track how long couples play together each day (configured to
  Vietnam timezone Asia/Ho_Chi_Minh).
- Milestone Rewards: Configure commands to run automatically at streak milestones.
- SQLite Storage: Lightweight, zero-setup database (bundled JDBC driver).
- Memory Safe: No player reference leaks, proper task cleanup, WAL-mode SQLite.

--------------------------------------------------------------------------------
REQUIREMENTS
--------------------------------------------------------------------------------

- Paper: 1.21+ (Spigot is NOT supported)
- Vault: Any version (Economy bridge)
- Economy Plugin: Any Vault-compatible plugin (EssentialsX, CMI, etc.)
- Java: 21+ (Must match server JDK version)

--------------------------------------------------------------------------------
INSTALLATION
--------------------------------------------------------------------------------

1. Download CookieMarriage-v1.0.jar.
2. Place the JAR file in your server's "plugins/" folder.
3. Make sure Vault and an economy plugin are installed and running.
4. Restart your server.
5. Edit "plugins/CookieMarriage/config.yml" and "reward.yml" to your liking.
6. Use "/plugman reload CookieMarriage" or restart the server to apply changes.

--------------------------------------------------------------------------------
COMMANDS & PERMISSIONS
--------------------------------------------------------------------------------

* Commands:
  - /marry <player>    : Send a marriage proposal.
                         (Permission: marriage.marry)
  - /marry accept      : Accept an incoming proposal.
                         (Permission: marriage.marry)
  - /marry deny        : Deny an incoming proposal.
                         (Permission: marriage.marry)
  - /divorce           : Divorce your partner (splits balance 50/50).
                         (Permission: marriage.divorce)
  - /marriage balance  : View combined Vault balance.
                         (Permission: marriage.use)
  - /marriage info     : View streak, session time, and marriage date.
                         (Permission: marriage.use)
  - /marriage help     : Show help.
                         (Permission: marriage.use)

* Permissions Defaults:
  - marriage.marry    : true (defaults to all players)
  - marriage.divorce  : true (defaults to all players)
  - marriage.use      : true (defaults to all players)

--------------------------------------------------------------------------------
CONFIGURATION REFERENCE
--------------------------------------------------------------------------------

* config.yml:
------------------------------------------------------------
economy:
  notify-transactions: true       # Notify both players when balance changes
  poll-interval-ticks: 200        # How often to check balance (ticks, default 10s)
  notify-threshold: 0.01          # Minimum change to trigger notification
  split-on-divorce: true          # Split balance 50/50 on divorce

proposal:
  timeout-seconds: 60             # How long a proposal lasts before expiring

attendance:
  required-minutes: 60            # Minutes both must be online together per day
  check-interval-ticks: 1200      # Scheduler interval (ticks, default 60s)
  timezone: "Asia/Ho_Chi_Minh"    # Timezone for daily streak reset
------------------------------------------------------------

* reward.yml:
------------------------------------------------------------
milestone:
  1:
    - "eco give %couple% 500"
  7:
    - "eco give %couple% 2000"
    - "broadcast %player1% and %player2% have been together for 7 days!"
  30:
    - "eco give %couple% 10000"
  100:
    - "eco give %couple% 50000"
    - "lp user %couple% permission set vip.rank true"
------------------------------------------------------------

* Reward Placeholders:
  - %couple%    : Runs command twice — once per player.
  - %player1%   : Player 1's name.
  - %player2%   : Player 2's name.
  - %streak%    : Current streak count.

--------------------------------------------------------------------------------
BUILDING FROM SOURCE
--------------------------------------------------------------------------------

To build this plugin yourself:

1. Clone the repository:
   git clone https://github.com/LoveCookieee-java/CookieMarriage.git
2. Navigate to the folder:
   cd CookieMarriage
3. Run the Gradle build command:
   ./gradlew build
   (Output JAR is located in: build/libs/CookieMarriage-v1.0.jar)

4. Run SpotBugs static analysis:
   ./gradlew spotbugsMain
   (Report is located in: build/reports/spotbugs/main.html)

--------------------------------------------------------------------------------
PROJECT STRUCTURE
--------------------------------------------------------------------------------

src/main/java/dev/marriage/
├── MarriagePlugin.java          <- Plugin entry point
├── command/                     <- /marry, /divorce, /marriage commands
├── config/                      <- ConfigManager, MessageConfig
├── database/                    <- SQLite (DatabaseManager, CoupleRepository)
├── listener/                    <- PlayerJoin, PlayerQuit events
├── model/                       <- CoupleData, PendingProposal
├── reward/                      <- Milestone reward dispatcher
├── scheduler/                   <- Attendance ticker
└── service/                     <- Services (Marriage, Economy, Attendance)

--------------------------------------------------------------------------------
LICENSE
--------------------------------------------------------------------------------

This project is licensed under the MIT License.
================================================================================
