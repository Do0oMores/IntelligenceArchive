package top.mores.intelligencearchive.server.service;

import top.mores.intelligencearchive.common.content.service.ArchiveContentService;
import top.mores.intelligencearchive.common.model.ArchiveDocument;
import top.mores.intelligencearchive.common.model.IntelNavigationResult;
import top.mores.intelligencearchive.common.model.IntelNavigationTargetType;
import top.mores.intelligencearchive.common.model.IntelNode;
import top.mores.intelligencearchive.common.service.IntelNavigationService;
import top.mores.intelligencearchive.common.service.IntelService;
import top.mores.intelligencearchive.common.service.InvestigationService;

import java.util.Objects;
import java.util.UUID;

/** Phase 5-A 的最小 IntelLink 服务端导航实现。 */
public final class SimpleIntelNavigationService implements IntelNavigationService {
    private final IntelService intelService;
    private final ArchiveContentService contentService;
    private final InvestigationService investigationService;

    public SimpleIntelNavigationService(
            IntelService intelService,
            ArchiveContentService contentService,
            InvestigationService investigationService
    ) {
        this.intelService = Objects.requireNonNull(intelService, "intelService 不能为 null");
        this.contentService = Objects.requireNonNull(contentService, "contentService 不能为 null");
        this.investigationService = Objects.requireNonNull(investigationService, "investigationService 不能为 null");
    }

    @Override
    public IntelNavigationResult resolve(UUID playerId, String targetIntelId) {
        Objects.requireNonNull(playerId, "playerId 不能为 null");
        Objects.requireNonNull(targetIntelId, "targetIntelId 不能为 null");
        if (targetIntelId.isBlank()) {
            throw new IllegalArgumentException("targetIntelId 不能为空");
        }
        if (!investigationService.hasDiscovered(playerId, targetIntelId)) {
            return IntelNavigationResult.unknown(targetIntelId);
        }

        ArchiveDocument document = intelService.findDocumentById(targetIntelId).orElse(null);
        if (document != null && contentService.findByDocumentId(document.id()).isPresent()) {
            return new IntelNavigationResult(
                    IntelNavigationTargetType.ARCHIVE,
                    document.id(),
                    document.title(),
                    document.summary(),
                    document.id()
            );
        }

        IntelNode node = intelService.findNodeById(targetIntelId).orElse(null);
        if (node != null) {
            return new IntelNavigationResult(
                    IntelNavigationTargetType.INTEL_NODE,
                    node.id(),
                    node.name(),
                    node.description(),
                    ""
            );
        }
        return IntelNavigationResult.unknown(targetIntelId);
    }
}
