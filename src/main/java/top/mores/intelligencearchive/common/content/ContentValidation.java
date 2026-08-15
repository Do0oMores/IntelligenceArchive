package top.mores.intelligencearchive.common.content;

import java.util.List;
import java.util.Objects;

/** Content Model 内部共享的最小纯 Java 不变量校验。 */
final class ContentValidation {
    private ContentValidation() {
    }

    static String requireNonBlank(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " 不能为 null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " 不能为空");
        }
        return value;
    }

    static List<ContentNode> immutableNodes(List<ContentNode> nodes) {
        Objects.requireNonNull(nodes, "nodes 不能为 null");
        for (ContentNode node : nodes) {
            Objects.requireNonNull(node, "nodes 不能包含 null");
        }
        return List.copyOf(nodes);
    }
}
