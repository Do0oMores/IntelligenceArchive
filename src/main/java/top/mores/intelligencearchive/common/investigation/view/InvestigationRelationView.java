package top.mores.intelligencearchive.common.investigation.view;

import top.mores.intelligencearchive.common.casefile.state.InvestigationConfidence;
import top.mores.intelligencearchive.common.casefile.state.InvestigationRelationType;

import java.time.Instant;
import java.util.Objects;

/**
 * 玩家主观建立的关系投影。
 *
 * <p>来源必须是 PlayerInvestigationEdge；即使推理错误也会保留，绝不以世界 IntelEdge 校正。</p>
 */
public record InvestigationRelationView(
        String sourceIntelId,
        String targetIntelId,
        InvestigationRelationType relationType,
        InvestigationConfidence confidence,
        Instant createdTime
) {
    public InvestigationRelationView {
        sourceIntelId = InvestigationViewValidation.requireText(sourceIntelId, "sourceIntelId");
        targetIntelId = InvestigationViewValidation.requireText(targetIntelId, "targetIntelId");
        relationType = Objects.requireNonNull(relationType, "relationType 不能为 null");
        confidence = Objects.requireNonNull(confidence, "confidence 不能为 null");
        createdTime = Objects.requireNonNull(createdTime, "createdTime 不能为 null");
    }
}
