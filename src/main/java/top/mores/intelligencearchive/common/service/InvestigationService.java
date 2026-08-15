package top.mores.intelligencearchive.common.service;

import top.mores.intelligencearchive.common.model.investigation.IntelDiscoveryRecord;
import top.mores.intelligencearchive.common.model.investigation.IntelDiscoveryStatus;
import top.mores.intelligencearchive.common.model.investigation.PlayerInvestigationState;

import java.util.Optional;
import java.util.UUID;

/**
 * 玩家调查认知的唯一修改入口。
 *
 * <p>{@link IntelService} 查询世界中客观存在的资料；本服务管理某个玩家是否已经发现、
 * 阅读或验证这些资料。保持两个服务独立可以避免把玩家状态写进共享 Document/Node。</p>
 */
public interface InvestigationService {
    PlayerInvestigationState getPlayerState(UUID playerId);

    IntelDiscoveryRecord discoverIntel(UUID playerId, String intelId);

    /**
     * 更新一条已经发现的记录。玩家尚未发现该 ID 时返回空，避免状态更新隐式授予情报。
     */
    Optional<IntelDiscoveryRecord> updateStatus(
            UUID playerId,
            String intelId,
            IntelDiscoveryStatus status
    );

    boolean hasDiscovered(UUID playerId, String intelId);
}
