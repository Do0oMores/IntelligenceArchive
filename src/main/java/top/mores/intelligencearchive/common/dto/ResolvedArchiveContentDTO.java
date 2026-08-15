package top.mores.intelligencearchive.common.dto;

import java.util.List;
import java.util.Objects;

/**
 * 玩家可见档案内容的网络 DTO。
 *
 * <p>DTO 独立于 Resolution Domain，使协议字段、数量上限和未来兼容策略不会污染服务端模型。</p>
 */
public record ResolvedArchiveContentDTO(
        String documentId,
        String contentId,
        String version,
        List<ResolvedContentNodeDTO> nodes
) {
    public static final int MAX_ID_LENGTH = 128;
    public static final int MAX_VERSION_LENGTH = 32;
    public static final int MAX_TEXT_LENGTH = 4096;
    public static final int MAX_REFERENCE_LENGTH = 256;
    public static final int MAX_PLACEHOLDER_LENGTH = 256;
    public static final int MAX_NODES = 100;

    public ResolvedArchiveContentDTO {
        documentId = ResolvedContentDtoValidation.requireText(
                documentId,
                "documentId",
                MAX_ID_LENGTH,
                false
        );
        contentId = ResolvedContentDtoValidation.requireText(contentId, "contentId", MAX_ID_LENGTH, false);
        version = ResolvedContentDtoValidation.requireText(version, "version", MAX_VERSION_LENGTH, false);
        Objects.requireNonNull(nodes, "nodes 不能为 null");
        if (nodes.size() > MAX_NODES) {
            throw new IllegalArgumentException("nodes 数量不能超过 " + MAX_NODES);
        }
        for (ResolvedContentNodeDTO node : nodes) {
            Objects.requireNonNull(node, "nodes 不能包含 null");
        }
        nodes = List.copyOf(nodes);
    }
}
