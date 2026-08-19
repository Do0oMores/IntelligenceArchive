package top.mores.intelligencearchive.client.investigation.view;

import java.time.Instant;
import java.util.Objects;

/** 玩家主观关系的展示模型；即使关系错误也必须原样保留。 */
public record InvestigationRelationViewModel(
        String sourceId,
        String sourceName,
        String targetId,
        String targetName,
        String relationType,
        String confidence,
        Instant createdTime
) {
    public InvestigationRelationViewModel {
        sourceId = Objects.requireNonNull(sourceId, "sourceId 不能为 null");
        sourceName = Objects.requireNonNull(sourceName, "sourceName 不能为 null");
        targetId = Objects.requireNonNull(targetId, "targetId 不能为 null");
        targetName = Objects.requireNonNull(targetName, "targetName 不能为 null");
        relationType = Objects.requireNonNull(relationType, "relationType 不能为 null");
        confidence = Objects.requireNonNull(confidence, "confidence 不能为 null");
        createdTime = Objects.requireNonNull(createdTime, "createdTime 不能为 null");
    }
}
