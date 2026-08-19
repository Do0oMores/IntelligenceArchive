package top.mores.intelligencearchive.common.model;

import java.util.Objects;

/**
 * Archive Terminal 列表中的轻量档案摘要。
 *
 * <p>摘要不携带正文节点、隐藏内容或权限规则，详情仍通过独立 Resolved Content 请求获取。</p>
 */
public record ArchiveSummary(
        String documentId,
        String title,
        ArchiveDocumentType type,
        ArchiveSecurityLevel securityLevel,
        String summary,
        ArchiveSummaryStatus status,
        String version
) {
    public ArchiveSummary {
        documentId = requireText(documentId, "documentId", false);
        title = requireText(title, "title", false);
        type = Objects.requireNonNull(type, "type 不能为 null");
        securityLevel = Objects.requireNonNull(securityLevel, "securityLevel 不能为 null");
        summary = requireText(summary, "summary", true);
        status = Objects.requireNonNull(status, "status 不能为 null");
        version = requireText(version, "version", false);
    }

    private static String requireText(String value, String fieldName, boolean allowEmpty) {
        Objects.requireNonNull(value, fieldName + " 不能为 null");
        if (!allowEmpty && value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " 不能为空");
        }
        return value;
    }
}
