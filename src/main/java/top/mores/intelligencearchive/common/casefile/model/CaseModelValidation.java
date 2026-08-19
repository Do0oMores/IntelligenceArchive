package top.mores.intelligencearchive.common.casefile.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Case 纯领域模型共享的最小不变量校验。 */
final class CaseModelValidation {
    private CaseModelValidation() {
    }

    static String requireId(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " 不能为 null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " 不能为空");
        }
        return value;
    }

    static String requireText(String value, String fieldName) {
        return Objects.requireNonNull(value, fieldName + " 不能为 null");
    }

    static Set<String> immutableIds(Set<String> values, String fieldName) {
        Objects.requireNonNull(values, fieldName + " 不能为 null");
        LinkedHashSet<String> copy = new LinkedHashSet<>();
        for (String value : values) {
            copy.add(requireId(value, fieldName + " 中的 ID"));
        }
        return Collections.unmodifiableSet(copy);
    }

    static <T> List<T> immutableList(List<T> values, String fieldName) {
        Objects.requireNonNull(values, fieldName + " 不能为 null");
        for (T value : values) {
            Objects.requireNonNull(value, fieldName + " 不能包含 null");
        }
        return List.copyOf(values);
    }
}
