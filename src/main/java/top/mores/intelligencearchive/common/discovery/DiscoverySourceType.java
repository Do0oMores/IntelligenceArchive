package top.mores.intelligencearchive.common.discovery;

/**
 * 外部发现来源的稳定分类。
 *
 * <p>枚举只描述来源语义，不绑定 NPC、物品、区域或事件系统的任何实现类。</p>
 */
public enum DiscoverySourceType {
    DIALOGUE,
    LOCATION,
    ITEM,
    DUNGEON,
    WORLD_EVENT,
    INVESTIGATION_INTERACTION,
    OTHER
}
