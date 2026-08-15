package top.mores.intelligencearchive.client.view;

import java.util.List;
import java.util.Objects;

/** Screen 使用的不可变档案展示模型。 */
public record ArchiveViewModel(
        String documentId,
        String contentId,
        String version,
        List<ArchiveViewNode> nodes
) {
    public ArchiveViewModel {
        Objects.requireNonNull(documentId, "documentId 不能为 null");
        Objects.requireNonNull(contentId, "contentId 不能为 null");
        Objects.requireNonNull(version, "version 不能为 null");
        Objects.requireNonNull(nodes, "nodes 不能为 null");
        for (ArchiveViewNode node : nodes) {
            Objects.requireNonNull(node, "nodes 不能包含 null");
        }
        nodes = List.copyOf(nodes);
    }
}
