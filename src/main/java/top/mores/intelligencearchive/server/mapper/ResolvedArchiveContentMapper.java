package top.mores.intelligencearchive.server.mapper;

import top.mores.intelligencearchive.common.content.resolution.ResolvedArchiveContent;
import top.mores.intelligencearchive.common.content.resolution.ResolvedAudioNode;
import top.mores.intelligencearchive.common.content.resolution.ResolvedContentNode;
import top.mores.intelligencearchive.common.content.resolution.ResolvedImageNode;
import top.mores.intelligencearchive.common.content.resolution.ResolvedIntelLinkNode;
import top.mores.intelligencearchive.common.content.resolution.ResolvedRedactedNode;
import top.mores.intelligencearchive.common.content.resolution.ResolvedRedactionState;
import top.mores.intelligencearchive.common.content.resolution.ResolvedTextNode;
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

/** 服务端将已解析玩家内容映射为受限网络 DTO。 */
public final class ResolvedArchiveContentMapper {
    private ResolvedArchiveContentMapper() {
    }

    public static ResolvedArchiveContentDTO toDto(ResolvedArchiveContent content) {
        ResolvedArchiveContent validContent = Objects.requireNonNull(content, "content 不能为 null");
        List<ResolvedContentNodeDTO> nodes = new ArrayList<>(validContent.nodes().size());
        for (ResolvedContentNode node : validContent.nodes()) {
            nodes.add(toDto(node));
        }
        return new ResolvedArchiveContentDTO(
                validContent.documentId(),
                validContent.contentId(),
                validContent.version(),
                nodes
        );
    }

    private static ResolvedContentNodeDTO toDto(ResolvedContentNode node) {
        if (node instanceof ResolvedTextNode textNode) {
            return new ResolvedTextNodeDTO(textNode.text());
        }
        if (node instanceof ResolvedImageNode imageNode) {
            return new ResolvedImageNodeDTO(imageNode.imageReference());
        }
        if (node instanceof ResolvedAudioNode audioNode) {
            return new ResolvedAudioNodeDTO(audioNode.audioReference());
        }
        if (node instanceof ResolvedIntelLinkNode linkNode) {
            return new ResolvedIntelLinkNodeDTO(linkNode.targetIntelId());
        }
        if (node instanceof ResolvedRedactedNode redactedNode) {
            return new ResolvedRedactedNodeDTO(
                    redactedNode.placeholder(),
                    redactedNode.state() == ResolvedRedactionState.CONDITION_SATISFIED
                            ? ResolvedRedactedNodeDTO.RedactionState.CONDITION_SATISFIED
                            : ResolvedRedactedNodeDTO.RedactionState.REDACTED
            );
        }
        throw new IllegalArgumentException("不支持的 ResolvedContentNode: " + node.getClass().getName());
    }
}
