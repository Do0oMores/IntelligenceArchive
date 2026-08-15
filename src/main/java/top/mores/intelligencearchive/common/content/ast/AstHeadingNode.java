package top.mores.intelligencearchive.common.content.ast;

/** Markdown ATX 标题；level 范围为 1–6。 */
public record AstHeadingNode(int level, String text) implements ArchiveAstNode {
    public AstHeadingNode {
        if (level < 1 || level > 6) {
            throw new IllegalArgumentException("heading level 必须在 1 到 6 之间");
        }
        text = AstValidation.requireNonBlank(text, "text");
    }

    @Override
    public AstNodeType type() {
        return AstNodeType.HEADING;
    }
}
