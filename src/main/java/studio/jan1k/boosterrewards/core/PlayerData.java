package studio.jan1k.boosterrewards.core;

import java.util.UUID;

public class PlayerData {
    private final UUID uuid;
    private String discordId;
    private boolean boosting;
    private long lastCheck;

    public PlayerData(UUID uuid, String discordId) {
        this.uuid = uuid;
        this.discordId = discordId;
        this.boosting = false;
        this.lastCheck = 0;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getDiscordId() {
        return discordId;
    }

    public void setDiscordId(String discordId) {
        this.discordId = discordId;
    }

    public boolean isBoosting() {
        return boosting;
    }

    public void setBoosting(boolean boosting) {
        this.boosting = boosting;
    }

    public long getLastCheck() {
        return lastCheck;
    }

    public void setLastCheck(long lastCheck) {
        this.lastCheck = lastCheck;
    }
}

