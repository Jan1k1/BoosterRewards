# API Documentation

Developer documentation for integrating with BoosterRewards.

## Overview

BoosterRewards provides an API for developers to integrate boost rewards into their own plugins.

## Maven Dependency

Add BoosterRewards as a dependency in your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.jan1k1</groupId>
        <artifactId>BoosterRewards</artifactId>
        <version>0.0.1</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

## Gradle Dependency

Add to your `build.gradle`:

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    compileOnly 'com.github.jan1k1:BoosterRewards:0.0.1'
}
```

## Plugin Dependency

Add to your `plugin.yml`:

```yaml
depend: [BoosterRewards]
```

Or for soft dependency:

```yaml
softdepend: [BoosterRewards]
```

## Getting the API

```java
import studio.jan1k.boosterrewards.BoosterReward;
import studio.jan1k.boosterrewards.api.BoosterRewardsAPI;

public class YourPlugin extends JavaPlugin {
    private BoosterRewardsAPI api;
    
    @Override
    public void onEnable() {
        if (getServer().getPluginManager().getPlugin("BoosterRewards") != null) {
            BoosterReward plugin = (BoosterReward) getServer().getPluginManager().getPlugin("BoosterRewards");
            this.api = plugin.getAPI();
        }
    }
}
```

## API Methods

### Check if Player is Linked

```java
UUID playerUUID = player.getUniqueId();
boolean isLinked = api.isLinked(playerUUID);

if (isLinked) {
    player.sendMessage("You are linked to Discord!");
}
```

### Get Discord ID

```java
UUID playerUUID = player.getUniqueId();
String discordId = api.getDiscordId(playerUUID);

if (discordId != null) {
    player.sendMessage("Your Discord ID: " + discordId);
}
```

### Check if Player is Boosting

```java
UUID playerUUID = player.getUniqueId();
boolean isBoosting = api.isBoosting(playerUUID);

if (isBoosting) {
    player.sendMessage("Thank you for boosting!");
}
```

### Get Boost Start Time

```java
UUID playerUUID = player.getUniqueId();
Long boostStart = api.getBoostStartTime(playerUUID);

if (boostStart != null) {
    long duration = System.currentTimeMillis() - boostStart;
    player.sendMessage("Boosting for: " + formatDuration(duration));
}
```

### Force Link Accounts

```java
UUID playerUUID = player.getUniqueId();
String discordId = "123456789012345678";

api.linkAccounts(playerUUID, discordId).thenAccept(success -> {
    if (success) {
        player.sendMessage("Accounts linked!");
    } else {
        player.sendMessage("Failed to link accounts");
    }
});
```

### Force Unlink Account

```java
UUID playerUUID = player.getUniqueId();

api.unlinkAccount(playerUUID).thenAccept(success -> {
    if (success) {
        player.sendMessage("Account unlinked!");
    }
});
```

## Events

BoosterRewards fires custom events that you can listen to.

### PlayerLinkEvent

Fired when a player links their account.

```java
import studio.jan1k.boosterrewards.events.PlayerLinkEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class YourListener implements Listener {
    
    @EventHandler
    public void onPlayerLink(PlayerLinkEvent event) {
        Player player = event.getPlayer();
        String discordId = event.getDiscordId();
        
        player.sendMessage("Welcome, linked player!");
    }
}
```

### PlayerUnlinkEvent

Fired when a player unlinks their account.

```java
import studio.jan1k.boosterrewards.events.PlayerUnlinkEvent;

@EventHandler
public void onPlayerUnlink(PlayerUnlinkEvent event) {
    Player player = event.getPlayer();
    player.sendMessage("You have been unlinked");
}
```

### BoostStartEvent

Fired when a linked player starts boosting.

```java
import studio.jan1k.boosterrewards.events.BoostStartEvent;

@EventHandler
public void onBoostStart(BoostStartEvent event) {
    Player player = event.getPlayer();
    String discordUser = event.getDiscordUsername();
    
    Bukkit.broadcastMessage(player.getName() + " just boosted the server!");
}
```

### BoostEndEvent

Fired when a player stops boosting.

```java
import studio.jan1k.boosterrewards.events.BoostEndEvent;

