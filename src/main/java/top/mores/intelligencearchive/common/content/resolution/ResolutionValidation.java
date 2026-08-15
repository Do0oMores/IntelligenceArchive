package top.mores.intelligencearchive.common.content.resolution;

import java.util.List;
import java.util.Objects;

/** Resolution 模型共享的不变量校验。 */
final class ResolutionValidation {
    private ResolutionValidation() {
    }

    static String requireNonBlank(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " 不能为 null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " 不能为空");
        }
        return value;
    }

    static List<ResolvedContentNode> immutableNodes(List<ResolvedContentNode> nodes) {
        Objects.requireNonNull(nodes, "nodes 不能为 null");
        for (ResolvedContentNode node : nodes) {
            Objects.requireNonNull(node, "nodes 不能包含 null");
        }
        return List.copyOf(nodes);
    }
}
