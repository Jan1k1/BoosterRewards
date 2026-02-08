# BoosterRewards | Discord-to-Minecraft Reward Synchronization
BoosterRewards is an architecturally optimized system designed to automate guild booster incentives with surgical precision. It eliminates the friction of manual reward distribution by synchronizing Discord booster telemetry directly with the Minecraft server's internal permission and economy layers.
![banner](https://cdn.modrinth.com/data/cached_images/575c55f7c9290fcddc600660cca2c6acd1aab664.png)

**📚 Documentation:** [https://jan1k1.github.io/BoosterRewards/](https://jan1k1.github.io/BoosterRewards/)

## Architectural Integrity
Modern server environments require strict adherence to performance best practices. BoosterRewards was engineered to avoid the structural pathologies found in legacy reward systems, focusing instead on data persistence, asynchronous execution, and zero-thread blocking.

- **Main-Thread Decoupling:** Every interaction with the Discord API, JDA listeners, and the underlying database (H2 or MySQL) is strictly offloaded to dedicated worker threads. This ensures that the primary Minecraft server tick remains unaffected regardless of guild size or database I/O volume.
- **Predictive State Synchronization:** The system employs a real-time listening architecture that detects boost status changes instantaneously. Revocation logic is prioritized to ensure that non-boosting accounts lose access to perks either immediately or upon their next authentication event.
- **Optimized Persistence Layer:** An in-memory cache layer sits between the application and the database, minimizing redundant queries and ensuring rapid lookups for linked accounts.
- **Bi-Directional Account Linking:**
    - **Protocol A (Discord-to-MC):** Users initiate the handshake in Discord to receive a verification token for Minecraft.
    - **Protocol B (MC-to-Discord):** Users generate the verification sequence in Minecraft for completion within the Discord interface.

![discordlink](https://cdn.modrinth.com/data/cached_images/29bed1a359be50ffa92c5166ed4edb07eff343a2.png)

## Administration and Reward Definition
The system abstracts complex reward serialization through a direct-manipulation interface. Administrators can define item-based rewards without manual configuration file edits.

- **Administrative Commands:**
    - `/boosterrewards` - Root command for system management.
    - `/setboosterreward` - Primary interface for reward pool definition.
    - `/claim` - End-user reward distribution interface.
    - `/link` / `/unlink` - Identity mapping management.
- **Serialized Item Management:** The in-game editor supports arbitrary NBT data, enchantments, and custom metadata. Objects placed in the administrative container are automatically serialized and committed to the persistence layer.
- **Atomic Saving:** The system ensures that all inventory state changes are committed to disk upon container closure, preventing data loss or state mismatch.

# picture placeholder (Admin GUI Dashboard)
# picture placeholder (In-Game Item Reward Editor)

## Configuration Patterns

### Multi-Tiered PERK Structures
Define multiple reward hierarchies based on specific Discord roles or tenure.
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
    role-id: "9876543210" # Identifier for specific high-tier roles
    on-boost:
      - "lp user %player% parent add veteran_booster"
      - "give %player% netherite_ingot 1"
    on-stop:
      - "lp user %player% parent remove veteran_booster"
```

### Real-Time Cross-Platform Telemetry
Maintain community awareness with synchronized boost announcements.
```yaml
features:
  announce-boosts:
    minecraft: true
    discord: true
    message: "&d&l%player% &7just boosted the server! &5&l[BOOSTER+]"
```

# picture placeholder (Chat Announcement Preview)

## Global Variables and Placeholders
BoosterRewards provides comprehensive variable support for commands, localized messaging, and Discord state:

- `%player%` - Minecraft authenticated username
- `%uuid%` - Persistent player identifier
- `%discord_user%` - Discord account descriptor
- `%discord_id%` - Discord snowflake identifier
- `%boost_start%` - Verified timestamp of boost initiation
- `%boost_duration%` - Calculated latency since boost start

---
**Project Identifier:** BoosterRewards | Discord-to-Minecraft Reward Synchronization
**URL:** boosterrewards
**Metadata Categories:** Management, Social, Economy, Utility
**Indexing Keywords:** discord, boost, rewards, link, sync, paper, spigot, boosterrewards, automated infrastructure