@EventHandler
public void onBoostEnd(BoostEndEvent event) {
    Player player = event.getPlayer();
    player.sendMessage("Your boost has expired");
}
```

### Cancellable Events

Some events can be cancelled:

```java
@EventHandler
public void onPlayerLink(PlayerLinkEvent event) {
    if (someCondition) {
        event.setCancelled(true);
        event.getPlayer().sendMessage("Linking is disabled");
    }
}
```

## Database Access

### Get Database Manager

```java
import studio.jan1k.boosterrewards.database.DatabaseManager;

DatabaseManager db = api.getDatabaseManager();
```

### Query Linked Players

```java
db.getAllLinkedPlayers().thenAccept(linkedPlayers -> {
    for (UUID uuid : linkedPlayers) {
        // Process each linked player
    }
});
```

### Custom Queries

```java
db.executeQuery("SELECT * FROM booster_users WHERE discord_id = ?", discordId)
    .thenAccept(resultSet -> {
        // Process results
    });
```

## Discord Bot Integration

### Get JDA Instance

```java
import net.dv8tion.jda.api.JDA;

JDA jda = api.getJDA();
Guild guild = jda.getGuildById(api.getGuildId());
```

### Send Discord Message

```java
String channelId = "123456789012345678";
TextChannel channel = jda.getTextChannelById(channelId);

if (channel != null) {
    channel.sendMessage("Message from plugin!").queue();
}
```

## Example Plugin

Complete example plugin that gives extra rewards to boosters:

```java
package com.example.boosterextras;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import studio.jan1k.boosterrewards.BoosterReward;
import studio.jan1k.boosterrewards.api.BoosterRewardsAPI;
import studio.jan1k.boosterrewards.events.BoostStartEvent;

public class BoosterExtras extends JavaPlugin implements Listener {
    
    private BoosterRewardsAPI api;
    
    @Override
    public void onEnable() {
        BoosterReward plugin = (BoosterReward) getServer().getPluginManager().getPlugin("BoosterRewards");
        if (plugin == null) {
            getLogger().severe("BoosterRewards not found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        this.api = plugin.getAPI();
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("BoosterExtras enabled!");
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        if (api.isBoosting(player.getUniqueId())) {
            player.sendMessage("§dWelcome back, booster!");
            player.setHealth(20.0);
            player.setFoodLevel(20);
        }
    }
    
    @EventHandler
    public void onBoostStart(BoostStartEvent event) {
        Player player = event.getPlayer();
        player.sendMessage("§d§lTHANK YOU FOR BOOSTING!");
        player.getInventory().addItem(new ItemStack(Material.DIAMOND, 10));
    }
}
```

## Database Schema

### Tables

**booster_users**
| Column | Type | Description |
|--------|------|-------------|
| `uuid` | VARCHAR(36) | Minecraft UUID (primary key) |
| `discord_id` | VARCHAR(20) | Discord user ID |
| `boost_start` | BIGINT | Timestamp when boost started |

**booster_boosters**
| Column | Type | Description |
|--------|------|-------------|
| `discord_id` | VARCHAR(20) | Discord user ID (primary key) |
| `uuid` | VARCHAR(36) | Minecraft UUID |
| `username` | VARCHAR(16) | Minecraft username |
| `is_active` | BOOLEAN | Whether boost is active |

**booster_pending_rewards**
| Column | Type | Description |
|--------|------|-------------|
| `id` | INT | Auto-increment ID |
| `uuid` | VARCHAR(36) | Minecraft UUID |
| `reward_type` | VARCHAR(50) | Type of reward |
| `timestamp` | BIGINT | When reward was queued |

## Best Practices

1. **Always check for null**
   ```java
   String discordId = api.getDiscordId(uuid);
   if (discordId != null) {
       // Use discordId
   }
   ```

2. **Use async methods**
   ```java
   api.linkAccounts(uuid, discordId).thenAccept(success -> {
       // Handle result asynchronously
   });
   ```

3. **Handle plugin reload**
   ```java
   @Override
   public void onDisable() {
       // Clean up resources
   }
   ```

4. **Check plugin enabled**
   ```java
   if (!api.isEnabled()) {
       return;
   }
   ```

## See Also

- [Configuration](configuration.md) - Configure BoosterRewards
- [Examples](examples.md) - Configuration examples
- [GitHub](https://github.com/jan1k1/BoosterRewards) - Source code
