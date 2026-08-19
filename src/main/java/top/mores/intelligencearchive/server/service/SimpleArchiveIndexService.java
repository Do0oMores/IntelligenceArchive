package top.mores.intelligencearchive.server.service;

import top.mores.intelligencearchive.common.content.ArchiveContent;
import top.mores.intelligencearchive.common.content.service.ArchiveContentService;
import top.mores.intelligencearchive.common.model.ArchiveDocument;
import top.mores.intelligencearchive.common.model.ArchiveSummary;
import top.mores.intelligencearchive.common.model.ArchiveSummaryStatus;
import top.mores.intelligencearchive.common.model.investigation.IntelDiscoveryStatus;
import top.mores.intelligencearchive.common.model.investigation.PlayerInvestigationState;
import top.mores.intelligencearchive.common.service.ArchiveIndexService;
import top.mores.intelligencearchive.common.service.ArchiveVisibility;
import top.mores.intelligencearchive.common.service.ArchiveVisibilityResolver;
import top.mores.intelligencearchive.common.service.IntelService;
import top.mores.intelligencearchive.common.service.InvestigationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 使用世界档案、内容目录和玩家认知状态生成服务端权威索引。
 *
 * <p>只有同时存在 ArchiveDocument、ArchiveContent 且玩家可见的条目才会返回。</p>
 */
public final class SimpleArchiveIndexService implements ArchiveIndexService {
    private final IntelService intelService;
    private final ArchiveContentService contentService;
    private final InvestigationService investigationService;
    private final ArchiveVisibilityResolver visibilityResolver;

    public SimpleArchiveIndexService(
            IntelService intelService,
            ArchiveContentService contentService,
            InvestigationService investigationService,
            ArchiveVisibilityResolver visibilityResolver
    ) {
        this.intelService = Objects.requireNonNull(intelService, "intelService 不能为 null");
        this.contentService = Objects.requireNonNull(contentService, "contentService 不能为 null");
        this.investigationService = Objects.requireNonNull(investigationService, "investigationService 不能为 null");
        this.visibilityResolver = Objects.requireNonNull(visibilityResolver, "visibilityResolver 不能为 null");
    }

    @Override
    public List<ArchiveSummary> findVisibleArchives(UUID playerId, int limit) {
        Objects.requireNonNull(playerId, "playerId 不能为 null");
        if (limit <= 0) {
            throw new IllegalArgumentException("limit 必须大于 0");
        }

        PlayerInvestigationState playerState = investigationService.getPlayerState(playerId);
        List<ArchiveSummary> summaries = new ArrayList<>();
        for (String documentId : contentService.findDocumentIds()) {
            if (summaries.size() >= limit) {
                break;
            }
            ArchiveDocument document = intelService.findDocumentById(documentId).orElse(null);
            ArchiveContent content = contentService.findByDocumentId(documentId).orElse(null);
            if (document == null || content == null
                    || visibilityResolver.resolve(playerState, document) != ArchiveVisibility.VISIBLE) {
                continue;
            }
            IntelDiscoveryStatus discoveryStatus = playerState.statusOf(documentId);
            ArchiveSummaryStatus summaryStatus = discoveryStatus == IntelDiscoveryStatus.DISCOVERED
                    ? ArchiveSummaryStatus.AVAILABLE
                    : ArchiveSummaryStatus.READ;
            summaries.add(new ArchiveSummary(
                    document.id(),
                    document.title(),
                    document.type(),
                    document.metadata().securityLevel(),
                    document.summary(),
                    summaryStatus,
                    content.version()
            ));
        }
        return List.copyOf(summaries);
    }
}
