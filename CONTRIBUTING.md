# Contributing

## Reporting Issues

If something doesn't work:

1. Check existing issues first
2. Include server version, Java version, and plugin version
3. Paste relevant errors from console
4. Describe what you expected vs. what happened

## Pull Requests

Features and fixes are welcome.

**Requirements:**
- Code must compile with Java 21
- Follow existing code style (no specific formatter, just match the pattern)
- Test on a real server before submitting
- Explain what changed and why

**What to avoid:**
- Breaking existing configs without migration path
- Adding dependencies without discussing first
- Removing features without justification

## Building

```bash
./gradlew build
```

JARs will be in `build/libs/`:
- `BoosterRewards-Fat.jar` - All dependencies bundled
- `BoosterRewards-Lite.jar` - Requires server to provide libraries

## Code Structure

- `src/main/java/studio/jan1k/boosterrewards/`
  - `commands/` - In-game commands
  - `core/` - Config, linking, rewards
  - `database/` - HikariCP + H2/MySQL
  - `discord/` - JDA bot integration
  - `gui/` - Admin GUI
  - `listeners/` - Player join/quit events
  - `tasks/` - Async reward sync

## Performance Guidelines

- All database calls must be async
- Use the PlayerData cache for online players
- Schedule Bukkit API calls on main thread
- Never block the main thread

## Questions

Ask in Issues or Discord before starting large changes.

