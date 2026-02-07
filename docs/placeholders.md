# Placeholders

Placeholders are variables that get replaced with actual values when used in commands and messages.

## Available Placeholders

| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%player%` | Minecraft username | `jan1k` |
| `%uuid%` | Player's Minecraft UUID | `069a79f4-44e9-4726-a5be-fca90e38aaf5` |
| `%discord_user%` | Discord username | `jan1k` |
| `%discord_id%` | Discord user ID (numeric) | `123456789012345678` |
| `%boost_start%` | Timestamp when boost started | `2026-02-07 22:30:00` |
| `%boost_duration%` | How long player has boosted | `3 days` |
| `%code%` | Link code (only in link messages) | `ABC123` |

## Usage in Commands

Placeholders can be used in reward commands defined in `config.yml`.

### Example: LuckPerms Integration

```yaml
rewards:
  booster:
    on-boost:
      - "lp user %player% parent add booster"
      - "lp user %player% meta set discord %discord_user%"
```

When `jan1k` (Discord: `jan1k#1234`) boosts, these commands execute:
```
lp user jan1k parent add booster
lp user jan1k meta set discord jan1k#1234
```

### Example: Economy Plugin

```yaml
rewards:
  booster:
    on-boost:
      - "eco give %player% 1000"
      - "msg %player% &aThanks for boosting, %discord_user%!"
```

### Example: Custom Commands

```yaml
rewards:
  booster:
    on-boost:
      - "player: warp boosterlounge"
      - "broadcast &d%player% &7just boosted the server!"
      - "give %player% diamond 5"
```

## Usage in Messages

Placeholders work in all messages defined in `messages.yml`.

### In-Game Messages

```yaml
in-game:
  link:
    success: "&aLinked to Discord user &f%discord_user%&a!"
  boost:
    started: "&a%player% started boosting! Rewards granted."
```

### Discord Messages

```yaml
discord:
  link:
    success-description: "Your code is: **%code%**\n\nType `/link %code%` in Minecraft."
  status:
    linked-description: "**Minecraft:** %player%\n**Boosting:** %boosting%\n**Since:** %boost_start%"
```

### Announcements

```yaml
discord:
  announcements:
    new-boost-description: "**%discord_user%** just boosted!\nLinked to: **%player%**"
```

## Context-Specific Placeholders

Some placeholders are only available in specific contexts:

| Placeholder | Available In |
|-------------|--------------|
| `%code%` | Link messages only |
| `%boost_start%` | When player is actively boosting |
| `%boost_duration%` | When player is actively boosting |

## Color Codes

While not placeholders, color codes can be used alongside placeholders:

### Minecraft Color Codes

```yaml
"&aGreen text with %player%"
"&c&lRed bold text"
"&#FF5733Hex color text"
```

### Discord Markdown

```yaml
"**Bold %player%**"
"*Italic %discord_user%*"
"__Underline__"
```

## Advanced Examples

### Multi-Line Messages

```yaml
discord:
  status:
    linked-description: |
      **Minecraft Account:** %player%
      **Discord Account:** %discord_user%
      **Boosting Since:** %boost_start%
      **Duration:** %boost_duration%
```

### Conditional Rewards

```yaml
rewards:
  booster:
    on-boost:
      - "lp user %player% parent add booster"
      - "msg %player% &dWelcome to the booster club, %discord_user%!"
      - "give %player% diamond 5"
  booster_2:
    on-boost:
      - "lp user %player% parent add premium_booster"
      - "msg %player% &5Double boost detected! Extra rewards for %discord_user%!"
      - "give %player% diamond 10"
```

### Broadcast Messages

```yaml
rewards:
  booster:
    on-boost:
      - "broadcast &8[&d&lBOOST&8] &7%player% &8(&f%discord_user%&8) &7just boosted the server!"
      - "lp user %player% parent add booster"
```

## Placeholder Formatting

### Timestamps

`%boost_start%` and `%boost_duration%` are automatically formatted:

- **boost_start:** `YYYY-MM-DD HH:mm:ss` (e.g., `2026-02-07 22:30:00`)
- **boost_duration:** Human-readable (e.g., `3 days`, `2 hours`, `45 minutes`)

### UUIDs

`%uuid%` is always in standard UUID format with dashes:
```
069a79f4-44e9-4726-a5be-fca90e38aaf5
```

## Common Use Cases

### 1. Welcome Message

```yaml
rewards:
  booster:
    on-boost:
      - "msg %player% &dThank you for boosting, %discord_user%!"
      - "msg %player% &7You've been granted the Booster rank!"
```

### 2. Logging

```yaml
rewards:
  booster:
    on-boost:
      - "console: log %player% (%uuid%) linked to %discord_user% (%discord_id%) started boosting"
```

### 3. Database Integration

```yaml
rewards:
  booster:
    on-boost:
      - "sql insert into boosters (uuid, discord_id, boost_start) values ('%uuid%', '%discord_id%', '%boost_start%')"
```

### 4. Custom Titles

```yaml
rewards:
  booster:
    on-boost:
      - "lp user %player% meta addprefix 100 '&d[Booster] '"
```

## See Also

- [Commands](commands.md) - Where to use placeholders in commands
- [Configuration](configuration.md#rewards) - Reward configuration
- [Examples](examples.md) - Real-world placeholder usage
