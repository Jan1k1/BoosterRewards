# Commands & Permissions

## In-Game Commands

### Player Commands

#### `/link <code>`

Link your Minecraft account to Discord.

**Usage:**
```
/link ABC123
```

**Permission:** None (available to all players)

**Process:**
1. Run `/link` in Discord to get a code
2. Run `/link <code>` in Minecraft with the code
3. Accounts are now linked

**Note:** The linking flow depends on the `linking.mode` setting in `config.yml`. See [Configuration](configuration.md#linking).

---

#### `/unlink` (alias: `/logout`)

Unlink your Discord account and remove all booster rewards.

**Usage:**
```
/unlink
/logout
```

**Permission:** None (available to all players)

**Note:** Only works if `linking.allow-relink` is `true` in `config.yml`.

---

#### `/claim`

Claim pending booster rewards.

**Usage:**
```
/claim
```

**Permission:** None (available to all players)

**Note:** Only needed if `features.auto-claim` is `false` in `config.yml`. Otherwise, rewards are given automatically.

---

### Admin Commands

#### `/boosterrewards` (aliases: `/br`, `/brp`)

Main plugin command with subcommands.

**Usage:**
```
/boosterrewards
/boosterrewards reload
/boosterrewards help
/boosterrewards admin
```

**Permission:** `boosterrewards.admin` (default: op)

**Subcommands:**

| Subcommand | Description |
|------------|-------------|
| `reload` | Reload all configuration files |
| `help` | Show help menu |
| `admin` | Open admin GUI panel |
| `link` | Same as `/link` command |
| `unlink` | Same as `/unlink` command |
| `claim` | Same as `/claim` command |

---

#### `/forceunlink <player>` (alias: `/forcelogout`)

Force unlink another player's account (admin only).

**Usage:**
```
/forceunlink jan1k
/forcelogout jan1k
```

**Permission:** `boosterrewards.admin` (default: op)

**Use Cases:**
- Player linked wrong account
- Account needs to be reset
- Troubleshooting linking issues

---

#### `/setboosterreward` (alias: `/rewardsadmin`)

Open the reward editor GUI (legacy command).

**Usage:**
```
/setboosterreward
```

**Permission:** `boosterrewards.admin` (default: op)

**Note:** This is a legacy command. Use `/boosterrewards admin` instead.

---

## Discord Commands

All Discord commands are slash commands. Type `/` in Discord to see available commands.

### `/link`

Get a linking code to connect your Minecraft account.

**Usage:**
```
/link
```

**Process:**
1. Run `/link` in Discord
2. Bot sends you a 6-digit code
3. Run `/link <code>` in Minecraft
4. Accounts are now linked

**Note:** Code expires after 5 minutes (configurable in `config.yml`).

---

### `/unlink`

Unlink your Minecraft account.

**Usage:**
```
/unlink
```

**Effect:** Removes link between Discord and Minecraft accounts. All booster rewards are revoked.

---

### `/status`

Check your boost status and linked account.

**Usage:**
```
/status
```

**Shows:**
- Linked Minecraft username
- Current boost status
- Boost start date (if boosting)
- Active rewards

---

## Permissions

### Permission Nodes

| Permission | Default | Description |
|------------|---------|-------------|
| `boosterrewards.admin` | op | Access to all admin commands and GUI |
| `boosterrewards.booster` | false | Given to boosters (configured in rewards) |

### Custom Reward Permissions

You can define custom permissions in reward configurations:

```yaml
rewards:
  booster:
    permission: "boosterrewards.booster"
```

These permissions are automatically granted when a player boosts and removed when they stop boosting (if `sync.remove-rewards-on-stop` is `true`).

### Permission Plugin Integration

BoosterRewards works with any permission plugin that supports runtime permission changes:

- **LuckPerms** (recommended)
- **PermissionsEx**
- **GroupManager**
- **UltraPermissions**

**Example with LuckPerms:**

```yaml
rewards:
  booster:
    on-boost:
      - "lp user %player% parent add booster"
      - "lp user %player% permission set boosterrewards.booster true"
    on-stop:
      - "lp user %player% parent remove booster"
      - "lp user %player% permission unset boosterrewards.booster"
```

## Command Examples

### Player Workflow

1. **Link account:**
   ```
   Discord: /link
   Bot: "Your code is: ABC123"
   Minecraft: /link ABC123
   ```

2. **Check status:**
   ```
   Discord: /status
   ```

3. **Unlink (if needed):**
   ```
   Minecraft: /unlink
   ```

### Admin Workflow

1. **Reload configuration:**
   ```
   /boosterrewards reload
   ```

2. **Open admin panel:**
   ```
   /br admin
   ```

3. **Force unlink a player:**
   ```
   /forceunlink PlayerName
   ```

## See Also

- [Placeholders](placeholders.md) - Available placeholders for commands
- [Configuration](configuration.md) - Configure command behavior
- [Troubleshooting](troubleshooting.md) - Command-related issues
