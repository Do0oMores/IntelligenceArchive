package top.mores.intelligencearchive.common.dto;

import java.util.Objects;

/** Resolved DTO 共享的协议长度校验。 */
final class ResolvedContentDtoValidation {
    private ResolvedContentDtoValidation() {
    }

    static String requireText(String value, String fieldName, int maxLength, boolean allowEmpty) {
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
