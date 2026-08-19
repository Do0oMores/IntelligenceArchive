package top.mores.intelligencearchive.common.casefile.state;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/** 玩家案件状态模型共享的最小校验。 */
final class CaseStateValidation {
    private CaseStateValidation() {
    }

    static String requireId(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " 不能为 null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " 不能为空");
        }
        return value;
    }

    static Set<String> immutableIds(Set<String> values, String fieldName) {
        Objects.requireNonNull(values, fieldName + " 不能为 null");
        LinkedHashSet<String> copy = new LinkedHashSet<>();
        for (String value : values) {
            copy.add(requireId(value, fieldName + " 中的 ID"));
        }
        return Collections.unmodifiableSet(copy);
    }
}
