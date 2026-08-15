package top.mores.intelligencearchive.application.result;

import top.mores.intelligencearchive.common.content.resolution.ResolvedArchiveContent;

import java.util.Objects;
import java.util.Optional;

/** ResolveArchiveContentUseCase 的不可变结果。 */
public record ResolveArchiveContentResult(
        OperationStatus status,
        String documentId,
        Optional<ResolvedArchiveContent> content,
        String message
) implements OperationResult {
    public ResolveArchiveContentResult {
        status = Objects.requireNonNull(status, "status 不能为 null");
        documentId = Objects.requireNonNull(documentId, "documentId 不能为 null");
        content = Objects.requireNonNull(content, "content 不能为 null");
        message = Objects.requireNonNull(message, "message 不能为 null");
        if (status == OperationStatus.SUCCESS && content.isEmpty()) {
            throw new IllegalArgumentException("成功结果必须包含 ResolvedArchiveContent");
        }
        if (status != OperationStatus.SUCCESS && content.isPresent()) {
            throw new IllegalArgumentException("失败结果不能包含 ResolvedArchiveContent");
        }
    }
}
