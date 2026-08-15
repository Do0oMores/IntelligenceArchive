package top.mores.intelligencearchive.common.content.resolution;

/** 玩家可见的普通文本。 */
public record ResolvedTextNode(String text) implements ResolvedContentNode {
    public ResolvedTextNode {
        text = ResolutionValidation.requireNonBlank(text, "text");
    }

    @Override
    public ResolvedContentNodeType type() {
        return ResolvedContentNodeType.TEXT;
    }
}
