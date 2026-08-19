package top.mores.intelligencearchive.common.model;

import java.util.Objects;

/** 服务端安全解析后的 IntelLink 目标，不包含权限规则或未授权世界数据。 */
public record IntelNavigationResult(
        IntelNavigationTargetType targetType,
        String targetId,
        String title,
        String description,
        String documentId
) {
    public IntelNavigationResult {
        targetType = Objects.requireNonNull(targetType, "targetType 不能为 null");
        targetId = requireText(targetId, "targetId", false);
        title = requireText(title, "title", targetType == IntelNavigationTargetType.UNKNOWN);
        description = requireText(description, "description", true);
        documentId = requireText(documentId, "documentId", true);
        if (targetType == IntelNavigationTargetType.ARCHIVE && documentId.isBlank()) {
            throw new IllegalArgumentException("ARCHIVE 导航结果必须包含 documentId");
        }
        if (targetType != IntelNavigationTargetType.ARCHIVE && !documentId.isEmpty()) {
            throw new IllegalArgumentException("非 ARCHIVE 导航结果不能包含 documentId");
        }
    }

    public static IntelNavigationResult unknown(String targetId) {
        return new IntelNavigationResult(IntelNavigationTargetType.UNKNOWN, targetId, "", "", "");
    }

    private static String requireText(String value, String fieldName, boolean allowEmpty) {
        Objects.requireNonNull(value, fieldName + " 不能为 null");
        if (!allowEmpty && value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " 不能为空");
        }
        return value;
    }
}
