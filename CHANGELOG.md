# Changelog

## [0.0.2] - 2026-02-13

### Changed
- **Dynamic Reward Tiers**: Refactored `BoosterReward` and `RewardSyncTask` to handle reward tiers more dynamically using config keys rather than hardcoded logic.
- **Improved Performance**: Improved member lookup and boost count calculation using Java Streams.
- **Code Quality**: Fixed several linting warnings and optimized imports.

### Fixed
- **Cleanup**: Removed misplaced `.class` files from the project root.
- **Null Safety**: Added explicit null checks in synchronization tasks to prevent edge-case NPEs.
