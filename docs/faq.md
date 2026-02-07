# Frequently Asked Questions

## General Questions

### What is BoosterRewards?

BoosterRewards is a Minecraft plugin that automatically rewards players who boost your Discord server. It links Minecraft accounts to Discord and gives configurable rewards when players boost.

### What Minecraft versions are supported?

Minecraft 1.21.4+ (Paper or Spigot). Java 21 is required.

### Is this plugin free?

Yes, BoosterRewards is open source under the MIT License. Free to use, modify, and distribute.

### Does it work on BungeeCord/Velocity?

Yes, install on the proxy server and use MySQL database for cross-server data sharing.

## Setup Questions

### Do I need a Discord bot?

Yes, you need to create a Discord bot and enable the "Server Members Intent". See [Installation Guide](installation.md#step-1-create-discord-bot).

### Can I use H2 database or do I need MySQL?

H2 (embedded) works fine for single servers under 500 players. Use MySQL/MariaDB for networks or larger servers.

### How do I get my Discord server ID?

1. Enable Developer Mode in Discord (Settings → Advanced → Developer Mode)
2. Right-click your server icon
3. Click "Copy Server ID"

### The bot is online but commands don't appear

Slash commands can take up to 1 hour to register. Restart Discord client to force refresh. Ensure bot has `applications.commands` scope.

## Linking Questions

### How do players link their accounts?

Default flow (DISCORD_TO_MINECRAFT):
1. Player runs `/link` in Discord
2. Bot gives them a 6-digit code
3. Player runs `/link <code>` in Minecraft

You can reverse this flow in `config.yml` with `linking.mode: MINECRAFT_TO_DISCORD`.

### How long are link codes valid?

5 minutes by default. Configurable in `config.yml`:
```yaml
linking:
  code-expiry: 300
```

### Can players unlink and relink?

Only if `linking.allow-relink` is `true` in `config.yml`. Otherwise, admins must use `/forceunlink <player>`.

### What if a player links the wrong account?

Admin can use `/forceunlink <player>` to reset the link, then player can link again.

## Rewards Questions

### What rewards can I give?

Any console command:
- LuckPerms groups
- Items via `/give`
- Economy money
- Teleports
- Custom plugin commands

See [Examples](examples.md) for configurations.

### Do rewards work offline?

If `features.auto-claim` is `true`, rewards are queued and given when player joins.

### Can I have different rewards for different boost tiers?

Yes, create multiple reward tiers in `config.yml`:
```yaml
rewards:
  tier1:
    # Basic rewards
  tier2:
    # Better rewards
```

### How often does it check for boosts?

Every 5 minutes by default (`sync.interval: 300`). Configurable in `config.yml`.

### Do rewards get removed when boost expires?

Only if `sync.remove-rewards-on-stop: true` in `config.yml`. You can make rewards permanent by setting this to `false`.

## Performance Questions

### Will this lag my server?

No. All Discord API calls and database operations run asynchronously. Zero main-thread blocking.

### How many players can it handle?

Tested on 1000+ player servers. Use MySQL and increase `sync.interval` for large servers. See [Configuration](configuration.md#performance-tuning).

### Does it use a lot of Discord API calls?

Depends on `sync.interval`. Default (300s) is ~12 calls per hour per online player. Increase interval to reduce API usage.

## Troubleshooting Questions

### Bot won't connect to Discord

1. Verify bot token in `discord.yml`
2. Enable "Server Members Intent" in Developer Portal
3. Check console for error messages

See [Troubleshooting](troubleshooting.md#bot-not-connecting).

### Rewards aren't being given

1. Enable `features.debug-mode: true`
2. Check console logs
3. Test commands manually in console
4. Verify player is linked with `/status` in Discord

See [Troubleshooting](troubleshooting.md#rewards-not-giving).

### Link codes don't work

1. Check code hasn't expired (5 minutes)
2. Verify `linking.mode` matches your workflow
3. Ensure no typos (codes are case-sensitive)

See [Troubleshooting](troubleshooting.md#link-codes-not-working).

## Feature Questions

### Can I customize messages?

Yes, all messages are in `messages.yml`. Supports color codes and placeholders.

### Can I announce when someone boosts?

Yes, set a channel in `discord.yml`:
```yaml
channels:
  boost-announcements: "123456789012345678"
```

And enable in `config.yml`:
```yaml
features:
  announce-boosts: true
```

### Is there an admin GUI?

Yes, run `/boosterrewards admin` (requires `boosterrewards.admin` permission).

### Can I create a permanent link panel in Discord?

Yes, enable in `config.yml`:
```yaml
panel:
  enabled: true
  channel-id: "123456789012345678"
```

### Does it support PlaceholderAPI?

Not currently, but you can use built-in placeholders in commands and messages. See [Placeholders](placeholders.md).

## Integration Questions

### Does it work with LuckPerms?

Yes, recommended permission plugin. Examples in [Configuration](examples.md#luckperms).

### Does it work with economy plugins?

Yes, any Vault-compatible economy plugin. Examples in [Configuration](examples.md#economy-integration).

### Can I use it with other plugins?

Yes, you can run any command in rewards. Works with most plugins.

### Is there an API for developers?

Yes, see [API Documentation](api.md).

## Network Questions

### Can I use this on a BungeeCord network?

Yes, install on the proxy server and use MySQL database.

### Do I need it on every backend server?

No, only on the proxy. Use LuckPerms or BungeeCord commands to sync permissions across servers.

### Can multiple servers share the same database?

Yes, use MySQL and configure all servers to use the same database.

## Security Questions

### Is my bot token safe?

Keep `discord.yml` secure. Never share your bot token. Use file permissions to restrict access.

### Can players abuse the linking system?

No, each Discord account can only link to one Minecraft account. Codes expire after 5 minutes.

### What data is stored?

Only:
- Minecraft UUID
- Discord user ID
- Boost start timestamp

No personal information is stored.

## Update Questions

### How do I update the plugin?

1. Download new JAR from [Releases](https://github.com/jan1k1/BoosterRewards/releases)
2. Stop server
3. Replace old JAR with new JAR
4. Start server

Configuration files are preserved.

### Will updates break my config?

No, configs are backward compatible. New options are added with defaults.

### How do I check for updates?

Enable in `config.yml`:
```yaml
features:
  update-checker: true
```

Console will notify you on startup if an update is available.

## Support Questions

### Where do I get help?

1. Check [Troubleshooting Guide](troubleshooting.md)
2. Read this FAQ
3. Create [GitHub Issue](https://github.com/jan1k1/BoosterRewards/issues)
4. Join [Discord Support Server](https://discord.gg/38Ebj42e)

### How do I report a bug?

Create a [GitHub Issue](https://github.com/jan1k1/BoosterRewards/issues) with:
- Plugin version
- Server version (Paper/Spigot)
- Java version
- Error messages from console
- Configuration (remove sensitive data)

### Can I request features?

Yes, create a [GitHub Issue](https://github.com/jan1k1/BoosterRewards/issues) with the "enhancement" label.

## See Also

- [Installation Guide](installation.md) - Setup instructions
- [Configuration Reference](configuration.md) - All configuration options
- [Troubleshooting](troubleshooting.md) - Common issues
- [Examples](examples.md) - Configuration examples
