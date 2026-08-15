package top.mores.intelligencearchive.common.content.resolution;

import top.mores.intelligencearchive.common.content.ArchiveContent;
import top.mores.intelligencearchive.common.content.AudioContentNode;
import top.mores.intelligencearchive.common.content.ContentNode;
import top.mores.intelligencearchive.common.content.ImageContentNode;
import top.mores.intelligencearchive.common.content.IntelLinkContentNode;
import top.mores.intelligencearchive.common.content.RedactedContentNode;
import top.mores.intelligencearchive.common.content.TextContentNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 将世界完整内容转换为特定玩家可见内容。
 *
 * <p>Resolver 从不修改 ArchiveContent；它为每次请求创建新的 Resolved 节点。隐藏逻辑必须在
 * 服务端此处完成，Renderer 只能消费结果，不能重新读取 conditionReference 自行决定。</p>
 */
public final class ArchiveContentResolver {
    private final ContentConditionEvaluator conditionEvaluator;

    public ArchiveContentResolver() {
        this(new SimpleContentConditionEvaluator());
    }

    public ArchiveContentResolver(ContentConditionEvaluator conditionEvaluator) {
        this.conditionEvaluator = Objects.requireNonNull(
                conditionEvaluator,
                "conditionEvaluator 不能为 null"
        );
    }

    public ResolvedArchiveContent resolve(ArchiveContent content, PlayerContentContext context) {
        ArchiveContent validContent = Objects.requireNonNull(content, "content 不能为 null");
        PlayerContentContext validContext = Objects.requireNonNull(context, "context 不能为 null");
        List<ResolvedContentNode> resolvedNodes = new ArrayList<>(validContent.nodes().size());
        for (ContentNode node : validContent.nodes()) {
            resolvedNodes.add(resolveNode(node, validContext));
        }
        return new ResolvedArchiveContent(
                validContent.documentId(),
                validContent.contentId(),
                validContent.version(),
                resolvedNodes
        );
    }

    private ResolvedContentNode resolveNode(ContentNode node, PlayerContentContext context) {
        if (node instanceof TextContentNode textNode) {
            return new ResolvedTextNode(textNode.text());
        }
        if (node instanceof ImageContentNode imageNode) {
            return new ResolvedImageNode(imageNode.imageReference());
        }
        if (node instanceof AudioContentNode audioNode) {
            return new ResolvedAudioNode(audioNode.audioReference());
        }
        if (node instanceof IntelLinkContentNode intelLinkNode) {
            return new ResolvedIntelLinkNode(intelLinkNode.targetIntelId());
        }
        if (node instanceof RedactedContentNode redactedNode) {
            boolean conditionSatisfied = conditionEvaluator.evaluate(
                    redactedNode.conditionReference(),
                    context
            );
            return new ResolvedRedactedNode(
                    redactedNode.placeholder(),
                    conditionSatisfied
                            ? ResolvedRedactionState.CONDITION_SATISFIED
                            : ResolvedRedactionState.REDACTED
            );
        }
        throw new IllegalArgumentException("不支持的 ContentNode: " + node.getClass().getName());
    }
}
