package top.mores.intelligencearchive.common.content.ast;

/** Markdown 图片语法的中间节点；替代文本允许为空。 */
public record AstImageNode(String altText, String imageReference) implements ArchiveAstNode {
    public AstImageNode {
        altText = AstValidation.requireText(altText, "altText");
        imageReference = AstValidation.requireNonBlank(imageReference, "imageReference");
    }

    @Override
    public AstNodeType type() {
        return AstNodeType.IMAGE;
    }
}
