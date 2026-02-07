# BoosterRewards Documentation

**Version:** 0.0.1  
**Minecraft:** 1.21.4+ (Paper/Spigot)  
**Java:** 21+

## Overview

BoosterRewards is a Minecraft plugin that rewards Discord server boosters with in-game perks. Link Minecraft accounts to Discord, automatically sync boost status, and execute custom commands when players boost or stop boosting your server.

## Key Features

- **Account Linking** - Players connect Minecraft accounts to Discord using secure 6-digit codes
- **Automatic Sync** - Checks boost status every 5 minutes (configurable)
- **Custom Rewards** - Run any command when players boost (permissions, items, currency)
- **Automatic Revocation** - Remove rewards when boost expires
- **Multi-Tier Support** - Different rewards for different boost levels
- **Performance Optimized** - Async operations, in-memory caching, zero main-thread blocking
- **Database Flexibility** - H2 (embedded) or MySQL/MariaDB for networks
- **Admin GUI** - Visual interface for managing rewards and settings

## Requirements

- Minecraft 1.21.4+ (Paper or Spigot)
- Java 21
- Discord bot with Server Members Intent enabled

## Documentation

### Getting Started
- [Installation Guide](installation.md) - Step-by-step setup instructions
- [Configuration Reference](configuration.md) - Complete configuration guide
- [Examples](examples.md) - Real-world configuration examples

### Reference
- [Commands & Permissions](commands.md) - All commands and permission nodes
- [Placeholders](placeholders.md) - Available placeholders for commands and messages
- [Troubleshooting](troubleshooting.md) - Common issues and solutions
- [FAQ](faq.md) - Frequently asked questions

### Advanced
- [API Documentation](api.md) - Developer integration guide

## Quick Start

1. Create a Discord bot at [discord.com/developers](https://discord.com/developers/applications)
2. Download the plugin JAR and place in `plugins/` folder
3. Start server to generate config files
4. Configure `discord.yml` with bot token and server ID
5. Configure `config.yml` with desired rewards
6. Restart server

See the [Installation Guide](installation.md) for detailed instructions.

## Support

- **Issues:** [GitHub Issues](https://github.com/jan1k1/BoosterRewards/issues)
- **Discord:** [Support Server](https://discord.gg/38Ebj42e)
- **Documentation:** [jan1k1.github.io/BoosterRewards](https://jan1k1.github.io/BoosterRewards)

## License

MIT License - Free to use, modify, and distribute.
