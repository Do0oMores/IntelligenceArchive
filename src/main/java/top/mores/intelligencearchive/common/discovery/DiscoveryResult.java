package top.mores.intelligencearchive.common.discovery;

import java.util.List;
import java.util.Objects;

/**
 * 外部系统可消费的统一发现结果。
 *
 * <p>结果只说明是否产生玩家新认知、是否改变调查状态以及新派生的 Clue ID；
 * 它不包含正确答案、世界真相、案件结局或奖励。</p>
 */
public record DiscoveryResult(
        DiscoveryStatus status,
        DiscoveryTargetType targetType,
        String targetId,
        IntelDiscoverySource source,
        boolean newlyDiscovered,
        List<String> newlyDerivedClueIds,
        boolean investigationStateChanged,
        String message
) {
    public DiscoveryResult {
        status = Objects.requireNonNull(status, "status 不能为 null");
        targetType = Objects.requireNonNull(targetType, "targetType 不能为 null");
        targetId = Objects.requireNonNull(targetId, "targetId 不能为 null");
        source = Objects.requireNonNull(source, "source 不能为 null");
        newlyDerivedClueIds = List.copyOf(Objects.requireNonNull(
                newlyDerivedClueIds,
                "newlyDerivedClueIds 不能为 null"
        ));
        message = Objects.requireNonNull(message, "message 不能为 null");
        if (status != DiscoveryStatus.SUCCESS
                && (newlyDiscovered || investigationStateChanged || !newlyDerivedClueIds.isEmpty())) {
            throw new IllegalArgumentException("非成功结果不能声明调查状态发生变化");
        }
    }

    public boolean producedNewClue() {
        return !newlyDerivedClueIds.isEmpty();
    }
}
