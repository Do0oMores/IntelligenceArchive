package top.mores.intelligencearchive.common.investigation.service;

import top.mores.intelligencearchive.common.investigation.view.PlayerInvestigationView;

import java.util.Optional;
import java.util.UUID;

/** 为未来调查终端提供经过服务端过滤的玩家认知快照。 */
public interface InvestigationViewService {
    /** Case 不存在或输入无效时返回 empty，不构造半有效 View。 */
    Optional<PlayerInvestigationView> buildView(UUID playerId, String caseId);
}
