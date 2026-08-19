package top.mores.intelligencearchive.common.casefile.service;

import top.mores.intelligencearchive.common.casefile.state.HypothesisStatus;
import top.mores.intelligencearchive.common.casefile.state.PlayerCaseInvestigationState;
import top.mores.intelligencearchive.common.casefile.state.PlayerInvestigationEdge;

import java.util.UUID;

/**
 * 玩家案件状态的存取边界。
 *
 * <p>实现只负责保存快照，不解析派生规则、不验证世界节点，也不判断假设。</p>
 */
public interface CaseInvestigationService {
    PlayerCaseInvestigationState getState(UUID playerId, String caseId);

    boolean discoverEvidence(UUID playerId, String caseId, String evidenceId);

    boolean discoverClue(UUID playerId, String caseId, String clueId);

    boolean addInvestigationEdge(UUID playerId, String caseId, PlayerInvestigationEdge edge);

    void updateHypothesisStatus(UUID playerId, String caseId, String hypothesisId, HypothesisStatus status);
}
