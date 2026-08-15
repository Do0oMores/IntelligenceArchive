package top.mores.intelligencearchive.common.content;

/**
 * 档案内容中一个独立元素的多态边界。
 *
 * <p>不同内容类型拥有不同数据约束，使用多态节点可以避免一个巨大对象同时携带 text、image、
 * audio 等互斥字段。接口保持开放，未来可自然增加 Video、Table 或 Map 节点。</p>
 */
public interface ContentNode {
    ContentNodeType type();
}
