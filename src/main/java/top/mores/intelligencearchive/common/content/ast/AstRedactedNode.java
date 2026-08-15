package top.mores.intelligencearchive.common.content.ast;

/**
 * Redacted 块的安全中间节点。
 *
 * <p>节点只保存占位符与条件引用，绝不保存块内原文。真实内容未来必须由服务端根据玩家状态
 * 选择其他内容版本，不能把秘密文本下发后再要求客户端隐藏。</p>
 */
public record AstRedactedNode(String placeholder, String conditionReference) implements ArchiveAstNode {
    public AstRedactedNode {
        placeholder = AstValidation.requireNonBlank(placeholder, "placeholder");
        conditionReference = AstValidation.requireNonBlank(conditionReference, "conditionReference");
    }

    @Override
    public AstNodeType type() {
        return AstNodeType.REDACTED;
    }
}
