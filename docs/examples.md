# Configuration Examples

Real-world configuration examples for common use cases.

## Basic Booster Rewards

Simple setup for giving LuckPerms group and items.

```yaml
rewards:
  booster:
    enabled: true
    permission: "boosterrewards.booster"
    on-boost:
      - "lp user %player% parent add booster"
      - "give %player% diamond 5"
      - "msg %player% &dThank you for boosting!"
    on-stop:
      - "lp user %player% parent remove booster"
```

## Multi-Tier Rewards

Different rewards for different boost levels.

```yaml
rewards:
  tier1_booster:
    enabled: true
    on-boost:
      - "lp user %player% parent add booster_tier1"
      - "give %player% diamond 5"
      - "eco give %player% 1000"
    on-stop:
      - "lp user %player% parent remove booster_tier1"
      
  tier2_premium:
    enabled: true
    on-boost:
      - "lp user %player% parent add booster_tier2"
      - "give %player% diamond 10"
      - "give %player% emerald 5"
      - "eco give %player% 2500"
    on-stop:
      - "lp user %player% parent remove booster_tier2"
      
  tier3_vip:
    enabled: true
    role-id: "123456789012345678"
    on-boost:
      - "lp user %player% parent add vip_booster"
      - "give %player% netherite_ingot 1"
      - "eco give %player% 5000"
    on-stop:
      - "lp user %player% parent remove vip_booster"
```

## Economy Integration

Rewards using popular economy plugins.

### Vault/Essentials Economy

```yaml
rewards:
  booster:
    enabled: true
    on-boost:
      - "eco give %player% 5000"
      - "msg %player% &a+$5000 for boosting!"
    on-stop:
      - "eco take %player% 5000"
```

### PlayerPoints

```yaml
rewards:
  booster:
    enabled: true
    on-boost:
      - "points give %player% 1000"
      - "msg %player% &a+1000 points for boosting!"
```

### TokenManager

```yaml
rewards:
  booster:
    enabled: true
    on-boost:
      - "tm give %player% 50"
      - "msg %player% &a+50 tokens for boosting!"
```

## Permission Groups

### LuckPerms

```yaml
rewards:
  booster:
    enabled: true
    on-boost:
      - "lp user %player% parent add booster"
      - "lp user %player% permission set essentials.fly true"
      - "lp user %player% meta addprefix 100 '&d[Booster] '"
    on-stop:
      - "lp user %player% parent remove booster"
      - "lp user %player% permission unset essentials.fly"
      - "lp user %player% meta removeprefix 100"
```

### PermissionsEx

```yaml
rewards:
  booster:
    enabled: true
    on-boost:
      - "pex user %player% group add booster"
      - "pex user %player% add essentials.fly"
    on-stop:
      - "pex user %player% group remove booster"
      - "pex user %player% remove essentials.fly"
```

## Custom Items

Give custom items with NBT data.

```yaml
rewards:
  booster:
    enabled: true
    on-boost:
      - "give %player% diamond_sword{Enchantments:[{id:sharpness,lvl:5},{id:unbreaking,lvl:3}],display:{Name:'{\"text\":\"Booster Sword\",\"color\":\"light_purple\"}'}} 1"
      - "give %player% elytra{Enchantments:[{id:unbreaking,lvl:3}]} 1"
```

## Kits and Crates

### DeluxeMenus Integration

```yaml
rewards:
  booster:
    enabled: true
    on-boost:
      - "dm open booster_menu %player%"
```

### CrateReloaded

```yaml
rewards:
  booster:
    enabled: true
    on-boost:
      - "crate give physical booster 1 %player%"
      - "msg %player% &dYou received a Booster Crate!"
```

### EssentialsX Kits

```yaml
rewards:
  booster:
    enabled: true
    on-boost:
      - "player: kit booster"
      - "msg %player% &aBooster kit claimed!"
```

## Broadcast Messages

Announce when players boost.

