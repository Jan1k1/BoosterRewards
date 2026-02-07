# Installation Guide

## Prerequisites

Before installing BoosterRewards, ensure you have:

- Minecraft server running Paper or Spigot 1.21.4+
- Java 21 installed
- Access to Discord Developer Portal
- Server restart capability

## Step 1: Create Discord Bot

### 1.1 Create Application

1. Go to [discord.com/developers/applications](https://discord.com/developers/applications)
2. Click **New Application** (top right)
3. Enter a name (e.g., "BoosterRewards Bot")
4. Click **Create**

### 1.2 Create Bot User

1. Click **Bot** in the left sidebar
2. Click **Reset Token**
3. Click **Yes, do it!** to confirm
4. Click **Copy** to copy your bot token
5. **Save this token** - you'll need it for configuration

### 1.3 Enable Required Intents

In the Bot settings page, scroll down to **Privileged Gateway Intents**:

1. Enable **SERVER MEMBERS INTENT** (required to check boost status)
2. Click **Save Changes**

### 1.4 Invite Bot to Server

1. Click **OAuth2** > **URL Generator** in the left sidebar
2. Under **Scopes**, select:
   - `bot`
   - `applications.commands`
3. Under **Bot Permissions**, select:
   - Send Messages
   - Embed Links
   - Manage Roles (optional, for Discord role assignment)
4. Copy the generated URL at the bottom
5. Open the URL in your browser
6. Select your Discord server
7. Click **Authorize**

## Step 2: Install Plugin

### 2.1 Download Plugin

1. Download the latest `BoosterRewards-X.X.X-Fat.jar` from [GitHub Releases](https://github.com/jan1k1/BoosterRewards/releases)
2. Place the JAR file in your server's `plugins/` folder

### 2.2 Generate Configuration Files

1. Start your Minecraft server
2. Wait for the server to fully load
3. Stop the server

The plugin will create the following files:
```
plugins/BoosterRewards/
├── config.yml
├── discord.yml
├── messages.yml
└── database/
    └── boosterrewards.db (if using H2)
```

## Step 3: Configure Discord Bot

Edit `plugins/BoosterRewards/discord.yml`:

### 3.1 Add Bot Token

```yaml
bot:
  token: "YOUR_BOT_TOKEN_HERE"
  guild-id: "000000000000000000"
```

Replace `YOUR_BOT_TOKEN_HERE` with the token from Step 1.2.

### 3.2 Get Guild ID

1. Open Discord
2. Go to **User Settings** > **Advanced**
3. Enable **Developer Mode**
4. Right-click your server icon
5. Click **Copy Server ID**
6. Paste the ID into `guild-id`

Example:
```yaml
bot:
  token: "MTIzNDU2Nzg5MDEyMzQ1Njc4.Gh1234.abcdefghijklmnopqrstuvwxyz1234567890"
  guild-id: "123456789012345678"
```

### 3.3 Optional: Configure Channels

To enable boost announcements and link logs:

1. Right-click the desired channel in Discord
2. Click **Copy Channel ID**
3. Paste into `discord.yml`:

```yaml
channels:
  boost-announcements: "123456789012345678"
  link-logs: "987654321098765432"
```

## Step 4: Configure Rewards

Edit `plugins/BoosterRewards/config.yml`:

### 4.1 Basic Reward Setup

```yaml
rewards:
  booster:
    enabled: true
    permission: "boosterrewards.booster"
    on-boost:
      - "lp user %player% parent add booster"
      - "give %player% diamond 5"
    on-stop:
      - "lp user %player% parent remove booster"
```

This example:
- Adds the "booster" LuckPerms group when player boosts
- Gives 5 diamonds
- Removes the group when boost expires

### 4.2 Database Configuration (Optional)

For small servers, the default H2 database is sufficient:

```yaml
database:
  type: H2
```

For networks or large servers, use MySQL:

```yaml
database:
  type: MYSQL
  host: "localhost"
  port: 3306
  database: "boosterrewards"
  username: "root"
  password: "your_password"
```

See [Configuration Reference](configuration.md) for all options.

## Step 5: Start Server

1. Start your Minecraft server
2. Check console for successful startup:

```
[BoosterRewards] Enabling BoosterRewards v0.0.1
[BoosterRewards] Connected to Discord guild: YourServerName
[BoosterRewards] Database initialized successfully
[BoosterRewards] Registered 3 Discord commands
```

If you see errors, see [Troubleshooting](troubleshooting.md).

## Step 6: Verify Installation

### 6.1 Test Discord Bot

In your Discord server, type `/` and you should see:
- `/link` - Link your Minecraft account to Discord
- `/unlink` - Unlink your Minecraft account
- `/status` - Check your boost status

### 6.2 Test In-Game Commands

Join your Minecraft server and run:
```
/boosterrewards
```

You should see the plugin help menu.

### 6.3 Test Linking

1. In Discord, run `/link`
2. Copy the 6-digit code
3. In Minecraft, run `/link <code>`
4. You should see a success message

## Next Steps

- [Configure rewards](configuration.md#rewards) for your server
- [Customize messages](configuration.md#messages) in `messages.yml`
- [Set up multi-tier rewards](examples.md#multi-tier-rewards)
- [Optimize performance](configuration.md#performance-tuning) for large servers

## Troubleshooting

If you encounter issues during installation:

- [Bot not connecting](troubleshooting.md#bot-not-connecting)
- [Commands not appearing](troubleshooting.md#commands-not-appearing)
- [Database errors](troubleshooting.md#database-errors)

See the full [Troubleshooting Guide](troubleshooting.md) for more help.
