package top.mores.intelligencearchive.application.result;

import java.util.Objects;

/**
 * 玩家建立情报关联入口的不可变结果。
 *
 * <p>Phase 2-D 成功仅代表输入和世界节点验证通过，不代表关系已经持久化。</p>
 */
public record LinkIntelResult(
        OperationStatus status,
        String sourceIntelId,
        String targetIntelId,
        String relationType,
        String message
) implements OperationResult {
    public LinkIntelResult {
        status = Objects.requireNonNull(status, "status 不能为 null");
        sourceIntelId = Objects.requireNonNull(sourceIntelId, "sourceIntelId 不能为 null");
        targetIntelId = Objects.requireNonNull(targetIntelId, "targetIntelId 不能为 null");
        relationType = Objects.requireNonNull(relationType, "relationType 不能为 null");
        message = Objects.requireNonNull(message, "message 不能为 null");
    }
}
