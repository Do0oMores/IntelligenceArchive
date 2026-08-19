package top.mores.intelligencearchive.common.casefile.state;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * 单个玩家针对单个 Case 的不可变认知快照。
 *
 * <p>状态只保存定义 ID、玩家关系和假设进度，不复制 Archive、IntelNode 或 Case 内容对象。</p>
 */
public record PlayerCaseInvestigationState(
        UUID playerId,
        String caseId,
        Set<String> discoveredEvidenceIds,
        Set<String> discoveredClueIds,
        List<PlayerInvestigationEdge> investigationEdges,
        Map<String, HypothesisStatus> hypothesisStatuses
) {
    public PlayerCaseInvestigationState {
        playerId = Objects.requireNonNull(playerId, "playerId 不能为 null");
        caseId = CaseStateValidation.requireId(caseId, "caseId");
        discoveredEvidenceIds = CaseStateValidation.immutableIds(discoveredEvidenceIds, "discoveredEvidenceIds");
        discoveredClueIds = CaseStateValidation.immutableIds(discoveredClueIds, "discoveredClueIds");
        investigationEdges = List.copyOf(Objects.requireNonNull(investigationEdges, "investigationEdges 不能为 null"));

        Objects.requireNonNull(hypothesisStatuses, "hypothesisStatuses 不能为 null");
        LinkedHashMap<String, HypothesisStatus> statusCopy = new LinkedHashMap<>();
        for (Map.Entry<String, HypothesisStatus> entry : hypothesisStatuses.entrySet()) {
            String hypothesisId = CaseStateValidation.requireId(entry.getKey(), "hypothesisStatuses key");
            HypothesisStatus status = Objects.requireNonNull(entry.getValue(), "hypothesisStatuses value 不能为 null");
            statusCopy.put(hypothesisId, status);
        }
        hypothesisStatuses = Collections.unmodifiableMap(statusCopy);
    }

    public HypothesisStatus hypothesisStatus(String hypothesisId) {
        return hypothesisStatuses.getOrDefault(hypothesisId, HypothesisStatus.UNTESTED);
    }

    public boolean containsEquivalentEdge(PlayerInvestigationEdge candidate) {
        return investigationEdges.stream().anyMatch(edge -> edge.semanticallyEquals(candidate));
    }
}
