package top.mores.intelligencearchive.common.discovery;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 描述“哪个外部行为请求授予情报”的纯领域值对象。
 *
 * <p>Source 不是触发器，也不会监听游戏行为；metadata 只允许小型字符串数据，避免把
 * Minecraft Entity、Forge/Bukkit Event 或外部系统对象带入 Archive 核心。</p>
 */
public record IntelDiscoverySource(
        String id,
        DiscoverySourceType type,
        String sourceReference,
        Map<String, String> metadata
) {
    public static final int MAX_METADATA_ENTRIES = 32;

    public IntelDiscoverySource {
        id = requireText(id, "id");
        type = Objects.requireNonNull(type, "type 不能为 null");
        sourceReference = requireText(sourceReference, "sourceReference");
        Objects.requireNonNull(metadata, "metadata 不能为 null");
        if (metadata.size() > MAX_METADATA_ENTRIES) {
            throw new IllegalArgumentException("metadata 条目不能超过 " + MAX_METADATA_ENTRIES);
        }
        LinkedHashMap<String, String> copy = new LinkedHashMap<>();
        metadata.forEach((key, value) -> copy.put(
                requireText(key, "metadata key"),
                Objects.requireNonNull(value, "metadata value 不能为 null")
        ));
        metadata = Map.copyOf(copy);
    }

    public IntelDiscoverySource(String id, DiscoverySourceType type, String sourceReference) {
        this(id, type, sourceReference, Map.of());
    }

    private static String requireText(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " 不能为 null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " 不能为空");
        }
        return value;
    }
}
