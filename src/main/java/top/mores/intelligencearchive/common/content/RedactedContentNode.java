package top.mores.intelligencearchive.common.content;

/**
 * 尚不可见内容的占位节点。
 *
 * <p>{@code conditionReference} 只是未来服务端条件系统使用的逻辑 ID，本模型不判断条件。
 * 节点也不携带被隐藏原文，避免表现层仅凭客户端模型绕过服务端权威决定。</p>
 */
public record RedactedContentNode(String placeholder, String conditionReference) implements ContentNode {
    public RedactedContentNode {
        placeholder = ContentValidation.requireNonBlank(placeholder, "placeholder");
        conditionReference = ContentValidation.requireNonBlank(conditionReference, "conditionReference");
    }

    @Override
    public ContentNodeType type() {
        return ContentNodeType.REDACTED;
    }
}
