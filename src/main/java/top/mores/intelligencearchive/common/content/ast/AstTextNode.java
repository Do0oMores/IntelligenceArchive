package top.mores.intelligencearchive.common.content.ast;

/** Markdown 中一行普通非空文本。 */
public record AstTextNode(String text) implements ArchiveAstNode {
    public AstTextNode {
        text = AstValidation.requireNonBlank(text, "text");
    }

    @Override
    public AstNodeType type() {
        return AstNodeType.TEXT;
    }
}
