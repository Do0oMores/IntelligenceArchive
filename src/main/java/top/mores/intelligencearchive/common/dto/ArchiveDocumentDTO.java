package top.mores.intelligencearchive.common.dto;

import java.util.Objects;

/**
 * 档案的稳定网络展示对象。
 *
 * <p>DTO 与服务端 {@code ArchiveDocument} 领域模型分离，只携带客户端当前展示需要的
 * 轻量字段，不包含正文、二进制内容、Repository 信息或服务端内部状态。类型和安全等级
 * 使用稳定字符串，避免网络协议直接绑定领域枚举实现。</p>
 */
public record ArchiveDocumentDTO(
        String id,
        String title,
        String type,
        String summary,
        String contentReference,
        long createdTimeEpochMillis,
        String author,
        String securityLevel
) {
    public static final int MAX_ID_LENGTH = 128;
    public static final int MAX_TITLE_LENGTH = 256;
    public static final int MAX_TYPE_LENGTH = 64;
    public static final int MAX_SUMMARY_LENGTH = 2_048;
    public static final int MAX_CONTENT_REFERENCE_LENGTH = 256;
    public static final int MAX_AUTHOR_LENGTH = 128;
    public static final int MAX_SECURITY_LEVEL_LENGTH = 64;

    public ArchiveDocumentDTO {
        id = requireNonBlank(id, "id", MAX_ID_LENGTH);
        title = requireNonBlank(title, "title", MAX_TITLE_LENGTH);
        type = requireNonBlank(type, "type", MAX_TYPE_LENGTH);
        summary = requireText(summary, "summary", MAX_SUMMARY_LENGTH);
        contentReference = requireNonBlank(
                contentReference,
                "contentReference",
                MAX_CONTENT_REFERENCE_LENGTH
        );
        author = requireNonBlank(author, "author", MAX_AUTHOR_LENGTH);
        securityLevel = requireNonBlank(securityLevel, "securityLevel", MAX_SECURITY_LEVEL_LENGTH);
    }

    private static String requireNonBlank(String value, String fieldName, int maxLength) {
        String validValue = requireText(value, fieldName, maxLength);
        if (validValue.isBlank()) {
            throw new IllegalArgumentException(fieldName + " 不能为空");
        }
        return validValue;
    }

    private static String requireText(String value, String fieldName, int maxLength) {
        Objects.requireNonNull(value, fieldName + " 不能为 null");
        if (value.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " 长度不能超过 " + maxLength);
        }
        return value;
    }
}
