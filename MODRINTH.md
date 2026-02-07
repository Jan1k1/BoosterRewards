# BoosterRewards | Discord-to-Minecraft Reward

BoosterRewards is a performance-first tool designed to automate guild booster incentives. It eliminates the friction of manual reward distribution by syncing Discord booster status directly to your Minecraft server's permission and economy systems.

![banner](https://cdn.modrinth.com/data/cached_images/e3193c5809f25c14e5eac90c76fefb3c233d26bc_0.webp)

## Why BoosterRewards?

Most booster reward plugins are bloated or poorly optimized. BoosterRewards was built to handle high-traffic servers with a focus on data integrity, asynchronous execution, and zero main-thread impact.

*   **Zero-Thread Blocking:** All Discord API, JDA interactions, and database calls (H2/MySQL) run on dedicated threads. Your server TPS stays at 20.0 regardless of guild size.
*   **Smart Sync:** Automatically detects when a player stops boosting. Removes rewards immediately (if online) or queues the revocation for their next login.
*   **Cache-First Architecture:** Uses an optimized in-memory cache for linked players, reducing database I/O to the absolute minimum.
*   **Dual-Flow Linking:** 
    *   **Discord -> MC:** Run `/link` in Discord, get a code, type it in Minecraft.
    *   **MC -> Discord:** Run `/link` in Minecraft, get a code, type it in Discord.

![discordlink](https://cdn.modrinth.com/data/cached_images/29bed1a359be50ffa92c5166ed4edb07eff343a2.png)

## The Admin Panel (In-Game Editor)

Forget manual YAML editing for items. BoosterRewards includes a built-in GUI editor. Just drop items into the chest, and they are automatically serialized and added to the reward pool.

*   **Commands:** 
    *   `/BoosterRewards` (alias `/nrp`) - Main plugin command (includes `reload`, `help`, etc.).
    *   `/setboosterreward` - Opens the main admin dashboard.
    *   `/claim` - Opens the reward claim GUI for players.
    *   `/link`, `/unlink` - Account management.
*   **Item Editor:** Drag and drop any item (custom NBT, enchantments, renamed items) directly into the editor.
*   **Live Saving:** Closing the inventory automatically syncs the changes to your `config.yml`.

# picture placeholder (Admin GUI Dashboad)
# picture placeholder (In-Game Item Reward Editor)

## Advanced Configuration Examples

### Multi-Tiered Rewards
Reward your long-term boosters or specific roles with different perks.

```yaml
rewards:
  standard:
    enabled: true
    on-boost:
      - "lp user %player% parent add booster"
      - "eco give %player% 5000"
    on-stop:
      - "lp user %player% parent remove booster"
  
  veteran:
    enabled: true
    role-id: "9876543210" # Specific role ID for 3+ month boosters
    on-boost:
      - "lp user %player% parent add veteran_booster"
      - "give %player% netherite_ingot 1"
    on-stop:
      - "lp user %player% parent remove veteran_booster"
```

### Automated Announcements

Keep your community active by announcing boosts in real-time across both platforms.

```yaml
features:
  announce-boosts:
    minecraft: true
    discord: true
    message: "&d&l%player% &7just boosted the server! &5&l[Booster+]"
```

# picture placeholder (Chat Announcement Preview)

## Placeholders

BoosterRewards supports a wide range of placeholders for commands, messages, and Discord embeds:

*   `%player%` - Minecraft name
*   `%uuid%` - Player UUID
*   `%discord_user%` - Discord username
*   `%discord_id%` - Discord snowflake ID
*   `%boost_start%` - Date the boost began
*   `%boost_duration%` - Total days/hours boosted

## Installation & Setup

1.  Download `BoosterRewards-Fat-0.0.1.jar`.
2.  Drop it into your `plugins/` folder and start the server.
3.  Configure `discord.yml`:
    *   `token`: Your Discord Bot Token.
    *   `guild-id`: Your Discord Server ID.
4.  **Important:** Go to the Discord Developer Portal and enable **Server Members Intent**.
5.  Set your reward commands in `config.yml`.

---

**Project Name:** BoosterRewards | Discord-to-Minecraft Reward
**URL:** BoosterRewards
**Categories:** Management, Social, Economy, Utility
**Keywords:** discord, boost, rewards, link, sync, paper, spigot, Booster, automated perks

