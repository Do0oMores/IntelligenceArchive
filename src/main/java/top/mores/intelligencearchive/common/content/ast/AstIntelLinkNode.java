package top.mores.intelligencearchive.common.content.ast;

/** {@code [intel:id]} 扩展语法的中间节点。 */
public record AstIntelLinkNode(String targetIntelId) implements ArchiveAstNode {
    public AstIntelLinkNode {
        targetIntelId = AstValidation.requireNonBlank(targetIntelId, "targetIntelId");
    }

    @Override
    public AstNodeType type() {
        return AstNodeType.INTEL_LINK;
    }
}
