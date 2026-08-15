package top.mores.intelligencearchive.network.codec;

import net.minecraft.network.FriendlyByteBuf;
import top.mores.intelligencearchive.common.dto.ResolvedArchiveContentDTO;
import top.mores.intelligencearchive.common.dto.ResolvedAudioNodeDTO;
import top.mores.intelligencearchive.common.dto.ResolvedContentNodeDTO;
import top.mores.intelligencearchive.common.dto.ResolvedImageNodeDTO;
import top.mores.intelligencearchive.common.dto.ResolvedIntelLinkNodeDTO;
import top.mores.intelligencearchive.common.dto.ResolvedRedactedNodeDTO;
import top.mores.intelligencearchive.common.dto.ResolvedTextNodeDTO;

import java.util.ArrayList;
import java.util.List;

/** Resolved DTO 的集中、显式网络编解码器。 */
public final class ResolvedArchiveContentDtoCodec {
    private static final int TEXT_TAG = 0;
    private static final int IMAGE_TAG = 1;
    private static final int AUDIO_TAG = 2;
    private static final int REDACTED_TAG = 3;
    private static final int INTEL_LINK_TAG = 4;

    private ResolvedArchiveContentDtoCodec() {
    }

    public static void encode(ResolvedArchiveContentDTO content, FriendlyByteBuf buffer) {
        buffer.writeUtf(content.documentId(), ResolvedArchiveContentDTO.MAX_ID_LENGTH);
        buffer.writeUtf(content.contentId(), ResolvedArchiveContentDTO.MAX_ID_LENGTH);
        buffer.writeUtf(content.version(), ResolvedArchiveContentDTO.MAX_VERSION_LENGTH);
        buffer.writeVarInt(content.nodes().size());
        for (ResolvedContentNodeDTO node : content.nodes()) {
            encodeNode(node, buffer);
        }
    }

    public static ResolvedArchiveContentDTO decode(FriendlyByteBuf buffer) {
        String documentId = buffer.readUtf(ResolvedArchiveContentDTO.MAX_ID_LENGTH);
        String contentId = buffer.readUtf(ResolvedArchiveContentDTO.MAX_ID_LENGTH);
        String version = buffer.readUtf(ResolvedArchiveContentDTO.MAX_VERSION_LENGTH);
        int nodeCount = buffer.readVarInt();
        if (nodeCount < 0 || nodeCount > ResolvedArchiveContentDTO.MAX_NODES) {
            throw new IllegalArgumentException("非法 resolved node 数量: " + nodeCount);
        }
        List<ResolvedContentNodeDTO> nodes = new ArrayList<>(nodeCount);
        for (int index = 0; index < nodeCount; index++) {
            nodes.add(decodeNode(buffer));
        }
        return new ResolvedArchiveContentDTO(documentId, contentId, version, nodes);
    }

    private static void encodeNode(ResolvedContentNodeDTO node, FriendlyByteBuf buffer) {
        if (node instanceof ResolvedTextNodeDTO textNode) {
            buffer.writeVarInt(TEXT_TAG);
            buffer.writeUtf(textNode.text(), ResolvedArchiveContentDTO.MAX_TEXT_LENGTH);
            return;
        }
        if (node instanceof ResolvedImageNodeDTO imageNode) {
            buffer.writeVarInt(IMAGE_TAG);
            buffer.writeUtf(imageNode.imageReference(), ResolvedArchiveContentDTO.MAX_REFERENCE_LENGTH);
            return;
        }
        if (node instanceof ResolvedAudioNodeDTO audioNode) {
            buffer.writeVarInt(AUDIO_TAG);
            buffer.writeUtf(audioNode.audioReference(), ResolvedArchiveContentDTO.MAX_REFERENCE_LENGTH);
            return;
        }
        if (node instanceof ResolvedRedactedNodeDTO redactedNode) {
            buffer.writeVarInt(REDACTED_TAG);
            buffer.writeUtf(redactedNode.placeholder(), ResolvedArchiveContentDTO.MAX_PLACEHOLDER_LENGTH);
            buffer.writeBoolean(
                    redactedNode.state() == ResolvedRedactedNodeDTO.RedactionState.CONDITION_SATISFIED
            );
            return;
        }
        if (node instanceof ResolvedIntelLinkNodeDTO linkNode) {
            buffer.writeVarInt(INTEL_LINK_TAG);
            buffer.writeUtf(linkNode.targetIntelId(), ResolvedArchiveContentDTO.MAX_ID_LENGTH);
            return;
        }
        throw new IllegalArgumentException("不支持的 resolved DTO node: " + node.getClass().getName());
    }

    private static ResolvedContentNodeDTO decodeNode(FriendlyByteBuf buffer) {
        int tag = buffer.readVarInt();
        return switch (tag) {
            case TEXT_TAG -> new ResolvedTextNodeDTO(
                    buffer.readUtf(ResolvedArchiveContentDTO.MAX_TEXT_LENGTH)
            );
            case IMAGE_TAG -> new ResolvedImageNodeDTO(
                    buffer.readUtf(ResolvedArchiveContentDTO.MAX_REFERENCE_LENGTH)
            );
            case AUDIO_TAG -> new ResolvedAudioNodeDTO(
                    buffer.readUtf(ResolvedArchiveContentDTO.MAX_REFERENCE_LENGTH)
            );
            case REDACTED_TAG -> new ResolvedRedactedNodeDTO(
                    buffer.readUtf(ResolvedArchiveContentDTO.MAX_PLACEHOLDER_LENGTH),
                    buffer.readBoolean()
                            ? ResolvedRedactedNodeDTO.RedactionState.CONDITION_SATISFIED
                            : ResolvedRedactedNodeDTO.RedactionState.REDACTED
            );
            case INTEL_LINK_TAG -> new ResolvedIntelLinkNodeDTO(
                    buffer.readUtf(ResolvedArchiveContentDTO.MAX_ID_LENGTH)
            );
            default -> throw new IllegalArgumentException("未知 resolved DTO node tag: " + tag);
        };
    }
}
