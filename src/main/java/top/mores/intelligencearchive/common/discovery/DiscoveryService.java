package top.mores.intelligencearchive.common.discovery;

import java.util.UUID;

/**
 * GameCore、Quest、NPC 或世界系统调用 IntelligenceArchive 的唯一发现入口边界。
 *
 * <p>这些外部系统负责判断何时发生发现；本接口只验证目标并记录结果。</p>
 */
public interface DiscoveryService {
    /** IntelNode 与 ArchiveDocument 共用世界情报发现流程。 */
    DiscoveryResult discoverIntel(UUID playerId, String intelId, IntelDiscoverySource source);

    /** Evidence 的 Case 定位由外围适配器完成，Discovery API 不接收 caseId。 */
    DiscoveryResult discoverEvidence(UUID playerId, String evidenceId, IntelDiscoverySource source);
}
