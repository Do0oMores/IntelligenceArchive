package top.mores.intelligencearchive.application.result;

import top.mores.intelligencearchive.common.casefile.state.HypothesisStatus;

import java.util.Objects;

/**
 * Hypothesis 显式评估结果。
 *
 * <p>结果只公开状态变化与稳定结果码，不包含尚缺的 Evidence、Clue 或隐藏答案。</p>
 */
public record EvaluateHypothesisResult(
        OperationStatus status,
        String caseId,
        String hypothesisId,
        HypothesisStatus oldStatus,
        HypothesisStatus newStatus,
        String message
) implements OperationResult {
    public EvaluateHypothesisResult {
        status = Objects.requireNonNull(status, "status 不能为 null");
        caseId = Objects.requireNonNull(caseId, "caseId 不能为 null");
        hypothesisId = Objects.requireNonNull(hypothesisId, "hypothesisId 不能为 null");
        oldStatus = Objects.requireNonNull(oldStatus, "oldStatus 不能为 null");
        newStatus = Objects.requireNonNull(newStatus, "newStatus 不能为 null");
        message = Objects.requireNonNull(message, "message 不能为 null");
    }
}
