package top.mores.intelligencearchive.common.event;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** 玩家已经发现一个情报对象的事实。 */
public record IntelDiscoveredEvent(UUID playerId, String intelId, Instant timestamp)
        implements ArchiveEvent {
    public IntelDiscoveredEvent {
        playerId = Objects.requireNonNull(playerId, "playerId 不能为 null");
        intelId = requireId(intelId, "intelId");
        timestamp = Objects.requireNonNull(timestamp, "timestamp 不能为 null");
    }

    private static String requireId(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " 不能为 null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " 不能为空");
        }
        return value;
    }
}
