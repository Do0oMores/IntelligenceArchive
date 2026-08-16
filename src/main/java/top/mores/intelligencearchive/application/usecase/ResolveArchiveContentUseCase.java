package top.mores.intelligencearchive.application.usecase;

import top.mores.intelligencearchive.application.result.OperationStatus;
import top.mores.intelligencearchive.application.result.ResolveArchiveContentResult;
import top.mores.intelligencearchive.common.content.ArchiveContent;
import top.mores.intelligencearchive.common.content.resolution.ArchiveContentResolver;
import top.mores.intelligencearchive.common.content.resolution.PlayerContentContext;
import top.mores.intelligencearchive.common.content.resolution.ResolvedArchiveContent;
import top.mores.intelligencearchive.common.content.service.ArchiveContentService;
import top.mores.intelligencearchive.common.model.investigation.PlayerInvestigationState;
import top.mores.intelligencearchive.common.service.InvestigationService;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * 查询并解析玩家可见档案内容的应用入口。
 *
 * <p>UseCase 只编排世界档案、内容版本和玩家状态查询；可见性细节全部委托给 Resolver，
 * 不直接遍历节点，也不把条件判断交给未来 Renderer。</p>
 */
public final class ResolveArchiveContentUseCase {
    private final ArchiveContentService contentService;
    private final InvestigationService investigationService;
    private final ArchiveContentResolver contentResolver;

    public ResolveArchiveContentUseCase(
            ArchiveContentService contentService,
            InvestigationService investigationService,
            ArchiveContentResolver contentResolver
    ) {
        this.contentService = Objects.requireNonNull(contentService, "contentService 不能为 null");
        this.investigationService = Objects.requireNonNull(
                investigationService,
                "investigationService 不能为 null"
        );
        this.contentResolver = Objects.requireNonNull(contentResolver, "contentResolver 不能为 null");
    }

    public ResolveArchiveContentResult execute(UUID playerId, String documentId) {
        String resultDocumentId = documentId == null ? "" : documentId;
        if (playerId == null || documentId == null || documentId.isBlank()) {
            return failure(
                    OperationStatus.INVALID_INPUT,
                    resultDocumentId,
                    "Player ID and document ID are required."
            );
        }
        // 索引不是安全边界：即使客户端猜中 ID，详情请求仍必须重新检查玩家认知状态。
        if (!investigationService.hasDiscovered(playerId, documentId)) {
            return failure(
                    OperationStatus.ARCHIVE_NOT_VISIBLE,
                    documentId,
                    "The archive document is not visible."
            );
        }
        Optional<ArchiveContent> content = contentService.findByDocumentId(documentId);
        if (content.isEmpty()) {
            return failure(
                    OperationStatus.CONTENT_NOT_FOUND,
                    documentId,
                    "The archive document has no configured content."
            );
        }

        PlayerInvestigationState investigationState = investigationService.getPlayerState(playerId);
        ResolvedArchiveContent resolved = contentResolver.resolve(
                content.get(),
                new PlayerContentContext(playerId, investigationState)
        );
        return new ResolveArchiveContentResult(
                OperationStatus.SUCCESS,
                documentId,
                Optional.of(resolved),
                "Archive content resolved."
        );
    }

    private static ResolveArchiveContentResult failure(
            OperationStatus status,
            String documentId,
            String message
    ) {
        return new ResolveArchiveContentResult(status, documentId, Optional.empty(), message);
    }
}
