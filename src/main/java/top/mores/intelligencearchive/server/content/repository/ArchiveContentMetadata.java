package top.mores.intelligencearchive.server.content.repository;

import top.mores.intelligencearchive.common.content.repository.ArchiveDocumentIdRules;

import java.util.Objects;
import java.util.regex.Pattern;

/** metadata.yml 当前阶段使用的最小、不可变字段集合。 */
record ArchiveContentMetadata(String documentId, String title, String type, String version) {
    private static final Pattern VERSION_PATTERN = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]*");

    ArchiveContentMetadata {
        documentId = ArchiveDocumentIdRules.requireValid(documentId);
        title = requireText(title, "title", 256);
        type = requireText(type, "type", 32);
        version = requireText(version, "version", 32);
        if (!VERSION_PATTERN.matcher(version).matches()) {
            throw new IllegalArgumentException("version 只能包含字母、数字、点、下划线和连字符");
        }
    }

    private static String requireText(String value, String fieldName, int maxLength) {
        Objects.requireNonNull(value, fieldName + " 不能为 null");
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " 不能为空");
        }
        if (trimmed.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " 长度不能超过 " + maxLength);
        }
        return trimmed;
    }
}
