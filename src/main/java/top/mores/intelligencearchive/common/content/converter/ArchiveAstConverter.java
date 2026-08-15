package top.mores.intelligencearchive.common.content.converter;

import top.mores.intelligencearchive.common.content.ArchiveContent;
import top.mores.intelligencearchive.common.content.AudioContentNode;
import top.mores.intelligencearchive.common.content.ContentNode;
import top.mores.intelligencearchive.common.content.ImageContentNode;
import top.mores.intelligencearchive.common.content.IntelLinkContentNode;
import top.mores.intelligencearchive.common.content.RedactedContentNode;
import top.mores.intelligencearchive.common.content.TextContentNode;
import top.mores.intelligencearchive.common.content.ast.ArchiveAst;
import top.mores.intelligencearchive.common.content.ast.ArchiveAstNode;
import top.mores.intelligencearchive.common.content.ast.AstAudioNode;
import top.mores.intelligencearchive.common.content.ast.AstHeadingNode;
import top.mores.intelligencearchive.common.content.ast.AstImageNode;
import top.mores.intelligencearchive.common.content.ast.AstIntelLinkNode;
import top.mores.intelligencearchive.common.content.ast.AstRedactedNode;
import top.mores.intelligencearchive.common.content.ast.AstTextNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 将 Markdown 中间语法树映射为游戏内容模型。
 *
 * <p>转换规则集中在这里，Parser 因而不依赖 ContentNode。Phase 3-A 尚无 HeadingContentNode，
 * 标题暂时转换为纯 TextContentNode；AST 仍保留标题级别供未来转换策略使用。</p>
 */
public final class ArchiveAstConverter {
    public ArchiveContent convert(
            String contentId,
            String documentId,
            String version,
            ArchiveAst ast
    ) {
        Objects.requireNonNull(ast, "ast 不能为 null");
        List<ContentNode> contentNodes = new ArrayList<>(ast.nodes().size());
        for (ArchiveAstNode astNode : ast.nodes()) {
            contentNodes.add(convertNode(astNode));
        }
        return new ArchiveContent(contentId, documentId, version, contentNodes);
    }

    private static ContentNode convertNode(ArchiveAstNode astNode) {
        if (astNode instanceof AstTextNode textNode) {
            return new TextContentNode(textNode.text());
        }
        if (astNode instanceof AstHeadingNode headingNode) {
            return new TextContentNode(headingNode.text());
        }
        if (astNode instanceof AstImageNode imageNode) {
            return new ImageContentNode(imageNode.imageReference());
        }
        if (astNode instanceof AstAudioNode audioNode) {
            return new AudioContentNode(audioNode.audioReference());
        }
        if (astNode instanceof AstRedactedNode redactedNode) {
            return new RedactedContentNode(
                    redactedNode.placeholder(),
                    redactedNode.conditionReference()
            );
        }
        if (astNode instanceof AstIntelLinkNode intelLinkNode) {
            return new IntelLinkContentNode(intelLinkNode.targetIntelId());
        }
        throw new IllegalArgumentException("不支持的 AST 节点: " + astNode.getClass().getName());
    }
}
