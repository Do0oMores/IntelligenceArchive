package top.mores.intelligencearchive.common.casefile.state;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;

/**
 * 玩家主动建立的认知关系。
 *
 * <p>它不是世界 IntelEdge；即使关系后来被推翻，也作为玩家推理历史继续存在。</p>
 */
public record PlayerInvestigationEdge(
        String edgeId,
        String sourceIntelNodeId,
        String targetIntelNodeId,
        InvestigationRelationType relationType,
        InvestigationConfidence confidence,
        Set<String> sourceClueIds,
        Instant createdAt
) {
    public PlayerInvestigationEdge {
        edgeId = CaseStateValidation.requireId(edgeId, "edgeId");
        sourceIntelNodeId = CaseStateValidation.requireId(sourceIntelNodeId, "sourceIntelNodeId");
        targetIntelNodeId = CaseStateValidation.requireId(targetIntelNodeId, "targetIntelNodeId");
        if (sourceIntelNodeId.equals(targetIntelNodeId)) {
            throw new IllegalArgumentException("调查关系的 source 与 target 不能相同");
        }
        relationType = Objects.requireNonNull(relationType, "relationType 不能为 null");
        confidence = Objects.requireNonNull(confidence, "confidence 不能为 null");
        sourceClueIds = CaseStateValidation.immutableIds(sourceClueIds, "sourceClueIds");
        createdAt = Objects.requireNonNull(createdAt, "createdAt 不能为 null");
    }

    /** edgeId 和时间不参与语义重复判断。 */
    public boolean semanticallyEquals(PlayerInvestigationEdge other) {
        return sourceIntelNodeId.equals(other.sourceIntelNodeId)
                && targetIntelNodeId.equals(other.targetIntelNodeId)
                && relationType == other.relationType
                && confidence == other.confidence
                && sourceClueIds.equals(other.sourceClueIds);
    }
}
