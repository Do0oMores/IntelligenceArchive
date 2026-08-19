package top.mores.intelligencearchive.common.service;

import top.mores.intelligencearchive.common.model.ArchiveDocument;
import top.mores.intelligencearchive.common.model.investigation.IntelDiscoveryStatus;
import top.mores.intelligencearchive.common.model.investigation.PlayerInvestigationState;

import java.util.Objects;

/**
 * Phase 5-A 的最小档案可见性规则。
 *
 * <p>只有服务端玩家状态中已存在发现记录的档案才可见；不解释等级、职业、任务或阵营。</p>
 */
public final class ArchiveVisibilityResolver {
    public ArchiveVisibility resolve(PlayerInvestigationState playerState, ArchiveDocument document) {
        Objects.requireNonNull(playerState, "playerState 不能为 null");
        Objects.requireNonNull(document, "document 不能为 null");
        return playerState.statusOf(document.id()) == IntelDiscoveryStatus.UNKNOWN
                ? ArchiveVisibility.HIDDEN
                : ArchiveVisibility.VISIBLE;
    }
}
