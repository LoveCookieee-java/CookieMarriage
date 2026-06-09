<div align="center">

<img src="https://img.shields.io/badge/Paper-1.21+-orange?style=for-the-badge&logo=minecraft" alt="Paper 1.21+"/>
<img src="https://img.shields.io/badge/Java-21-blue?style=for-the-badge&logo=openjdk" alt="Java 21"/>
<img src="https://img.shields.io/badge/Version-v1.0-brightgreen?style=for-the-badge" alt="v1.0"/>
<img src="https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge" alt="MIT"/>

# 🍪 CookieMarriage

**A Paper 1.21+ plugin that lets players marry, share economy balances, and build streaks together.**

[Features](#features) • [Installation](#installation) • [Commands](#commands) • [Configuration](#configuration) • [Permissions](#permissions)

</div>

---

## ✨ Features

- 💍 **Marriage System** — Propose, accept, or deny marriage between two players
- 👑 **Wedding Ring Requirement** — Require specific items (Vanilla, MMOItems, ItemEdit, ItemsAdder) to propose (consumed on marriage)
- ⏳ **Remarry Cooldown** — Configurable waiting period after divorcing before marrying again (default 7 days)
- 💰 **Shared Economy** — Married couples share a combined Vault balance with real-time change notifications
- 💔 **Divorce** — Split the combined balance 50/50 on divorce
- 📅 **Daily Streak** — Track how long couples play together each day (Vietnam timezone `Asia/Ho_Chi_Minh`)
- 🎁 **Milestone Rewards** — Configure commands to run automatically at streak milestones
- 🗄️ **SQLite Storage** — Lightweight, zero-setup database (bundled JDBC driver)
- 🔒 **Memory Safe** — No player reference leaks, proper task cleanup, WAL-mode SQLite

---

## 📋 Requirements

| Dependency | Version | Notes |
|-----------|---------|-------|
| **Paper** | 1.21+ | Spigot is **not** supported |
| **Vault** | Any | Economy bridge |
| **Economy Plugin** | Any Vault-compatible | EssentialsX, CMI, etc. |
| **Java** | 21+ | Must match server JDK |

---

## 📦 Installation

1. Download `CookieMarriage-v1.0.jar` from [Releases](../../releases)
2. Place it in your server's `plugins/` folder
3. Make sure **Vault** and an economy plugin are installed
4. Restart the server
5. Edit `plugins/CookieMarriage/config.yml` and `reward.yml` to your liking
6. Use `/plugman reload CookieMarriage` or restart again

---

## 🎮 Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/marry <player>` | Send a marriage proposal | `marriage.marry` |
| `/marry accept` | Accept an incoming proposal | `marriage.marry` |
| `/marry deny` | Deny an incoming proposal | `marriage.marry` |
| `/divorce` | Divorce your partner (splits balance 50/50) | `marriage.divorce` |
| `/marriage balance` | View combined Vault balance | `marriage.use` |
| `/marriage info` | View streak, session time, and marriage date | `marriage.use` |
| `/marriage help` | Show help | `marriage.use` |

---

## 🔑 Permissions

| Permission | Default | Description |
|-----------|---------|-------------|
| `marriage.player` | `true` | Parent permission for all player features (marry, divorce, use) |
| `marriage.marry` | `true` | Use `/marry` |
| `marriage.divorce` | `true` | Use `/divorce` |
| `marriage.use` | `true` | Use `/marriage` subcommands |

---

## ⚙️ Configuration

### `config.yml`

```yaml
economy:
  notify-transactions: true       # Notify both players when balance changes
  poll-interval-ticks: 200        # How often to check balance (ticks, default 10s)
  notify-threshold: 0.01          # Minimum change to trigger notification
  split-on-divorce: true          # Split balance 50/50 on divorce

proposal:
  timeout-seconds: 60             # How long a proposal lasts before expiring
  divorce-cooldown-days: 7        # Cooldown days before remarrying (default 7, 0 to disable)

attendance:
  required-minutes: 60            # Minutes both must be online together per day
  check-interval-ticks: 1200      # Scheduler interval (ticks, default 60s)
  timezone: "Asia/Ho_Chi_Minh"    # Timezone for daily streak reset

### `ring.yml`

Configure the items required to propose. Support soft-dependencies (no external compile dependencies needed):

```yaml
require-ring: true               # Require a ring to propose
ring-items:
  - "VANILLA;DIAMOND"            # Format: VANILLA;<Material>
  # - "MMOITEMS;RING;wedding_ring" # Format: MMOITEMS;<TYPE>;<ID>
  # - "ITEMEDIT;wedding_ring"      # Format: ITEMEDIT;<ID>
  # - "ITEMSADDER;mypack:wedding_ring" # Format: ITEMSADDER;<namespace:id>
```

### `reward.yml`

Configure commands to run at streak milestones. Use `%couple%` to run a command for **each** player individually:

```yaml
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
```

**Placeholders:**

| Placeholder | Expands to |
|-------------|-----------|
| `%couple%` | Runs command **twice** — once per player |
| `%player1%` | Player 1's name |
| `%player2%` | Player 2's name |
| `%streak%` | Current streak count |

---

## 🏗️ Building from Source

```bash
git clone https://github.com/LoveCookieee-java/CookieMarriage.git
cd CookieMarriage
./gradlew build
# Output: build/libs/CookieMarriage-v1.0.jar

# Run SpotBugs static analysis
./gradlew spotbugsMain
# Report: build/reports/spotbugs/main.html
```

---

## 📁 Project Structure

```
src/main/java/dev/marriage/
├── MarriagePlugin.java          ← Plugin entry point
├── command/                     ← /marry, /divorce, /marriage
├── config/                      ← ConfigManager, MessageConfig, RingConfig
├── database/                    ← SQLite (DatabaseManager, CoupleRepository)
├── item/                        ← RingChecker, RingItem
├── listener/                    ← PlayerJoin, PlayerQuit events
├── model/                       ← CoupleData, PendingProposal
├── reward/                      ← Milestone reward dispatcher
├── scheduler/                   ← Attendance ticker
└── service/                     ← MarriageService, EconomyService, AttendanceService
```

---

## 📄 License

This project is licensed under the **MIT License** — see [LICENSE](LICENSE) for details.
