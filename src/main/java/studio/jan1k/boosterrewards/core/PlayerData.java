package studio.jan1k.boosterrewards.core;

import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private final String discordId;
    private boolean isBoosting;
    private int boostCount;

    public PlayerData(UUID uuid, String discordId) {
        this.uuid = uuid;
        this.discordId = discordId;
        this.isBoosting = false;
        this.boostCount = 0;
    }

    public PlayerData(UUID uuid, String discordId, boolean isBoosting, int boostCount) {
        this.uuid = uuid;
        this.discordId = discordId;
        this.isBoosting = isBoosting;
        this.boostCount = boostCount;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getDiscordId() {
        return discordId;
    }

    public boolean isBoosting() {
        return isBoosting;
    }

    public void setBoosting(boolean boosting) {
        isBoosting = boosting;
    }

    public int getBoostCount() {
        return boostCount;
    }

    public void setBoostCount(int boostCount) {
        this.boostCount = boostCount;
    }
}
