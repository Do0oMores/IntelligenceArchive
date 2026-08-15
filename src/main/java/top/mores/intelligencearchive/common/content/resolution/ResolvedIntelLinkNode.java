package top.mores.intelligencearchive.common.content.resolution;

/** 玩家可见内容中的情报链接；是否可点击仍由后续 Renderer 表现。 */
public record ResolvedIntelLinkNode(String targetIntelId) implements ResolvedContentNode {
    public ResolvedIntelLinkNode {
        targetIntelId = ResolutionValidation.requireNonBlank(targetIntelId, "targetIntelId");
    }

    @Override
    public ResolvedContentNodeType type() {
        return ResolvedContentNodeType.INTEL_LINK;
    }
}
