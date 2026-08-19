package top.mores.intelligencearchive.common.event;

import top.mores.intelligencearchive.common.discovery.IntelDiscoverySource;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Evidence 经外围适配器确认并写入玩家调查状态后的业务事实。 */
public record EvidenceDiscoveredEvent(
        UUID playerId,
        String evidenceId,
        IntelDiscoverySource source,
        List<String> newlyDerivedClueIds,
        Instant timestamp
) implements ArchiveEvent {
    public EvidenceDiscoveredEvent {
        playerId = Objects.requireNonNull(playerId, "playerId 不能为 null");
        evidenceId = requireId(evidenceId);
        source = Objects.requireNonNull(source, "source 不能为 null");
        newlyDerivedClueIds = List.copyOf(Objects.requireNonNull(
                newlyDerivedClueIds,
                "newlyDerivedClueIds 不能为 null"
        ));
        timestamp = Objects.requireNonNull(timestamp, "timestamp 不能为 null");
    }

    private static String requireId(String value) {
        Objects.requireNonNull(value, "evidenceId 不能为 null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("evidenceId 不能为空");
        }
        return value;
    }
}
