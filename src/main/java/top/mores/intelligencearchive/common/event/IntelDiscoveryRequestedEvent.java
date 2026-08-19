package top.mores.intelligencearchive.common.event;

import top.mores.intelligencearchive.common.discovery.IntelDiscoverySource;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * 统一入口已收到一次有效 Intel/Archive 发现请求的事实。
 *
 * <p>它不是执行命令；实际状态变化仍由 DiscoverIntelUseCase 决定。重复发现也可以产生该事件，
 * 便于外围日志和统计区分“请求发生”与“新认知产生”。</p>
 */
public record IntelDiscoveryRequestedEvent(
        UUID playerId,
        String intelId,
        IntelDiscoverySource source,
        Instant timestamp
) implements ArchiveEvent {
    public IntelDiscoveryRequestedEvent {
        playerId = Objects.requireNonNull(playerId, "playerId 不能为 null");
        intelId = requireId(intelId);
        source = Objects.requireNonNull(source, "source 不能为 null");
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
