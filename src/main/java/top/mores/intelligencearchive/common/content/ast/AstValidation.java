package top.mores.intelligencearchive.common.content.ast;

import java.util.List;
import java.util.Objects;

/** AST 内部共享的不变量校验。 */
final class AstValidation {
    private AstValidation() {
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

    static List<ArchiveAstNode> immutableNodes(List<ArchiveAstNode> nodes) {
        Objects.requireNonNull(nodes, "nodes 不能为 null");
        for (ArchiveAstNode node : nodes) {
            Objects.requireNonNull(node, "nodes 不能包含 null");
        }
        return List.copyOf(nodes);
    }
}
