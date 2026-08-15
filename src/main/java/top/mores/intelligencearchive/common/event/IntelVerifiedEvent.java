package top.mores.intelligencearchive.common.event;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** 玩家已经验证一个情报对象的事实。 */
public record IntelVerifiedEvent(UUID playerId, String intelId, Instant timestamp)
        implements ArchiveEvent {
    public IntelVerifiedEvent {
        playerId = Objects.requireNonNull(playerId, "playerId 不能为 null");
        intelId = requireId(intelId);
        timestamp = Objects.requireNonNull(timestamp, "timestamp 不能为 null");
    }

    private static String requireId(String value) {
        Objects.requireNonNull(value, "intelId 不能为 null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("intelId 不能为空");
        }
        return value;
    }
}
