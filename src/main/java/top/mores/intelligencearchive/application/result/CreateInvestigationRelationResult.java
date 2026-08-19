package top.mores.intelligencearchive.application.result;

import top.mores.intelligencearchive.common.casefile.state.PlayerInvestigationEdge;

import java.util.Objects;
import java.util.Optional;

/** 玩家关系创建结果；失败时不会构造半有效 Edge。 */
public record CreateInvestigationRelationResult(
        OperationStatus status,
        String caseId,
        Optional<PlayerInvestigationEdge> edge,
        String message
) implements OperationResult {
    public CreateInvestigationRelationResult {
        status = Objects.requireNonNull(status, "status 不能为 null");
        caseId = Objects.requireNonNull(caseId, "caseId 不能为 null");
        edge = Objects.requireNonNull(edge, "edge 不能为 null");
        message = Objects.requireNonNull(message, "message 不能为 null");
    }
}
