# BoosterRewards Project Handover

## 1. Project Identity
*   **Name**: BoosterRewards
*   **Version**: 0.0.1
*   **Purpose**: Minecraft plugin (Spigot/Paper 1.21.1) to reward players for boosting a Discord server.
*   **Author**: jan1k

## 2. Technology Stack
*   **Language**: Java 21
*   **Build System**: Gradle 8.12
*   **Minecraft API**: Spigot 1.21.1-R0.1-SNAPSHOT
*   **Discord API**: JDA (Java Discord API)
    *   *Current Issue*: Attempting to use version `6.3.0` (or latest), but Gradle fails to resolve it or find the `Button` class.
*   **Database**: H2 (default, file-based) or MySQL/MariaDB (via HikariCP).
*   **Metrics**: bStats (ID: 29390).
*   **Utilities**: ShadowJar (for shading dependencies), Skidfuscator (configured but not active in build).

## 3. Core Architecture & Recent Changes (CRITICAL CONTEXT)

### A. Reward System (Refactored)
*   **Manager**: `src/main/java/studio/jan1k/boosterrewards/core/RewardManager.java`
    *   Orchestrates giving and revoking rewards.
    *   Runs commands defined in `config.yml` (`on-boost`, `on-stop`).
    *   Delegates item giving to `ItemRewardHandler`.
*   **Item Handler**: `src/main/java/studio/jan1k/boosterrewards/core/ItemRewardHandler.java`
    *   **New Class**: Extracts logic for giving `ItemStack` rewards.
    *   Reads from config paths like `rewards.booster.on-boost.items`.
*   **Support**: Now supports multiple tiers: `booster` (Tier 1) and `booster_2` (Tier 2/Nitro).

### B. Admin GUI (Refactored)
*   **Class**: `src/main/java/studio/jan1k/boosterrewards/gui/AdminGUI.java`
*   **Modes**:
    1.  `MAIN_MENU`: Buttons for "Edit Rewards", "Post Link Panel", "Reload Config".
    2.  `REWARD_SELECTOR`: Chooses between "Booster Tier 1" and "Booster Tier 2".
    3.  `EDITOR`: Inventory GUI to drag-and-drop items to save them as rewards.
*   **Features**:
    *   **Post Link Panel**: Sends a Discord Embed with a "Link Account" button to a configured channel.

### C. Discord Integration
*   **Class**: `src/main/java/studio/jan1k/boosterrewards/discord/DiscordBot.java`
*   **Features**:
    *   Slash commands (handled via `onSlashCommandInteraction`).
    *   Member update events (to detect boosting status).
    *   `postLinkPanel(String channelId)`: Posts the embed.
*   **Blocking Issue**: The import `net.dv8tion.jda.api.interactions.components.Button` (or `.components.buttons.Button`) is failing compilation with JDA 6.3.0.

### D. Database Schema (H2/MySQL)
*   **Table `booster_users`**:
    *   `uuid` (VARCHAR 36, PK): Minecraft UUID.
    *   `discord_id` (VARCHAR 20): Linked Discord ID.
    *   `boost_start` (BIGINT): Timestamp.
*   **Table `booster_boosters`**:
    *   `discord_id` (VARCHAR 20, PK).
    *   `uuid` (VARCHAR 36).
    *   `username` (VARCHAR 16).
    *   `is_active` (BOOLEAN).
*   **Table `booster_pending_rewards`**: Stores rewards for offline players.

## 4. Configuration Overview
*   **`config.yml`**:
    *   `rewards`: Defines `booster`, `booster_2`, etc.
    *   `sync`: Interval (300s), `verify-on-startup`, `remove-rewards-on-stop`.
    *   `linking`: `mode` (DISCORD_TO_MINECRAFT or MINECRAFT_TO_DISCORD), `code-expiry`, `allow-relink`.
    *   `features`: Toggles for `admin-gui`, `update-checker`, `metrics`, `auto-claim`, `announce-boosts`.
    *   `panel`: Settings for the Discord Link Panel (`channel-id`).
*   **`discord.yml`**: Bot token, Guild ID, Channel IDs, Messages.
*   **`plugin.yml`**: Commands and permissions.

## 5. Commands & Permissions
*   `/link [code]`: Link account.
*   `/unlink` / `/logout`: Unlink account.
*   `/claim`: Claim pending rewards.
*   `/boosterrewards` (`/br`, `/brp`): Main admin command.
*   `/forceunlink <player>` (`/forcelogout`): **New** admin command to force unlink.
*   `/setboosterreward`: **Legacy** command to open editor (now accessible via `/br admin`).
*   **Permission**: `boosterrewards.admin` (default: op).

## 6. Current Status & Next Steps (THE HANDOVER)
**Status**: The project code is feature-complete but **FAILING TO BUILD**.

### The Problem
*   User wants JDA 6.3.0 (or "newest").
*   Gradle fails to resolve `net.dv8tion:JDA:6.3.0` or `latest.release` correctly, or the resolved artifact is missing the `Button` class in the expected package.
*   **Error**: `cannot find symbol class Button`.

### Action Plan for Next Session
1.  **Fix JDA Dependency**:
    *   Investigate the correct repository for JDA 6.3.0 (if it exists). Current config has `mavenCentral`, `jitpack`, `m2.dv8tion.net`.
    *   If 6.3.0 is truly unavailable or broken, negotiate a stable version (e.g., `5.0.0-beta.24`) with the user, strictly proving why 6.3.0 fails.
    *   Check `deps_info.log` (generated in previous session) for resolution failure details.
2.  **Verify Build**: Run `./gradlew clean build` until successful.
3.  **Test New Features**:
    *   Deploy `BoosterRewards-0.0.1-Fat.jar` to a test server.
    *   Run `/br admin` -> "Post Link Panel".
    *   Run `/br admin` -> "Edit Rewards" -> Check "Tier 2" saving.
    *   Test `/forceunlink`.

This file contains **everything** needed to resume work immediately.
