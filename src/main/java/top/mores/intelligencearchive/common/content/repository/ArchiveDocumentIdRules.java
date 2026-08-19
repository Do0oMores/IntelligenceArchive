package top.mores.intelligencearchive.common.content.repository;

import java.util.Objects;
import java.util.regex.Pattern;

/** 资源档案 documentId 的稳定格式规则。 */
public final class ArchiveDocumentIdRules {
    public static final int MAX_LENGTH = 128;

    // 至少两个点分段；每段仅允许小写字母、数字、下划线和连字符。
    private static final Pattern PATTERN = Pattern.compile(
            "[a-z0-9][a-z0-9_-]*(\\.[a-z0-9][a-z0-9_-]*)+"
    );

    private ArchiveDocumentIdRules() {
    }

    public static boolean isValid(String documentId) {
        return documentId != null
                && documentId.length() <= MAX_LENGTH
                && PATTERN.matcher(documentId).matches();
    }

    public static String requireValid(String documentId) {
        Objects.requireNonNull(documentId, "documentId 不能为 null");
        if (!isValid(documentId)) {
            throw new IllegalArgumentException(
                    "documentId 必须为 namespace.id 形式，且只能包含小写字母、数字、点、下划线和连字符"
            );
        }
        return documentId;
    }
}
