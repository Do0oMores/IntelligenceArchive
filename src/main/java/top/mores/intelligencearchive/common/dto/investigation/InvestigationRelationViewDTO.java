package top.mores.intelligencearchive.common.dto.investigation;

import java.time.Instant;
import java.util.Objects;

/** 玩家主观关系的网络投影；没有 edgeId、sourceClueIds 或 World IntelEdge。 */
public record InvestigationRelationViewDTO(
        String sourceId,
        String targetId,
        String relationType,
        String confidence,
        Instant createdTime
) {
    public static final int MAX_ID_LENGTH = 128;
    public static final int MAX_ENUM_NAME_LENGTH = 64;

    public InvestigationRelationViewDTO {
        sourceId = InvestigationViewDtoValidation.requireText(sourceId, "sourceId", MAX_ID_LENGTH);
        targetId = InvestigationViewDtoValidation.requireText(targetId, "targetId", MAX_ID_LENGTH);
        relationType = InvestigationViewDtoValidation.requireText(
                relationType,
                "relationType",
                MAX_ENUM_NAME_LENGTH
        );
        confidence = InvestigationViewDtoValidation.requireText(
                confidence,
                "confidence",
                MAX_ENUM_NAME_LENGTH
        );
        createdTime = Objects.requireNonNull(createdTime, "createdTime 不能为 null");
    }
}
