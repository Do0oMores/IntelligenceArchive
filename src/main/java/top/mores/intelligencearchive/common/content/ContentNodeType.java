package top.mores.intelligencearchive.common.content;

/**
 * 内容节点的稳定分类。
 *
 * <p>该枚举只描述内容语义，不包含渲染方式；客户端、服务器和编辑工具可以各自解释同一类型。</p>
 */
public enum ContentNodeType {
    TEXT,
    IMAGE,
    AUDIO,
    REDACTED,
    INTEL_LINK
}
