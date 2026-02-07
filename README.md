# BoosterRewards | Discord Booster Rewards

Reward your Discord server boosters with in-game perks. Link Minecraft accounts to Discord, automatically sync boost status, and execute custom commands when players boost or stop boosting.

## What It Does

- **Account Linking**: Players connect their Minecraft account to Discord using a 6-digit code
- **Automatic Sync**: Checks boost status every 5 minutes (configurable)
- **Custom Rewards**: Run any command when a player boosts (permissions, items, currency)
- **Revocation**: Automatically remove rewards when boost expires
- **Cache System**: In-memory caching to prevent database overload on large servers

## Installation

1. Download the latest JAR from [Releases](https://github.com/jan1k1/BoosterRewards/releases)
2. Place in your server's `plugins/` folder
3. Start the server to generate config files
4. Stop the server and configure:
   - `discord.yml` - Add your bot token and server ID
   - `config.yml` - Set up rewards and sync interval
   - `messages.yml` - Customize player messages
5. Start the server

## Discord Bot Setup

You need a Discord bot with the **Server Members Intent** enabled.

1. Go to https://discord.com/developers/applications
2. Create New Application → Bot tab → Reset Token
3. Enable "Server Members Intent" under Privileged Gateway Intents
4. Copy token to `discord.yml`
5. Use OAuth2 URL Generator to invite bot with:
   - Scopes: `bot`, `applications.commands`
   - Permissions: Send Messages, Embed Links

## Configuration

### Basic Reward Example

```yaml
rewards:
  booster:
    enabled: true
    on-boost:
      - "lp user %player% parent add booster"
      - "give %player% diamond 5"
    on-stop:
      - "lp user %player% parent remove booster"
```

### Linking Modes

Set `linking.mode` in `config.yml`:

- `DISCORD_TO_MINECRAFT` (default): Player runs `/link` in Discord, gets code, types `/link <code>` in-game
- `MINECRAFT_TO_DISCORD`: Player runs `/link` in-game, gets code, types `/link <code>` in Discord

## Commands

**In-Game:**
- `/link <code>` - Link your account
- `/logout` - Unlink your account
- `/Booster` - Admin panel (requires `BoosterRewards.admin`)

**Discord:**
- `/link` - Get a linking code
- `/unlink` - Remove account link
- `/status` - Check your boost status and rewards

## Performance

Designed for 1000+ player servers:
- All database operations run asynchronously
- Player data cached in memory
- Zero main-thread blocking
- HikariCP connection pooling

## Requirements

- Minecraft 1.21.4+ (Paper/Spigot)
- Java 21
- Discord bot with Server Members Intent

## Support

- Issues: [GitHub Issues](https://github.com/jan1k1/BoosterRewards/issues)
- Discord: [Join Support Server](https://discord.gg/38Ebj42e)

## License

MIT License - Free to use, modify, and distribute.

