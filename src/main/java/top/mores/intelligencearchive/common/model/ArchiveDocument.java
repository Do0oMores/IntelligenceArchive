package top.mores.intelligencearchive.common.model;

import java.util.Objects;
import java.util.Set;

/**
 * 一份可查询的情报档案。
 *
 * <p>模型只保存 {@code contentReference}，不保存 Markdown 全文、图片二进制或音频数据。
 * 这样领域对象可以保持轻量，并允许未来由资源包、文件系统或其他内容提供器加载正文，
 * 而不会让数据库记录和网络对象被大型内容绑定。</p>
 *
 * @param links 关联档案的 ID 集合，不直接持有其他档案对象
 */
public record ArchiveDocument(
        String id,
        String title,
        ArchiveDocumentType type,
        String summary,
        String contentReference,
        ArchiveMetadata metadata,
        Set<String> links
) {
    public ArchiveDocument {
        id = DomainValidation.requireNonBlank(id, "id");
        title = DomainValidation.requireNonBlank(title, "title");
        type = Objects.requireNonNull(type, "type 不能为 null");
        summary = DomainValidation.requireText(summary, "summary");
        contentReference = DomainValidation.requireNonBlank(contentReference, "contentReference");
        metadata = Objects.requireNonNull(metadata, "metadata 不能为 null");
        links = DomainValidation.immutableIdentifiers(links, "links");
    }
}
