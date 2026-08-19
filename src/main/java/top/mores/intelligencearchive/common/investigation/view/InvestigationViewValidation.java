package top.mores.intelligencearchive.common.investigation.view;

import java.util.Objects;

/** View 模型共享的最小输入校验，避免无效 ID 穿过未来 DTO 边界。 */
final class InvestigationViewValidation {
    private InvestigationViewValidation() {
    }

    static String requireText(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " 不能为 null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " 不能为空");
        }
        return value;
    }
}
