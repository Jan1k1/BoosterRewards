# Troubleshooting

## Bot Connection Issues

### Bot Not Connecting

**Symptoms:**
- Console shows "Failed to connect to Discord"
- Bot appears offline in Discord
- No slash commands appear

**Solutions:**

1. **Verify Bot Token**
   - Check `discord.yml` for correct token
   - Token should start with `MTI...` or similar
   - No extra spaces or quotes around token
   
2. **Check Server Members Intent**
   - Go to [Discord Developer Portal](https://discord.com/developers/applications)
   - Select your application → Bot tab
   - Scroll to "Privileged Gateway Intents"
   - Enable "SERVER MEMBERS INTENT"
   - Click "Save Changes"
   
3. **Verify Guild ID**
   - Right-click your server in Discord
   - Click "Copy Server ID"
   - Paste into `discord.yml` under `guild-id`
   - Ensure Developer Mode is enabled in Discord settings

4. **Check Console for Errors**
   ```
   [BoosterRewards] Failed to login: The provided token is invalid!
   ```
   This means your token is wrong or expired. Reset it in the Developer Portal.

### Commands Not Appearing in Discord

**Symptoms:**
- Bot is online but `/link` doesn't show up
- Slash commands menu is empty

**Solutions:**

1. **Wait for Registration**
   - Commands can take up to 1 hour to appear
   - Restart Discord client to force refresh
   
2. **Check Command Configuration**
   - Verify `discord.yml` has commands enabled:
   ```yaml
   commands:
     link:
       enabled: true
   ```
   
3. **Verify Bot Permissions**
   - Bot needs `applications.commands` scope
   - Re-invite bot with correct permissions
   
4. **Check Bot Role Position**
   - Bot's role must be high enough in role hierarchy
   - Move bot role above other roles in Server Settings

## Reward Issues

### Rewards Not Giving

**Symptoms:**
- Player boosts but receives no rewards
- Console shows no errors
- `/status` shows boosting but no perks

**Solutions:**

1. **Enable Debug Mode**
   ```yaml
   features:
     debug-mode: true
   ```
   Restart server and check console for detailed logs.

2. **Test Commands Manually**
   - Run reward commands in console manually
   - Example: `lp user jan1k parent add booster`
   - If manual command fails, issue is with the command itself
   
3. **Check Reward Configuration**
   ```yaml
   rewards:
     booster:
       enabled: true  # Must be true
       on-boost:
         - "lp user %player% parent add booster"
   ```
   
4. **Verify Player is Linked**
   - Player must be linked to Discord
   - Check with `/status` in Discord
   
5. **Check Sync Settings**
   ```yaml
   sync:
     interval: 300
     check-on-join: true
   ```
   Lower interval for faster detection.

### Rewards Not Removed When Boost Expires

**Symptoms:**
- Player stops boosting but keeps rewards
- Permissions not revoked

**Solutions:**

1. **Check Configuration**
   ```yaml
   sync:
     remove-rewards-on-stop: true  # Must be true
   ```
   
2. **Verify on-stop Commands**
   ```yaml
   rewards:
     booster:
       on-stop:
         - "lp user %player% parent remove booster"
   ```
   Ensure commands are correct and will actually remove perks.

3. **Manual Sync**
   - Run `/boosterrewards admin` → Force Sync
   - Or restart server with `verify-on-startup: true`

## Linking Issues

### Link Codes Not Working

**Symptoms:**
- "Invalid or expired code" message
- Code doesn't link accounts

**Solutions:**

1. **Check Code Expiry**
   ```yaml
   linking:
     code-expiry: 300  # 5 minutes
   ```
   Codes expire quickly. Generate new code if expired.

2. **Verify Linking Mode**
   ```yaml
   linking:
     mode: DISCORD_TO_MINECRAFT
   ```
   - `DISCORD_TO_MINECRAFT`: Run `/link` in Discord first
   - `MINECRAFT_TO_DISCORD`: Run `/link` in Minecraft first

3. **Check for Typos**
   - Codes are case-sensitive
   - Use copy-paste instead of typing
   
4. **Verify Bot Permissions**
   - Bot needs permission to send DMs
   - Check user's privacy settings

### Cannot Unlink Account

**Symptoms:**
- `/unlink` says "not allowed"
- Cannot relink to different account

**Solution:**

```yaml
linking:
  allow-relink: true  # Must be true
```

If set to `false`, only admins can unlink using `/forceunlink <player>`.

### Already Linked Error

**Symptoms:**
- "You are already linked" when trying to link
- But player doesn't think they're linked

**Solutions:**

1. **Check Current Link**
   - Discord: `/status`
   - Shows current linked account
   
2. **Unlink First**
   - Discord: `/unlink`
   - Or Minecraft: `/unlink`
   
3. **Admin Force Unlink**
   - `/forceunlink <player>`
   - Requires `boosterrewards.admin` permission

## Database Issues

### Database Connection Failed

**Symptoms:**
- "Failed to connect to database" in console
- Plugin disables itself

**Solutions:**

**For H2 (default):**
1. Check file permissions in `plugins/BoosterRewards/database/`
2. Delete `boosterrewards.db` and restart (will reset data)

**For MySQL/MariaDB:**
1. **Verify Credentials**
   ```yaml
   database:
     type: MYSQL
     host: "localhost"
     port: 3306
     database: "boosterrewards"
     username: "root"
     password: "your_password"
   ```
   
2. **Test Connection**
   - Use MySQL client to connect manually
   - Ensure database exists: `CREATE DATABASE boosterrewards;`
   
3. **Check Firewall**
   - MySQL port (3306) must be open
   - Allow connections from Minecraft server IP

### Data Not Persisting

**Symptoms:**
- Links disappear after restart
- Rewards reset

**Solutions:**

1. **Check Database Type**
   ```yaml
   database:
     type: H2  # or MYSQL
   ```
   
2. **Verify Database File Exists**
   - H2: `plugins/BoosterRewards/database/boosterrewards.db`
   - Should persist between restarts
   
3. **Check Disk Space**
   - Ensure server has available disk space
   - Database writes may fail if disk is full

## Performance Issues

### Server Lag When Syncing

**Symptoms:**
- TPS drops every 5 minutes
- Lag spikes during sync

**Solutions:**

1. **Increase Sync Interval**
   ```yaml
   sync:
     interval: 600  # 10 minutes instead of 5
   ```
   
2. **Disable Check on Join**
   ```yaml
   sync:
     check-on-join: false
   ```
   
3. **Use MySQL Instead of H2**
   - H2 can be slow on large servers
   - MySQL handles concurrent operations better
   
4. **Increase Database Pool**
   ```yaml
   database:
     pool-size: 20  # Increase from 10
   ```

### High Discord API Usage

**Symptoms:**
- Rate limit warnings in console
- Bot becomes unresponsive

**Solutions:**

1. **Increase Sync Interval**
   ```yaml
   sync:
     interval: 900  # 15 minutes
   ```
   
2. **Disable Verify on Startup**
   ```yaml
   sync:
     verify-on-startup: false
   ```
   Only enable when needed.

## Common Error Messages

### "Cannot find symbol class Button"

**Issue:** Build error, not runtime error. JDA dependency issue.

**Solution:** This is a development issue. Ensure JDA 6.3.0 is properly configured in `build.gradle`.

### "Player not found"

**Issue:** Trying to give rewards to offline player.

**Solution:** Enable pending rewards:
```yaml
features:
  auto-claim: true
```
Rewards will be given when player joins.

### "Permission denied"

**Issue:** Bot lacks Discord permissions.

**Solution:**
1. Check bot role position in server settings
2. Ensure bot has "Send Messages" and "Embed Links" permissions
3. Re-invite bot with correct permissions

## Debug Mode

Enable detailed logging to diagnose issues:

```yaml
features:
  debug-mode: true
```

**What it logs:**
- All Discord API calls
- Database queries
- Sync operations
- Reward executions
- Link attempts

**Warning:** Very verbose. Only enable when troubleshooting.

## Getting Help

If you still have issues:

1. **Check Console Logs**
   - Look for error messages
   - Note any stack traces
   
2. **Enable Debug Mode**
   - Reproduce the issue
   - Copy relevant logs
   
3. **Create GitHub Issue**
   - [GitHub Issues](https://github.com/jan1k1/BoosterRewards/issues)
   - Include:
     - Plugin version
     - Server version (Paper/Spigot)
     - Java version
     - Error messages
     - Configuration (remove sensitive data)
   
4. **Join Discord**
   - [Support Server](https://discord.gg/38Ebj42e)
   - Faster community support

## See Also

- [Configuration](configuration.md) - Verify your configuration
- [Installation](installation.md) - Ensure proper setup
- [FAQ](faq.md) - Common questions
