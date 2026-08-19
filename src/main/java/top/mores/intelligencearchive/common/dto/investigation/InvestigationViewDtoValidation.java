package top.mores.intelligencearchive.common.dto.investigation;

import java.util.List;
import java.util.Objects;

/** Investigation View DTO 的统一边界校验，限制网络分配规模与字符串长度。 */
public final class InvestigationViewDtoValidation {
    private InvestigationViewDtoValidation() {
    }

    public static String requireText(String value, String fieldName, int maxLength) {
        Objects.requireNonNull(value, fieldName + " 不能为 null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " 不能为空");
        }
        if (value.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " 长度不能超过 " + maxLength);
        }
        return value;
    }

    public static <T> List<T> immutableList(List<T> values, String fieldName, int maxCount) {
        Objects.requireNonNull(values, fieldName + " 不能为 null");
        if (values.size() > maxCount) {
            throw new IllegalArgumentException(fieldName + " 数量不能超过 " + maxCount);
        }
        for (T value : values) {
            Objects.requireNonNull(value, fieldName + " 不能包含 null");
        }
        return List.copyOf(values);
    }
}
