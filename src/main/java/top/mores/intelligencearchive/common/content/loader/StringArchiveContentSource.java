package top.mores.intelligencearchive.common.content.loader;

import java.util.Objects;

/** 测试、工具或内存内容提供者使用的不可变字符串 Source。 */
public record StringArchiveContentSource(
        String contentId,
        String documentId,
        String version,
        String markdown
) implements ArchiveContentSource {
    public StringArchiveContentSource {
        contentId = requireNonBlank(contentId, "contentId");
        documentId = requireNonBlank(documentId, "documentId");
        version = requireNonBlank(version, "version");
        markdown = Objects.requireNonNull(markdown, "markdown 不能为 null");
    }

    @Override
    public String readMarkdown() {
        return markdown;
    }

    private static String requireNonBlank(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " 不能为 null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " 不能为空");
        }
        return value;
    }
}
