package top.mores.intelligencearchive.common.content;

import java.util.List;

/**
 * 一份档案某个版本的有序内容主体。
 *
 * <p>ArchiveDocument 描述标题、类型、安全等级等档案身份；ArchiveContent 描述内部元素。
 * 二者通过 {@code documentId} 关联而不互相持有，使同一档案未来可以拥有多个内容版本，
 * 也允许内容加载和文档元数据采用不同存储策略。</p>
 */
public record ArchiveContent(
        String contentId,
        String documentId,
        String version,
        List<ContentNode> nodes
) {
    /** 未显式指定版本时使用最小初始版本，不实现复杂版本控制。 */
    public ArchiveContent(String contentId, String documentId, List<ContentNode> nodes) {
        this(contentId, documentId, "1", nodes);
    }

    public ArchiveContent {
        contentId = ContentValidation.requireNonBlank(contentId, "contentId");
        documentId = ContentValidation.requireNonBlank(documentId, "documentId");
        version = ContentValidation.requireNonBlank(version, "version");
        nodes = ContentValidation.immutableNodes(nodes);
    }
}
