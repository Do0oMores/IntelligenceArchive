package top.mores.intelligencearchive.common.event;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** 玩家已经阅读一份档案的事实。 */
public record ArchiveReadEvent(UUID playerId, String documentId, Instant timestamp)
        implements ArchiveEvent {
    public ArchiveReadEvent {
        playerId = Objects.requireNonNull(playerId, "playerId 不能为 null");
        documentId = requireId(documentId);
        timestamp = Objects.requireNonNull(timestamp, "timestamp 不能为 null");
    }

    private static String requireId(String value) {
        Objects.requireNonNull(value, "documentId 不能为 null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("documentId 不能为空");
        }
        return value;
    }
}
