package top.mores.intelligencearchive.common.model.investigation;

import java.time.Instant;
import java.util.Objects;

/**
 * 玩家对一个世界情报 ID 的认知记录。
 *
 * <p>这里只保存 ID，不保存 {@code ArchiveDocument}、{@code IntelNode} 或 Minecraft 对象。
 * 世界资料可以独立更新、持久化或跨服传输，而玩家认知不会复制整份世界对象。</p>
 */
public record IntelDiscoveryRecord(
        String intelId,
        IntelDiscoveryStatus status,
        Instant discoveredTime,
        Instant updatedTime
) {
    public IntelDiscoveryRecord {
        Objects.requireNonNull(intelId, "intelId 不能为 null");
        if (intelId.isBlank()) {
            throw new IllegalArgumentException("intelId 不能为空");
        }

        status = Objects.requireNonNull(status, "status 不能为 null");
        if (status == IntelDiscoveryStatus.UNKNOWN) {
            throw new IllegalArgumentException("UNKNOWN 使用记录缺席表示，不应持久化为发现记录");
        }

        discoveredTime = Objects.requireNonNull(discoveredTime, "discoveredTime 不能为 null");
        updatedTime = Objects.requireNonNull(updatedTime, "updatedTime 不能为 null");
        if (updatedTime.isBefore(discoveredTime)) {
            throw new IllegalArgumentException("updatedTime 不能早于 discoveredTime");
        }
    }
}
