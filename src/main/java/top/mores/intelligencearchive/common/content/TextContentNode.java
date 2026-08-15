package top.mores.intelligencearchive.common.content;

/**
 * 一段无渲染格式的普通文本。
 *
 * <p>这里不保存 HTML、Markdown AST 或 Minecraft Component；具体排版属于后续表现层。</p>
 */
public record TextContentNode(String text) implements ContentNode {
    public TextContentNode {
        text = ContentValidation.requireNonBlank(text, "text");
    }

    @Override
    public ContentNodeType type() {
        return ContentNodeType.TEXT;
    }
}