```yaml
rewards:
  booster:
    enabled: true
    on-boost:
      - "broadcast &8[&d&lBOOST&8] &7%player% &8(&f%discord_user%&8) &7just boosted the server!"
      - "lp user %player% parent add booster"
      - "give %player% diamond 5"
```

## Temporary Permissions

Give permissions that expire (requires LuckPerms).

```yaml
rewards:
  booster:
    enabled: true
    on-boost:
      - "lp user %player% parent add booster"
      - "lp user %player% permission settemp essentials.fly true 30d"
    on-stop:
      - "lp user %player% parent remove booster"
```

## Network Setup (BungeeCord/Velocity)

### Proxy Server

Install BoosterRewards on proxy server only.

**config.yml:**
```yaml
database:
  type: MYSQL
  host: "localhost"
  port: 3306
  database: "boosterrewards_network"
  username: "root"
  password: "password"

sync:
  interval: 300
  check-on-join: true
```

### Backend Servers

Use LuckPerms sync or run commands via BungeeCord.

```yaml
rewards:
  booster:
    enabled: true
    on-boost:
      - "lpb user %player% parent add booster"
      - "alertall &d%player% &7just boosted the Discord!"
```

## One-Time Rewards

Rewards that can only be claimed once.

```yaml
rewards:
  first_boost:
    enabled: true
    one-time: true
    on-boost:
      - "give %player% netherite_ingot 1"
      - "eco give %player% 10000"
      - "msg %player% &dFirst time boost bonus!"
      - "lp user %player% parent add booster"
    on-stop:
      - "lp user %player% parent remove booster"
```

## Cooldown Rewards

Prevent reward spam with cooldowns.

```yaml
rewards:
  booster:
    enabled: true
    cooldown: 86400
    on-boost:
      - "give %player% diamond 5"
      - "msg %player% &aDaily boost reward claimed!"
```

## Discord Role Assignment

Assign Discord roles when linked.

**discord.yml:**
```yaml
roles:
  linked-role-id: "123456789012345678"
  custom-booster-role-id: "987654321098765432"
```

**config.yml:**
```yaml
rewards:
  booster:
    enabled: true
    on-boost:
      - "lp user %player% parent add booster"
```

The plugin automatically assigns Discord roles configured in `discord.yml`.

## Performance Optimized (Large Servers)

Configuration for 1000+ player servers.

```yaml
database:
  type: MYSQL
  host: "localhost"
  port: 3306
  database: "boosterrewards"
  username: "booster_user"
  password: "secure_password"
  pool-size: 20
  timeout: 5000

sync:
  interval: 900
  remove-rewards-on-stop: true
  check-on-join: false
  verify-on-startup: false
  rank-mode: HIGHEST

features:
  debug-mode: false
  auto-claim: true
  announce-boosts: true
```

## Link Panel Setup

Permanent Discord embed for linking.

**config.yml:**
```yaml
panel:
  enabled: true
  channel-id: "123456789012345678"
  auto-update: true
  update-interval: 60
```

**messages.yml:**
```yaml
panel:
  embed:
    title: "🎮 Link Your Minecraft Account"
    description: "Click below to link and claim booster rewards!"
    color: "#E354FF"
  button:
    label: "🔗 Link Account"
    style: "PRIMARY"
```

## Custom Messages

Fully customized messages.

**messages.yml:**
```yaml
prefix: "&#E354FF&lBOOST &8» &7"

in-game:
  link:
    success: "&d&lSUCCESS! &7Linked to &f%discord_user%"
    invalid-code: "&c&lERROR! &7Invalid code"
    
discord:
  link:
    success-title: "✅ Account Linked!"
    success-description: "**Code:** %code%\n**Expires:** 5 minutes\n\nRun `/link %code%` in Minecraft"
```

## See Also

- [Configuration Reference](configuration.md) - All configuration options
- [Placeholders](placeholders.md) - Available placeholders
- [Commands](commands.md) - Command reference
