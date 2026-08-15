package top.mores.intelligencearchive.common.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/** 领域对象共享的最小不变量校验，避免每个模型重复实现空值与空白检查。 */
final class DomainValidation {
    private DomainValidation() {
    }

    static String requireNonBlank(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " 不能为 null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " 不能为空");
        }
        return value;
    }

    static String requireText(String value, String fieldName) {
        return Objects.requireNonNull(value, fieldName + " 不能为 null");
    }

    static Set<String> immutableIdentifiers(Set<String> identifiers, String fieldName) {
        Objects.requireNonNull(identifiers, fieldName + " 不能为 null");

        LinkedHashSet<String> copy = new LinkedHashSet<>();
        for (String identifier : identifiers) {
            copy.add(requireNonBlank(identifier, fieldName + " 中的标识"));
        }
        return Collections.unmodifiableSet(copy);
    }
}
