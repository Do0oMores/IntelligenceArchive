package top.mores.intelligencearchive.common.content;

/**
 * 图片资源的逻辑引用。
 *
 * <p>节点不保存图片二进制，也不绑定 Minecraft ResourceLocation。加载器未来可以把该字符串
 * 解释为资源包路径或其他受控内容来源，而模型仍可被服务器和编辑工具复用。</p>
 */
public record ImageContentNode(String imageReference) implements ContentNode {
    public ImageContentNode {
        imageReference = ContentValidation.requireNonBlank(imageReference, "imageReference");
    }

    @Override
    public ContentNodeType type() {
        return ContentNodeType.IMAGE;
    }
}
