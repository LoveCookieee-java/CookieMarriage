# CookieMarriage — Setup & Developer Guide

## Table of Contents

1. [Quick Start](#1-quick-start)
2. [Detailed Installation](#2-detailed-installation)
3. [Configuration Reference](#3-configuration-reference)
4. [Reward System Deep Dive](#4-reward-system-deep-dive)
5. [Streak & Attendance Mechanics](#5-streak--attendance-mechanics)
6. [Economy Integration](#6-economy-integration)
7. [Permissions & LuckPerms Setup](#7-permissions--luckperms-setup)
8. [Building from Source](#8-building-from-source)
9. [Troubleshooting](#9-troubleshooting)
10. [Architecture Overview](#10-architecture-overview)

---

## 1. Quick Start

```bash
# 1. Download the jar
# 2. Drop into plugins/
# 3. Install Vault + EssentialsX (or any economy plugin)
# 4. Restart server
# 5. Done!
```

**Minimum `server.properties` settings:**
```properties
# No special settings needed — just Paper 1.21+
```

---

## 2. Detailed Installation

### Step 1 — Prerequisites

Make sure these are already installed and working:

| Plugin | Where to get |
|--------|-------------|
| **Vault** | https://www.spigotmc.org/resources/vault.34315/ |
| **EssentialsX** (recommended economy) | https://essentialsx.net/downloads.html |

### Step 2 — Install CookieMarriage

```
server/
└── plugins/
    ├── Vault.jar
    ├── EssentialsX.jar
    └── CookieMarriage-v1.0.jar   ← place here
```

### Step 3 — First Boot

Start the server. You should see in console:
```
[CookieMarriage] Hooked into economy: Essentials Economy
[CookieMarriage] SQLite database initialised: marriage.db
[CookieMarriage] Loaded 0 couple(s) from database.
[CookieMarriage] AttendanceScheduler started (every 1200 ticks).
[CookieMarriage] Economy polling started (every 200 ticks).
[CookieMarriage] CookieMarriage vv1.0 enabled successfully.
```

If you see errors, check [Troubleshooting](#9-troubleshooting).

### Step 4 — Configure

Generated files in `plugins/CookieMarriage/`:
```
plugins/CookieMarriage/
├── config.yml      ← Economy, proposal, attendance settings
├── messages.yml    ← All player-facing messages
├── reward.yml      ← Streak milestone rewards
└── marriage.db     ← SQLite database (auto-created)
```

---

## 3. Configuration Reference

### `config.yml` — Full Reference

```yaml
economy:
  notify-transactions: true
  # true  = notify both partners when their combined balance changes
  # false = silent mode (no economy notifications)

  poll-interval-ticks: 200
  # Ticks between balance checks. 20 ticks = 1 second.
  # Default: 200 (10 seconds). Minimum enforced: 20 (1 second).
  # Increase to 400-600 if you have many couples and notice TPS drops.

  notify-threshold: 0.01
  # Minimum balance delta to send a notification.
  # Prevents spam from floating-point noise in economy plugins.
  # Example: set to 1.0 to only notify on changes of $1 or more.

  split-on-divorce: true
  # true  = automatically split combined balance 50/50 on /divorce
  # false = balances untouched on divorce

proposal:
  timeout-seconds: 60
  # Seconds before an unanswered proposal expires automatically.
  # Minimum enforced: 10 seconds.

  divorce-cooldown-days: 7
  # Cooldown time (in days) after a player divorces before they
  # can send or accept new proposals.
  # Default: 7 days. Set to 0 to disable.

attendance:
  required-minutes: 60
  # Minutes BOTH players must be online simultaneously in one day
  # for a streak check-in to be awarded.

  check-interval-ticks: 1200
  # How often the scheduler runs (ticks). Default: 1200 (60 seconds).
  # This is the co-online minute counter tick, not the actual day check.

  timezone: "Asia/Ho_Chi_Minh"
  # IANA timezone string for streak day boundaries.
  # Full list: https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
  # Examples:
  #   "Asia/Ho_Chi_Minh"    → UTC+7  (Vietnam)
  #   "America/New_York"    → EST/EDT
  #   "Europe/London"       → GMT/BST
  #   "UTC"                 → Universal
```

### `ring.yml` — Wedding Ring Configuration

Configure the items required to propose:

```yaml
require-ring: true
# Enable or disable the wedding ring requirement.
# true  = Players must have a valid ring in their inventory to propose.
# false = Marriage is free, no ring required.

ring-items:
# List of valid items that can be used as wedding rings.
# Proposers only need to hold one matching item from this list.
#
# Format:
#   - "VANILLA;<Material>"
#     Any vanilla Minecraft item (material names are case-insensitive).
#     Example: "VANILLA;DIAMOND"
#
#   - "MMOITEMS;<TYPE>;<ID>"
#     Items from MMOItems (type and id are case-insensitive).
#     Example: "MMOITEMS;RING;wedding_ring"
#
#   - "ITEMEDIT;<ID>"
#     Custom items from ItemEdit (id is case-insensitive).
#     Example: "ITEMEDIT;wedding_ring"
#
#   - "ITEMSADDER;<namespace:id>"
#     Custom items from ItemsAdder (id is case-insensitive).
#     Example: "ITEMSADDER;mypack:wedding_ring"
ring-items:
  - "VANILLA;DIAMOND"
```

### `messages.yml` — Customisation

All messages support `&`-prefixed color codes and the `%prefix%` placeholder.

```yaml
prefix: "&6[&eCookieMarriage&6] &r"

marry:
  proposal-sent: "%prefix%&aYou sent a proposal to &e%player%&a. Expires in &e%timeout%s&a."
  proposal-received: "%prefix%&e%player% &ahas proposed to you! Type &e/marry accept &aor &e/marry deny&a."
  proposal-expired-proposer: "%prefix%&cYour proposal to &e%player% &chas expired."
  proposal-pending: "%prefix%&cYou already have a pending proposal."
  already-married-self: "%prefix%&cYou are already married!"
  already-married-target: "%prefix%&e%player% &cis already married!"
  cannot-self-marry: "%prefix%&cYou cannot propose to yourself."
  proposer-offline: "%prefix%&cThe proposer has gone offline."
  no-proposal: "%prefix%&cYou have no incoming proposals."
  proposal-denied-denier: "%prefix%&cYou denied &e%player%'s &cproposal."
  proposal-denied-proposer: "%prefix%&e%player% &cdenied your proposal."
  success: "%prefix%&d❤ &e%player1% &dand &e%player2% &dare now married! &d❤"
  no-ring: "%prefix%&cYou need a &e&lWedding Ring &cin your inventory to propose!"
  ring-consumed: "%prefix%&aYour wedding ring has been given away. Congratulations! &6❤"
  cooldown-self: "%prefix%&cYou divorced recently. Please wait &e%time% &cto marry again!"
  cooldown-target: "%prefix%&e%player% &cdivorced recently. Please wait &e%time% &cbefore proposing!"

divorce:
  not-married: "%prefix%&cYou are not married."
  success: "%prefix%&7%player1% &aand &7%player2% &ahave divorced."
  each-received: "%prefix%&aYou received &e%amount% &afrom the shared balance."

economy:
  combined-balance: "%prefix%&aCombined balance (&e%couple%&a): &e%balance%"
  combined-increased: "%prefix%&a+%amount% to shared balance &7(total: %balance%)"
  combined-decreased: "%prefix%&c-%amount% from shared balance &7(total: %balance%)"

attendance:
  streak-achieved: "%prefix%&6✦ Streak &e%streak% &6day(s)! Great job &e%couple%&6!"
  streak-reset: "%prefix%&cStreak reset for &e%couple%&c. Log in together daily!"

marriage:
  info: |
    %prefix%&e--- &6%couple% &e---
    %prefix%&7Married: &e%married_date%
    %prefix%&7Streak: &e%streak% &7day(s)
    %prefix%&7Session today: &e%session% &7min(s)

general:
  not-married: "%prefix%&cYou are not married."
  no-permission: "%prefix%&cYou don't have permission."
  player-not-found: "%prefix%&cPlayer &e%player% &cnot found."
  usage-marry: "%prefix%&cUsage: /marry <player> | accept | deny"
  help: |
    %prefix%&e/marry <player> &7- Propose
    %prefix%&e/marry accept  &7- Accept proposal
    %prefix%&e/marry deny    &7- Deny proposal
    %prefix%&e/divorce       &7- Divorce
    %prefix%&e/marriage info &7- View stats
    %prefix%&e/marriage bal  &7- View balance
```

---

## 4. Reward System Deep Dive

### `reward.yml` Structure

```yaml
milestone:
  <streak_days>:
    - "command without slash"
    - "another command"
```

### Placeholder Expansion Rules

| Placeholder | Behaviour |
|-------------|-----------|
| `%couple%` | Command dispatched **TWICE**: once with player1's name, once with player2's name |
| `%player1%` | Replaced with player 1's exact in-game name (single dispatch) |
| `%player2%` | Replaced with player 2's exact in-game name (single dispatch) |
| `%streak%` | Current streak number |

### Full Example

```yaml
milestone:
  1:
    - "eco give %couple% 500"
    # Runs: "eco give Alice 500" then "eco give Bob 500"

  7:
    - "eco give %couple% 2000"
    - "broadcast &e%player1% &aand &e%player2% &ahave a 7-day streak!"
    # broadcast uses %player1%/%player2% — only dispatched once

  14:
    - "eco give %couple% 5000"
    - "lp user %couple% parent add vip"
    # lp command runs twice (grants vip to both)

  30:
    - "eco give %couple% 15000"

  100:
    - "eco give %couple% 100000"
    - "lp user %couple% parent add legend"
    - "broadcast &6🏆 &e%player1% &6and &e%player2% &6have a 100-day streak!"
```

### Important Notes

- Milestone commands run on the **console** (full server permissions)
- Commands only fire on the **exact** streak day, not cumulatively
- If the streak resets and is re-achieved, commands fire again
- Commands are dispatched synchronously on the main thread

---

## 5. Streak & Attendance Mechanics

### Day Definition

A "day" is defined by the midnight boundary in your configured `timezone` (default `Asia/Ho_Chi_Minh`, UTC+7).

- A player logged in at 23:50 VN time and one at 00:05 VN time are on **different** days.
- The streak counter resets at midnight VN time.

### Check-in Flow

```
Every 60 seconds (main thread):
  For each married couple:
    If BOTH players are online:
      sessionMinutes += 1
      If sessionMinutes >= required-minutes AND not checked-in today:
        streak += 1
        Save to DB
        Dispatch milestone rewards (if applicable)

At midnight (VN timezone):
  For each couple that did NOT check in yesterday:
    streak = 0
    Save to DB
    Notify online players
```

### Session Reset

The session minute counter (not the streak) resets when:
- Either player **logs out**
- Either player **logs in** (to prevent carrying over from previous sessions)

This means both players must be online *continuously* for the required duration.

---

## 6. Economy Integration

### How Balance Polling Works

CookieMarriage does **not** intercept Vault transactions. Instead:

1. Every `poll-interval-ticks`, it reads both players' Vault balances
2. Compares the sum with the last cached value
3. If the difference exceeds `notify-threshold`, both players are notified
4. The new balance is cached in SQLite

**Thread model:**
- Balance reads happen on an **async thread** (safe for most economy plugins)
- DB writes and player notifications are dispatched back to the **main thread**

### Divorce Balance Split

```
total = vault.getBalance(player1) + vault.getBalance(player2)
each  = total / 2

For each player:
  if they have more than "each" → withdraw (balance - each)
  if they have less than "each" → deposit (each - balance)
```

If a transaction fails (economy error), it is logged and the other player's balance is unchanged.

---

## 7. Permissions & LuckPerms Setup

### Default Setup (all players can marry)

No configuration needed — all three permissions default to `true`.

### Restricting Marriage (VIP only)

```bash
# Deny marriage for default group
lp group default permission set marriage.marry false
lp group default permission set marriage.divorce false

# Allow for VIP
lp group vip permission set marriage.marry true
lp group vip permission set marriage.divorce true
lp group vip permission set marriage.use true
```

---

## 8. Building from Source

### Prerequisites

- JDK 21+
- Git

### Steps

```bash
git clone https://github.com/LoveCookieee-java/CookieMarriage.git
cd CookieMarriage

# Windows
.\gradlew.bat build

# Linux / macOS (if gradlew is present)
./gradlew build
```

**Output:** `build/libs/CookieMarriage-v1.0.jar`

### SpotBugs Analysis

```bash
.\gradlew.bat spotbugsMain
# Opens: build/reports/spotbugs/main.html
```

### Gradle Tasks

| Task | Description |
|------|-------------|
| `build` | Compile, test (no-source), create shaded jar |
| `shadowJar` | Create fat jar with bundled SQLite JDBC |
| `spotbugsMain` | Static analysis report |
| `clean` | Remove `build/` directory |

---

## 9. Troubleshooting

### "No economy plugin is registered with Vault"

**Cause:** Vault is installed but no economy plugin is providing the service.

**Fix:** Install an economy plugin (EssentialsX recommended):
```
plugins/
├── Vault.jar
├── EssentialsX.jar   ← add this
└── CookieMarriage-v1.0.jar
```

### "Database initialisation failed"

**Cause:** Plugin cannot create or write to `plugins/CookieMarriage/`.

**Fix:** Check folder permissions:
```bash
chmod 755 plugins/CookieMarriage   # Linux
```

### Commands return "Missing message: ..."

**Cause:** `messages.yml` is missing a key after an update.

**Fix:** Delete `plugins/CookieMarriage/messages.yml` and restart — it will be regenerated from defaults.

### Streak not counting

**Checklist:**
- [ ] Both players are on the **same server** (not separate servers in a BungeeCord network)
- [ ] Both are **online at the same time** (not just logged in at different times)
- [ ] `required-minutes` has not been set extremely high in config
- [ ] Check `logs/latest.log` for attendance scheduler messages

### Economy notifications spamming

**Fix:** Increase `notify-threshold` in `config.yml`:
```yaml
economy:
  notify-threshold: 1.0   # Only notify on $1+ changes
```

---

## 10. Architecture Overview

```
MarriagePlugin (JavaPlugin)
│
├── onEnable() initialisation order:
│   1. Config files (ConfigManager, MessageConfig, RewardManager)
│   2. DatabaseManager → CoupleRepository (SQLite WAL mode)
│   3. Vault economy hook
│   4. MarriageService (loads couple cache from DB)
│   5. AttendanceService + EconomyService
│   6. Commands, Listeners, Schedulers
│
├── Service Layer
│   ├── MarriageService   — propose/accept/deny/divorce, couple cache (ConcurrentHashMap<UUID, CoupleData>)
│   ├── EconomyService    — async poll loop → main-thread notifications
│   └── AttendanceService — sync 60s tick, midnight reset, Vietnam timezone
│
├── Database Layer
│   ├── DatabaseManager   — single Connection, WAL+NORMAL sync, WAL checkpoint on close
│   └── CoupleRepository  — PreparedStatement CRUD (never String concatenation)
│
└── Memory Safety
    ├── No Player object references stored (UUID only)
    ├── PendingProposal holds BukkitTask ref → cancelled on resolve/quit
    ├── instance = null in onDisable()
    └── All repeating tasks stored and cancelled in onDisable()
```
