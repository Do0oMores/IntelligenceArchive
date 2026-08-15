package top.mores.intelligencearchive.common.content;

/**
 * 档案正文中的情报对象链接。
 *
 * <p>节点只保存目标 ID，不直接持有 IntelNode。这样世界节点更新、网络传输和内容版本之间
 * 不会形成大型对象图。</p>
 */
public record IntelLinkContentNode(String targetIntelId) implements ContentNode {
    public IntelLinkContentNode {
        targetIntelId = ContentValidation.requireNonBlank(targetIntelId, "targetIntelId");
    }

    @Override
    public ContentNodeType type() {
        return ContentNodeType.INTEL_LINK;
    }
}
