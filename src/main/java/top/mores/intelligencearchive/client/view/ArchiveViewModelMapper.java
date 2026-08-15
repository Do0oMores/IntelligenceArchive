package top.mores.intelligencearchive.client.view;

import top.mores.intelligencearchive.common.dto.ResolvedArchiveContentDTO;
import top.mores.intelligencearchive.common.dto.ResolvedAudioNodeDTO;
import top.mores.intelligencearchive.common.dto.ResolvedContentNodeDTO;
import top.mores.intelligencearchive.common.dto.ResolvedImageNodeDTO;
import top.mores.intelligencearchive.common.dto.ResolvedIntelLinkNodeDTO;
import top.mores.intelligencearchive.common.dto.ResolvedRedactedNodeDTO;
import top.mores.intelligencearchive.common.dto.ResolvedTextNodeDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** DTO → ViewModel 的唯一客户端映射边界，Screen 不接触网络 DTO。 */
public final class ArchiveViewModelMapper {
    private ArchiveViewModelMapper() {
    }

    public static ArchiveViewModel fromDto(ResolvedArchiveContentDTO dto) {
        ResolvedArchiveContentDTO validDto = Objects.requireNonNull(dto, "dto 不能为 null");
        List<ArchiveViewNode> nodes = new ArrayList<>(validDto.nodes().size());
        for (ResolvedContentNodeDTO node : validDto.nodes()) {
            nodes.add(fromDto(node));
        }
        return new ArchiveViewModel(
                validDto.documentId(),
                validDto.contentId(),
                validDto.version(),
                nodes
        );
    }

    private static ArchiveViewNode fromDto(ResolvedContentNodeDTO node) {
        if (node instanceof ResolvedTextNodeDTO textNode) {
            return new TextViewNode(textNode.text());
        }
        if (node instanceof ResolvedImageNodeDTO imageNode) {
            return new ImageViewNode(imageNode.imageReference());
        }
        if (node instanceof ResolvedAudioNodeDTO audioNode) {
            return new AudioViewNode(audioNode.audioReference());
        }
        if (node instanceof ResolvedIntelLinkNodeDTO linkNode) {
            return new IntelLinkViewNode(linkNode.targetIntelId());
        }
        if (node instanceof ResolvedRedactedNodeDTO redactedNode) {
            return new RedactedViewNode(
                    redactedNode.placeholder(),
                    redactedNode.state() == ResolvedRedactedNodeDTO.RedactionState.CONDITION_SATISFIED
                            ? RedactedViewNode.State.CONDITION_SATISFIED
                            : RedactedViewNode.State.REDACTED
            );
        }
        throw new IllegalArgumentException("不支持的 ResolvedContentNodeDTO: " + node.getClass().getName());
    }
}
