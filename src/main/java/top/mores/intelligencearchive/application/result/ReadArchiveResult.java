package top.mores.intelligencearchive.application.result;

import top.mores.intelligencearchive.common.model.investigation.IntelDiscoveryStatus;

import java.util.Objects;

/** 玩家阅读档案用例的不可变结果。 */
public record ReadArchiveResult(
        OperationStatus status,
        String documentId,
        IntelDiscoveryStatus oldStatus,
        IntelDiscoveryStatus newStatus,
        String message
) implements OperationResult {
    public ReadArchiveResult {
        status = Objects.requireNonNull(status, "status 不能为 null");
        documentId = Objects.requireNonNull(documentId, "documentId 不能为 null");
        oldStatus = Objects.requireNonNull(oldStatus, "oldStatus 不能为 null");
        newStatus = Objects.requireNonNull(newStatus, "newStatus 不能为 null");
        message = Objects.requireNonNull(message, "message 不能为 null");
    }
}
