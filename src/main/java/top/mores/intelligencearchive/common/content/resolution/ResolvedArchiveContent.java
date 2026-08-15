package top.mores.intelligencearchive.common.content.resolution;

import java.util.List;

/**
 * 某个玩家当前可见的不可变档案内容。
 *
 * <p>Resolver 总是创建新对象，不删除或替换原始 ArchiveContent 内的节点，因此同一世界内容
 * 可以安全地为不同玩家生成不同快照。</p>
 */
public record ResolvedArchiveContent(
        String documentId,
        String contentId,
        String version,
        List<ResolvedContentNode> nodes
) {
    public ResolvedArchiveContent {
        documentId = ResolutionValidation.requireNonBlank(documentId, "documentId");
        contentId = ResolutionValidation.requireNonBlank(contentId, "contentId");
        version = ResolutionValidation.requireNonBlank(version, "version");
        nodes = ResolutionValidation.immutableNodes(nodes);
    }
}
