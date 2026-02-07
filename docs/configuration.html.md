# Configuration Guide

## File Structure

NitroRewards uses three config files:

- **config.yml** - Core plugin settings, rewards, sync timing
- **discord.yml** - Bot credentials, Discord channel/role IDs
- **messages.yml** - All player-facing text (customizable)

All files support color codes using `&` format.

## config.yml

### Placeholders

Available in reward commands and messages:

- `%player%` - Minecraft username
- `%uuid%` - Player UUID
- `%discord_user%` - Discord username
- `%discord_id%` - Discord user ID (numeric)
- `%boost_start%` - Timestamp when boost started
- `%boost_duration%` - How long player has boosted

### Database

```yaml
database:
  type: H2  # H2, MYSQL, or MARIADB
  host: "localhost"
  port: 3306
  database: "nitrorewards"
  username: "root"
  password: ""
  table-prefix: "nitro_"
  pool-size: 10
  timeout: 5000
```

**When to use MySQL:**
- Networks with multiple servers sharing data
- Servers with 500+ players
- When you need external database access

**H2 is fine for:**
- Single servers
- Under 500 players
- Simplicity (no external database setup)

### Rewards

```yaml
rewards:
  booster:
    enabled: true
    permission: "nitrorewards.booster"
    on-boost:
      - "lp user %player% parent add booster"
      - "give %player% diamond 5"
    on-stop:
      - "lp user %player% parent remove booster"
    cooldown: 0
    one-time: false
```

**Command Prefixes:**
- No prefix or `console:` = Run as console (default)
- `player:` = Run as the player

**Examples:**
```yaml
on-boost:
  - "lp user %player% parent add booster"           # Console
  - "console: eco give %player% 1000"                # Explicit console
  - "player: warp boosterzone"                        # Runs as player
```

**Multiple Reward Tiers:**
```yaml
rewards:
  booster:
    enabled: true
    # Standard booster rewards
    
  nitro:
    enabled: true
    role-id: "123456789"  # Specific role to check
    on-boost:
      - "lp user %player% parent add nitro"
```

### Sync Settings

```yaml
sync:
  interval: 300  # Check every 5 minutes (in seconds)
  remove-rewards-on-stop: true
  check-on-join: true
  verify-on-startup: true
  rank-mode: AGGREGATE  # AGGREGATE or HIGHEST
```

**interval:** How often to check all online players' boost status. Lower = more accurate but more API calls.

**remove-rewards-on-stop:** If false, rewards are permanent once given.

**check-on-join:** Recheck boost status when player logs in.

**verify-on-startup:** Sync all linked accounts when server starts.

**rank-mode:**
- `AGGREGATE` - Give all reward tiers they qualify for
- `HIGHEST` - Only give the highest tier

### Linking

```yaml
linking:
  mode: DISCORD_TO_MINECRAFT  # or MINECRAFT_TO_DISCORD
  code-expiry: 300
  code-length: 6
  allow-relink: false
  require-boost-to-link: false
```

**mode:**
- `DISCORD_TO_MINECRAFT`: Player runs `/link` in Discord, gets code, types `/link <code>` in Minecraft
- `MINECRAFT_TO_DISCORD`: Player runs `/link` in Minecraft, gets code, types `/link <code>` in Discord

**allow-relink:** If false, players can't unlink and relink. They're stuck.

**require-boost-to-link:** If true, only boosters can link accounts.

## discord.yml

### Bot Setup

```yaml
bot:
  token: "YOUR_BOT_TOKEN_HERE"
  guild-id: "000000000000000000"
```

**Getting your bot token:**
1. https://discord.com/developers/applications
2. New Application → Bot tab
3. Reset Token → Copy
4. Enable "Server Members Intent"

**Getting guild ID:**
1. Enable Developer Mode (Settings → Advanced → Developer Mode)
2. Right-click your server → Copy Server ID

### Roles & Channels

```yaml
roles:
  linked-role-id: "000000000000000000"
  custom-booster-role-id: "000000000000000000"

channels:
boost-announcements: "000000000000000000"
  link-logs: "000000000000000000"
```

All optional. Set to "000000000000000000" to disable.

### Commands

```yaml
commands:
  link:
    enabled: true
    description: "Link your Minecraft account to Discord"
  unlink:
    enabled: true
  status:
    enabled: true
```

## messages.yml

All player-facing text. Supports `&` color codes.

```yaml
link:
  usage: "&cUsage: /link <code>"
  success: "&aLinked to Discord user: %discord_user%"
  already-linked: "&cYou are already linked!"
  invalid-code: "&cInvalid or expired code."

logout:
  success: "&aYou have been unlinked."
  not-linked: "&cYou are not linked!"
```

## Performance Tuning

**For large servers (1000+ players):**
- Increase `database.pool-size` to 20
- Increase `sync.interval` to 600 (10 minutes)
- Use MySQL instead of H2
- Set `sync.check-on-join` to false

**For small servers (under 100):**
- Default settings are fine
- H2 database is sufficient

## Troubleshooting

**Bot not connecting:**
- Check token in discord.yml
- Verify "Server Members Intent" is enabled
- Make sure guild-id is correct

**Rewards not giving:**
- Check console for errors
- Verify Discord role ID is correct (if using specific roles)
- Test commands manually in console
- Enable `features.debug-mode` for verbose logs

**Link codes not working:**
- Codes expire after 5 minutes (configurable)
- Check `linking.mode` matches your workflow
- Verify bot has permissions to send messages
