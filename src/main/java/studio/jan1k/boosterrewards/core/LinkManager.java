package studio.jan1k.boosterrewards.core;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LinkManager {

    private final Map<String, String> codeToDiscordInfo = new ConcurrentHashMap<>();
    private final Map<String, String> discordIdToCode = new ConcurrentHashMap<>();
    private static final SecureRandom random = new SecureRandom();
    private static final String CHARS = "ABCDEFHJKLMNPQRSTUVWXYZ23456789";

    public String generateCode(String discordId, String username) {
        if (discordIdToCode.containsKey(discordId)) {
            return discordIdToCode.get(discordId);
        }

        String code = generateRandomCode();
        while (codeToDiscordInfo.containsKey(code)) {
            code = generateRandomCode();
        }

        codeToDiscordInfo.put(code, "DISCORD:" + discordId + ":" + username);
        discordIdToCode.put(discordId, code);
        return code;
    }

    public String generateMinecraftCode(java.util.UUID uuid, String username) {
        // Check if existing code (could map UUID -> Code if needed, but simple map
        // suffices)
        // For simplicity, just generate a new one or check existing
        // Better implementation: bidirectional map for UUID too.
        // But for now, just generate new one.

        String code = generateRandomCode();
        while (codeToDiscordInfo.containsKey(code)) {
            code = generateRandomCode();
        }

        codeToDiscordInfo.put(code, "MINECRAFT:" + uuid.toString() + ":" + username);
        return code;
    }

    public String getInfo(String code) {
        return codeToDiscordInfo.get(code);
    }

    // Deprecated but kept for compatibility during refactor
    public String getDiscordInfo(String code) {
        String info = codeToDiscordInfo.get(code);
        if (info != null && info.startsWith("DISCORD:")) {
            return info.substring(8); // Remove prefix
        }
        // Fallback for old codes if any (unlikely due to restart)
        return info;
    }

    public void invalidateCode(String code) {
        String info = codeToDiscordInfo.remove(code);
        if (info != null && info.startsWith("DISCORD:")) {
            String discordId = info.split(":")[1];
            discordIdToCode.remove(discordId);
        }
    }

    private String generateRandomCode() {
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}

