package dev.ungifts.data;
import java.util.UUID;
import java.util.Map;
public interface DataStorage {
    long getLastClaimTime(UUID uuid, String type);
    void setLastClaimTime(UUID uuid, String type, long time);
    void close();
    Map<String, Long> getPlayerData(UUID uuid);
    void setPlayerData(UUID uuid, Map<String, Long> data);
}