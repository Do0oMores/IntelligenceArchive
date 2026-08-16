package top.mores.intelligencearchive.common.model.investigation;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * 一个玩家当前拥有的调查认知快照。
 *
 * <p>世界中的 {@code ArchiveDocument} 和 {@code IntelNode} 描述客观存在的资料，
 * 本模型只描述特定玩家知道什么。二者分离后，同一世界中的不同玩家才能拥有不同认知。</p>
 *
 * <p>发现记录使用不可变 List 对外暴露，构造时会防御性复制且拒绝重复 ID；
 * 服务实现即使使用 Map 加速查询，也不会泄露可变内部容器。</p>
 */
public record PlayerInvestigationState(
        UUID playerId,
        List<IntelDiscoveryRecord> discoveredIntels
) {
    public PlayerInvestigationState {
        playerId = Objects.requireNonNull(playerId, "playerId 不能为 null");
        Objects.requireNonNull(discoveredIntels, "discoveredIntels 不能为 null");
        Set<String> uniqueIntelIds = new HashSet<>();
        for (IntelDiscoveryRecord record : discoveredIntels) {
            IntelDiscoveryRecord validRecord = Objects.requireNonNull(record, "discoveredIntels 不能包含 null");
            if (!uniqueIntelIds.add(validRecord.intelId())) {
                throw new IllegalArgumentException("discoveredIntels 不能包含重复 intelId: " + validRecord.intelId());
            }
        }
        discoveredIntels = List.copyOf(discoveredIntels);
    }

    /** 未保存记录就表示玩家完全不知道该情报。 */
    public IntelDiscoveryStatus statusOf(String intelId) {
        return findDiscovery(intelId)
                .map(IntelDiscoveryRecord::status)
                .orElse(IntelDiscoveryStatus.UNKNOWN);
    }

    public Optional<IntelDiscoveryRecord> findDiscovery(String intelId) {
        requireIntelId(intelId);
        return discoveredIntels.stream()
                .filter(record -> record.intelId().equals(intelId))
                .findFirst();
    }

    private static void requireIntelId(String intelId) {
        Objects.requireNonNull(intelId, "intelId 不能为 null");
        if (intelId.isBlank()) {
            throw new IllegalArgumentException("intelId 不能为空");
        }
    }
}
