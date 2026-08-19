package top.mores.intelligencearchive.common.dto;

import top.mores.intelligencearchive.common.model.ArchiveSummaryStatus;

import java.util.Objects;

/**
 * Archive Terminal 索引使用的网络摘要。
 *
 * <p>该 DTO 刻意不包含正文节点、内容引用和隐藏字段，防止索引响应变成详情响应，
 * 也避免未授权内容通过列表接口泄漏。</p>
 */
public record ArchiveSummaryDTO(
        String documentId,
        String title,
        String type,
        String securityLevel,
        String summary,
        ArchiveSummaryStatus status,
        String version
) {
    public static final int MAX_DOCUMENT_ID_LENGTH = 128;
    public static final int MAX_TITLE_LENGTH = 256;
    public static final int MAX_TYPE_LENGTH = 64;
    public static final int MAX_SECURITY_LEVEL_LENGTH = 64;
    public static final int MAX_SUMMARY_LENGTH = 512;
    public static final int MAX_VERSION_LENGTH = 64;

    public ArchiveSummaryDTO {
        documentId = requireText(documentId, "documentId", MAX_DOCUMENT_ID_LENGTH, false);
        title = requireText(title, "title", MAX_TITLE_LENGTH, false);
        type = requireText(type, "type", MAX_TYPE_LENGTH, false);
        securityLevel = requireText(securityLevel, "securityLevel", MAX_SECURITY_LEVEL_LENGTH, false);
        summary = requireText(summary, "summary", MAX_SUMMARY_LENGTH, true);
        status = Objects.requireNonNull(status, "status 不能为 null");
        version = requireText(version, "version", MAX_VERSION_LENGTH, false);
    }

    private static String requireText(String value, String fieldName, int maxLength, boolean allowEmpty) {
        Objects.requireNonNull(value, fieldName + " 不能为 null");
        if (!allowEmpty && value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " 不能为空");
        }
        if (value.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " 长度不能超过 " + maxLength);
        }
        return value;
    }
}
