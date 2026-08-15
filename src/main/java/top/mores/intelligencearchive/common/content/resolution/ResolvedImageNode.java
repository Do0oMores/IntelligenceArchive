package top.mores.intelligencearchive.common.content.resolution;

/** 已允许玩家看到的图片引用；本节点不加载或验证资源。 */
public record ResolvedImageNode(String imageReference) implements ResolvedContentNode {
    public ResolvedImageNode {
        imageReference = ResolutionValidation.requireNonBlank(imageReference, "imageReference");
    }

    @Override
    public ResolvedContentNodeType type() {
        return ResolvedContentNodeType.IMAGE;
    }
}
